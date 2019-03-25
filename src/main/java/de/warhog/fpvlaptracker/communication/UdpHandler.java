package de.warhog.fpvlaptracker.communication;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.warhog.fpvlaptracker.communication.entities.UdpPacketBatteryLow;
import de.warhog.fpvlaptracker.communication.entities.UdpPacketBatteryShutdown;
import de.warhog.fpvlaptracker.communication.entities.UdpPacketCalibrationDone;
import de.warhog.fpvlaptracker.communication.entities.UdpPacketLap;
import de.warhog.fpvlaptracker.communication.entities.UdpPacketMessage;
import de.warhog.fpvlaptracker.communication.entities.UdpPacketRegister;
import de.warhog.fpvlaptracker.communication.entities.UdpPacketRssi;
import de.warhog.fpvlaptracker.controllers.WebSocketController;
import de.warhog.fpvlaptracker.entities.Participant;
import de.warhog.fpvlaptracker.race.RaceLogicHandler;
import de.warhog.fpvlaptracker.service.AudioService;
import de.warhog.fpvlaptracker.service.LedService;
import de.warhog.fpvlaptracker.service.ParticipantsDbService;
import de.warhog.fpvlaptracker.service.ParticipantsService;
import de.warhog.fpvlaptracker.service.ServiceLayerException;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.awt.Color;
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
    private RaceLogicHandler race;

    @Autowired
    private ParticipantsService participantsService;

    @Autowired
    private ParticipantsDbService participantsDbService;

    @Autowired
    private WebSocketController webSocketController;

    @Autowired
    private AudioService audioService;

    @Autowired
    private LedService ledService;

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
        String ipStr = getIpFromLong(udpPacketRegister.getIp());
        InetAddress inetAddress;
        try {
            inetAddress = InetAddress.getByName(ipStr);
        } catch (UnknownHostException ex) {
            LOG.error("invalid ip address given: " + ipStr, ex);
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

    public static boolean isValidJson(String json) {
        try {
            final ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.readTree(json);
            return true;
        } catch (IOException ex) {
            return false;
        }
    }

    @SuppressFBWarnings(value = "REC_CATCH_EXCEPTION", justification = "catch exception to make sure all types of exceptions are catched and the loop is not ended in this cases")
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

                lastPacketReceived = System.currentTimeMillis();

                String packetString = new String(packet.getData(), Charset.defaultCharset()).trim();
                if (!isValidJson(packetString)) {
                    LOG.debug("invalid json string, skipping");
                    continue;
                }
                JsonNode rootNode = mapper.readValue(packetString, JsonNode.class);
                PacketType packetType = PacketType.valueOf(rootNode.path("type").asText().toUpperCase());
                LOG.debug("packet type is " + packetType);

                switch (packetType) {
                    case REGISTER32:
                        UdpPacketRegister udpPacketRegister = mapper.readValue(packet.getData(), UdpPacketRegister.class);
                        udpPacketRegister.setPacketType(packetType);
                        processRegister(udpPacketRegister);
                        break;
                    case REGISTERLED:
                        UdpPacketRegister udpPacketRegisterLed = mapper.readValue(packet.getData(), UdpPacketRegister.class);
                        udpPacketRegisterLed.setPacketType(packetType);
                        processRegisterLed(udpPacketRegisterLed);
                        break;
                    case LAP:
                        UdpPacketLap udpPacketLap = mapper.readValue(packet.getData(), UdpPacketLap.class);
                        udpPacketLap.setPacketType(packetType);
                        processLap(udpPacketLap, packet.getAddress());
                        break;
                    case RSSI:
                        UdpPacketRssi udpPacketRssi = mapper.readValue(packet.getData(), UdpPacketRssi.class);
                        udpPacketRssi.setPacketType(packetType);
                        processRssi(udpPacketRssi, packet.getAddress());
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
                    case BATTERY_SHUTDOWN:
                        LOG.info("got battery shutdown packet");
                        UdpPacketBatteryShutdown udpPacketBatteryShutdown = mapper.readValue(packet.getData(), UdpPacketBatteryShutdown.class);
                        udpPacketBatteryShutdown.setPacketType(packetType);
                        processBatteryShutdown(udpPacketBatteryShutdown);
                        break;
                    default:
                        if (testLocalAddress(((InetSocketAddress) packet.getSocketAddress()).getAddress())) {
                            LOG.info("packet from same ip");
                            continue;
                        }
                        LOG.error("unknown packet type: " + packetType);
                        break;
                }

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
        sendDataBroadcast("requestRegistration");
    }

    public void sendDataBroadcast(String data) {
        LOG.info("sending broadcast: " + data);
        byte dataBuf[] = new byte[1024];
        try {
            DatagramPacket packet = new DatagramPacket(dataBuf, dataBuf.length, InetAddress.getByName("255.255.255.255"), 31337);
            packet.setData(data.getBytes(Charset.defaultCharset()));
            socket.send(packet);
        } catch (IOException ex) {
            LOG.error("cannot send data broadcast: " + ex.getMessage(), ex);
        }
    }

    public void sendDataUnicast(InetAddress inetAddress, String data) {
        LOG.info("sending unicast to " + inetAddress.toString() + ": " + data);
        byte dataBuf[] = new byte[1024];
        try {
            DatagramPacket packet = new DatagramPacket(dataBuf, dataBuf.length, inetAddress, 31337);
            packet.setData(data.getBytes(Charset.defaultCharset()));
            socket.send(packet);
        } catch (IOException ex) {
            LOG.error("cannot send data unicast: " + ex.getMessage(), ex);
        }
    }

    private void processCalibrationDone(UdpPacketCalibrationDone udpPacketCalibrationDone) {
        if (!participantsService.hasParticipant(udpPacketCalibrationDone.getChipid())) {
            LOG.info("got calibration done from non registered participant");
        } else {
            String name = participantsService.getParticipant(udpPacketCalibrationDone.getChipid()).getName();
            audioService.speakCalibrationDone(name);
            webSocketController.sendAlertMessage(WebSocketController.WarningMessageTypes.INFO, "calibration done", "calibration done for pilot " + name);
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
            String participantName = participantsService.getParticipant(udpPacketBatteryLow.getChipid()).getName();
            audioService.speakBatteryLow(participantName);
            webSocketController.sendAlertMessage(WebSocketController.WarningMessageTypes.WARNING, "battery low", "battery of participant " + participantName + " is almost empty");
        }
    }

    private void processBatteryShutdown(UdpPacketBatteryShutdown udpPacketBatteryShutdown) {
        if (!participantsService.hasParticipant(udpPacketBatteryShutdown.getChipid())) {
            LOG.info("got battery shutdown from non registered participant");
        } else {
            String participantName = participantsService.getParticipant(udpPacketBatteryShutdown.getChipid()).getName();
            audioService.speakBatteryShutdown(participantName);
            webSocketController.sendAlertMessage(WebSocketController.WarningMessageTypes.DANGER, "battery shutdown voltage reached", "battery of participant " + participantName + " is empty, shutting down tracker node", true);
        }
    }

    private String getIpFromLong(Long ip) {
        String ipStr = String.format("%d.%d.%d.%d",
                (ip & 0xff),
                (ip >> 8 & 0xff),
                (ip >> 16 & 0xff),
                (ip >> 24 & 0xff));
        return ipStr;
    }

    private void processRegisterLed(UdpPacketRegister udpPacketRegisterLed) {
        String ipStr = getIpFromLong(udpPacketRegisterLed.getIp());
        InetAddress inetAddress;
        try {
            inetAddress = InetAddress.getByName(ipStr);
        } catch (UnknownHostException ex) {
            LOG.error("invalid ip address given: " + ipStr, ex);
            throw new RuntimeException("invalid ip");
        }

        ledService.addInetAddress(inetAddress);

        try {
            audioService.speakRegistered("LED");
            LOG.info("registered led board: " + inetAddress.toString());
        } catch (Exception ex) {
            LOG.error(ex.getMessage(), ex);
        }

        ledService.countdownColor(Color.blue, 5000);
    }

    private void processRssi(UdpPacketRssi udpPacketRssi, InetAddress address) {
        if (!participantsService.hasParticipant(udpPacketRssi.getChipid())) {
            LOG.info("got rssi from non registered participant");
        } else {
            LOG.debug("update rssi to " + udpPacketRssi.getRssi() + " of " + udpPacketRssi.getChipid());
            webSocketController.sendRssiMessage(udpPacketRssi.getChipid(), udpPacketRssi.getRssi());
//            Participant participant = participantsService.getParticipant(udpPacketRssi.getChipid());
//            participant.getParticipantDeviceData().setRssi(udpPacketRssi.getRssi());
        }
    }

}
