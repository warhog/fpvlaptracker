package de.warhog.fpvlaptracker.communication;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.warhog.fpvlaptracker.communication.entities.UdpPacketBatteryLow;
import de.warhog.fpvlaptracker.communication.entities.UdpPacketCalibrationDone;
import de.warhog.fpvlaptracker.communication.entities.UdpPacketLap;
import de.warhog.fpvlaptracker.communication.entities.UdpPacketMessage;
import de.warhog.fpvlaptracker.communication.entities.UdpPacketRegister;
import de.warhog.fpvlaptracker.controllers.WebSocketController;
import de.warhog.fpvlaptracker.entities.Participant;
import de.warhog.fpvlaptracker.race.RaceLogic;
import de.warhog.fpvlaptracker.service.AudioService;
import de.warhog.fpvlaptracker.service.ParticipantsDbService;
import de.warhog.fpvlaptracker.service.ParticipantsService;
import de.warhog.fpvlaptracker.service.ServiceLayerException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class UdpHandler implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(UdpHandler.class);

    private DatagramSocket socket;
    private Boolean run = true;
    private Thread thr;
    private Long lastPacketReceived = 0L;

    @Autowired
    private RaceLogic race;

    @Autowired
    private ParticipantsService participantsService;

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
                name = participantsDbService.getNameForChipId(udpPacketRegister.getChipid());
            } catch (ServiceLayerException ex) {
                LOG.debug("no name for chipid " + udpPacketRegister.getChipid());
            }
            Participant participant = new Participant(name, udpPacketRegister.getChipid(), inetAddress);
            if (participantsService.hasParticipant(participant)) {
                LOG.error("participant already existing: " + udpPacketRegister.getChipid(), participant);
                return;
            }
            participantsService.addParticipant(participant);
            webSocketController.sendNewParticipantMessage(udpPacketRegister.getChipid());
            audioService.speakRegistered(participant.getName());
            LOG.info("registered participant: " + participant.toString());
        } catch (Exception ex) {
            LOG.error(ex.getMessage(), ex);
        }
    }

    private boolean testLocalAddress(InetAddress inetAddress) {
        if (inetAddress.isAnyLocalAddress() || inetAddress.isLoopbackAddress()) {
            LOG.info("is local address");
            return true;
        }
        try {
            return NetworkInterface.getByInetAddress(inetAddress) != null;
        } catch (SocketException ex) {
            LOG.error("cannot find address on network interface", ex);
            return false;
        }
    }

    @Override
    public void run() {
        LOG.info("udp receiver running");
        requestRegistrationBroadcast();
        ObjectMapper mapper = new ObjectMapper();
        while (run) {
            try {
                byte data[] = new byte[1024];
                DatagramPacket packet = new DatagramPacket(data, data.length);
                LOG.debug("waiting for packet");
                socket.receive(packet);
                LOG.debug("got packet: " + new String(packet.getData(), Charset.defaultCharset()).trim());

                if (testLocalAddress(((InetSocketAddress) packet.getSocketAddress()).getAddress())) {
                    LOG.info("packet from same ip");
                    continue;
                }

                lastPacketReceived = System.currentTimeMillis();

                String packetString = new String(packet.getData(), Charset.defaultCharset()).trim();
                JsonNode rootNode = mapper.readValue(packetString, JsonNode.class);
                PacketType packetType = PacketType.valueOf(rootNode.path("type").asText().toUpperCase());
                LOG.debug("packet type is " + packetType);

                switch (packetType) {
                    case REGISTER32:
                        UdpPacketRegister udpPacketRegister = mapper.readValue(packet.getData(), UdpPacketRegister.class);
                        udpPacketRegister.setPacketType(packetType);
                        processRegister(udpPacketRegister);
                        break;
                    case LAP:
                        UdpPacketLap udpPacketLap = mapper.readValue(packet.getData(), UdpPacketLap.class);
                        udpPacketLap.setPacketType(packetType);
                        processLap(udpPacketLap, packet.getAddress());
                        break;
                    case CALIBRATIONDONE:
                        LOG.info("got calibration packet");
                        UdpPacketCalibrationDone udpPacketCalibrationDone = mapper.readValue(packet.getData(), UdpPacketCalibrationDone.class);
                        udpPacketCalibrationDone.setPacketType(packetType);
                        processCalibrationDone(udpPacketCalibrationDone);
                        break;
                    case MESSAGE:
                        LOG.info("got message packet");
                        UdpPacketMessage udpPacketMessage = mapper.readValue(packet.getData(), UdpPacketMessage.class);
                        udpPacketMessage.setPacketType(packetType);
                        processMessage(udpPacketMessage);
                        break;
                    case BATTERY_LOW:
                        LOG.info("got battery low packet");
                        UdpPacketBatteryLow udpPacketBatteryLow = mapper.readValue(packet.getData(), UdpPacketBatteryLow.class);
                        udpPacketBatteryLow.setPacketType(packetType);
                        processBatteryLow(udpPacketBatteryLow);
                        break;
                    default:
                        LOG.error("unknown packet type: " + packetType);
                        break;
                }

//            } catch (InterruptedException ex) {
//                LOG.error("interrupted", ex);
//                run = false;
            } catch (Exception ex) {
                LOG.error("error during handler run: " + ex.getMessage(), ex);
            }
        }
    }

    private void processLap(UdpPacketLap udpPacketLap, InetAddress address) {
        if (!participantsService.hasParticipant(udpPacketLap.getChipid())) {
            LOG.info("got lap from non registered participant, try to get registration");
            requestRegistration(address);
        } else {
            race.addLap(udpPacketLap.getChipid(), udpPacketLap.getDuration(), udpPacketLap.getRssi());
            webSocketController.sendNewLapMessage(udpPacketLap.getChipid());
        }
    }

    @Scheduled(fixedDelay = 10000L)
    public void sendUdpStatus() {
        String status = "down";
        if (this.run && this.thr != null && this.thr.isAlive()) {
            status = "up";
            if (System.currentTimeMillis() > (this.lastPacketReceived + 10 * 60 * 1000)) {
                status += " (no msg)";
            }
        }
        webSocketController.sendStatusMessage(status);
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

    private void requestRegistrationBroadcast() {
        LOG.info("sending request registration broadcast");
        byte data[] = new byte[1024];
        try {
            DatagramPacket packet = new DatagramPacket(data, data.length, InetAddress.getByName("255.255.255.255"), 31337);
            String request = "requestRegistration";
            packet.setData(request.getBytes(Charset.defaultCharset()));
            socket.send(packet);
        } catch (IOException ex) {
            LOG.error("cannot send registration request");
        }
    }

    private void processCalibrationDone(UdpPacketCalibrationDone udpPacketCalibrationDone) {
        if (!participantsService.hasParticipant(udpPacketCalibrationDone.getChipid())) {
            LOG.info("got calibration done from non registered participant");
        } else {
            audioService.speakCalibrationDone(participantsService.getParticipant(udpPacketCalibrationDone.getChipid()).getName());
//            webSocketController.sendAudioMessage(AudioFile.CALIBRATION_DONE);
        }
    }

    private void processMessage(UdpPacketMessage udpPacketMessage) {
        if (!participantsService.hasParticipant(udpPacketMessage.getChipid())) {
            LOG.info("got message from non registered participant");
        } else {
            audioService.speak(udpPacketMessage.getMessage());
        }
    }

    private void processBatteryLow(UdpPacketBatteryLow udpPacketBatteryLow) {
        if (!participantsService.hasParticipant(udpPacketBatteryLow.getChipid())) {
            LOG.info("got battery low from non registered participant");
        } else {
            audioService.speakBatteryLow(participantsService.getParticipant(udpPacketBatteryLow.getChipid()).getName());
        }
    }

}
