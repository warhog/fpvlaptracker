package de.warhog.fpvlaptracker.communication;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.warhog.fpvlaptracker.communication.entities.UdpPacketLap;
import de.warhog.fpvlaptracker.communication.entities.UdpPacketRegister;
import de.warhog.fpvlaptracker.controllers.WebSocketController;
import de.warhog.fpvlaptracker.race.entities.Participant;
import de.warhog.fpvlaptracker.race.RaceLogic;
import de.warhog.fpvlaptracker.service.AudioService;
import de.warhog.fpvlaptracker.service.ParticipantsDbService;
import de.warhog.fpvlaptracker.service.ServiceLayerException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UdpHandler implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(UdpHandler.class);

    private DatagramSocket socket;
    private Boolean run = true;
    private Thread thr;

    @Autowired
    private RaceLogic race;

    @Autowired
    private ParticipantsDbService participantsDbService;

    @Autowired
    private WebSocketController webSocketController;
    
    @Autowired
    private AudioService audioService;

    public void setup() {
        try {
            LOG.info("setting up udp receiver");
            socket = new DatagramSocket(31337, InetAddress.getByName("0.0.0.0"));
            thr = new Thread(this);
            thr.start();
        } catch (UnknownHostException | SocketException ex) {
            LOG.error(ex.getMessage(), ex);
            throw new RuntimeException(ex.getMessage());
        }
    }

    public void stop() {
        LOG.info("stopping udp receiver");
        run = false;
        thr.interrupt();
    }

    private void processRegister(UdpPacketRegister udpPacketRegister) {
        Long ip = udpPacketRegister.getIp();
        String ipStr = String.format("%d.%d.%d.%d",
                (ip & 0xff),
                (ip >> 8 & 0xff),
                (ip >> 16 & 0xff),
                (ip >> 24 & 0xff));

        InetAddress inetAddress;
        try {
            inetAddress = InetAddress.getByName(ipStr);
        } catch (UnknownHostException ex) {
            LOG.error("invalid ip address given: " + ip, ex);
            throw new RuntimeException("invalid ip");
        }

        try {
            String name = udpPacketRegister.getChipid().toString();
            try {
                name = participantsDbService.getNameForChipIdFromDb(udpPacketRegister.getChipid());
            } catch (ServiceLayerException ex) {
                LOG.debug("no name for chipid " + udpPacketRegister.getChipid());
            }
            Participant participant = new Participant(name, udpPacketRegister.getChipid(), inetAddress);
            if (udpPacketRegister.getPacketType() == PacketType.REGISTERBT || udpPacketRegister.getPacketType() == PacketType.REGISTERBT2) {
                participant.setAllowConfigureName(true);
            } else if (udpPacketRegister.getPacketType() == PacketType.REGISTER) {
                participant.setAllowFullConfiguration(true);
                participant.setCallable(true);
            }
            if (participantsDbService.hasParticipant(participant)) {
                LOG.error("participant already existing: " + udpPacketRegister.getChipid(), participant);
                return;
            }
            participantsDbService.addParticipant(participant);
            webSocketController.sendNewParticipantMessage(udpPacketRegister.getChipid());
            audioService.playRegistered();
            LOG.info("registered participant: " + participant.toString());
        } catch (Exception ex) {
            LOG.error(ex.getMessage(), ex);
        }
    }

    @Override
    public void run() {
        LOG.info("udp receiver running");
        ObjectMapper mapper = new ObjectMapper();
        while (run) {
            try {
                byte data[] = new byte[1024];
                DatagramPacket packet = new DatagramPacket(data, data.length);
                LOG.debug("waiting for packet");
                socket.receive(packet);
                LOG.debug("got packet: " + new String(packet.getData(), Charset.defaultCharset()).trim());

                String packetString = new String(packet.getData(), Charset.defaultCharset()).trim();
                JsonNode rootNode = mapper.readValue(packetString, JsonNode.class);
                PacketType packetType = PacketType.valueOf(rootNode.path("type").asText().toUpperCase());
                LOG.debug("packet type is " + packetType);
                
                switch (packetType) {
                    case REGISTER:
                        UdpPacketRegister udpPacketRegister = mapper.readValue(packet.getData(), UdpPacketRegister.class);
                        udpPacketRegister.setPacketType(packetType);
                        processRegister(udpPacketRegister);
                        break;
                    case REGISTERBT:
                    case REGISTERBT2:
                    case REGISTER32:
                        UdpPacketRegister udpPacketRegisterBt = mapper.readValue(packet.getData(), UdpPacketRegister.class);
                        udpPacketRegisterBt.setPacketType(packetType);
                        processRegister(udpPacketRegisterBt);
                        break;
                    case LAP:
                        UdpPacketLap udpPacketLap = mapper.readValue(packet.getData(), UdpPacketLap.class);
                        udpPacketLap.setPacketType(packetType);
                        processLap(udpPacketLap, packet.getAddress());
                        break;
                    case CALIBRATIONDONE:
                        LOG.info("got calibration packet");
                        // TODO process packet
                        break;
                    default:
                        LOG.error("unknown packet type: " + packetType);
                        break;
                }

            } catch (IOException ex) {
                LOG.error(ex.getMessage(), ex);
                run = false;
            }
        }
    }

    private void processLap(UdpPacketLap udpPacketLap, InetAddress address) {
        if (!participantsDbService.hasParticipant(udpPacketLap.getChipid())) {
            LOG.info("got lap from non registered participant, try to get registration");
            requestRegistration(address);
        } else {
            race.addLap(udpPacketLap.getChipid(), udpPacketLap.getDuration(), udpPacketLap.getRssi());
            webSocketController.sendNewLapMessage(udpPacketLap.getChipid());
        }
    }

    private void requestRegistration(InetAddress address) {
        byte data[] = new byte[1024];
        DatagramPacket packet = new DatagramPacket(data, data.length, address, 31337);
        String request = "requestRegistration";
        packet.setData(request.getBytes(Charset.defaultCharset()));
        try {
            socket.send(packet);
        } catch (IOException ex) {
            LOG.error("cannot send registration request");
        }
    }

}
