package de.warhog.fpvlaptracker.communication;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.warhog.fpvlaptracker.communication.entities.UdpPacket;
import de.warhog.fpvlaptracker.communication.entities.UdpPacketBatteryLow;
import de.warhog.fpvlaptracker.communication.entities.UdpPacketBatteryShutdown;
import de.warhog.fpvlaptracker.communication.entities.UdpPacketCalibrationDone;
import de.warhog.fpvlaptracker.communication.entities.UdpPacketLap;
import de.warhog.fpvlaptracker.communication.entities.UdpPacketMessage;
import de.warhog.fpvlaptracker.communication.entities.UdpPacketRegister;
import de.warhog.fpvlaptracker.communication.entities.UdpPacketRegisterRequest;
import de.warhog.fpvlaptracker.communication.entities.UdpPacketRegisterResponse;
import de.warhog.fpvlaptracker.communication.entities.UdpPacketRssi;
import de.warhog.fpvlaptracker.communication.entities.UdpPacketScan;
import de.warhog.fpvlaptracker.configuration.ApplicationConfig;
import de.warhog.fpvlaptracker.controllers.WebSocketController;
import de.warhog.fpvlaptracker.entities.Node;
import de.warhog.fpvlaptracker.race.RaceLogicHandler;
import de.warhog.fpvlaptracker.service.AudioService;
import de.warhog.fpvlaptracker.service.LedService;
import de.warhog.fpvlaptracker.service.NodesService;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.awt.Color;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class UdpHandler implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(UdpHandler.class);

    private Boolean run = true;
    private Thread thr;
    private Thread broadcastThread;
    private Thread unicastThread;
    private Long lastPacketReceived = 0L;
    private final LinkedBlockingQueue<DatagramPacket> packetQueue = new LinkedBlockingQueue<>();

    @Autowired
    private ApplicationConfig applicationConfig;

    @Autowired
    private WebSocketController webSocketController;

    @Autowired
    private AudioService audioService;

    @Autowired
    private LedService ledService;
    
    @Autowired
    private NodesService nodeService;
    
    @Autowired
    private RaceLogicHandler raceLogicHandler;

    public void setup() {
        thr = new Thread(this, "UdphandlerMain");
        thr.start();
    }

    public void stop() {
        LOG.info("stopping udp receiver");
        run = false;
        thr.interrupt();
        unicastThread.interrupt();
        broadcastThread.interrupt();
    }

    private void processRegister(UdpPacketRegister udpPacketRegister, InetAddress sourceInetAddress) {
        try {
            ArrayList<String> supportedNodeVersions = new ArrayList<>();
            supportedNodeVersions.add("FLT32-R1.6");
            if (!supportedNodeVersions.contains(udpPacketRegister.getVersion())) {
                LOG.error("node has not supported version: " + udpPacketRegister.getVersion());
                webSocketController.sendAlertMessage(WebSocketController.WarningMessageTypes.WARNING, "unsupported firmware version", "the node " + udpPacketRegister.getChipid().toString() + " is running an unsupported firmware version (" + udpPacketRegister.getVersion() + "). expected version: " + String.join(", ", supportedNodeVersions));
                return;
            }
            Node node = new Node(udpPacketRegister.getChipid(), sourceInetAddress);
            if (nodeService.hasNode(node)) {
                LOG.error("node already existing: " + udpPacketRegister.getChipid(), node);
                return;
            }
            nodeService.addNode(node);
            LOG.info("registered node: " + node.toString());

            // reply with UdpPacketRegisterResponse
            UdpPacketRegisterResponse udpPacketRegisterResponse = new UdpPacketRegisterResponse(udpPacketRegister.getChipid(), InetAddress.getByName(applicationConfig.getNetworkServerIp()));
            LOG.debug("send registration response: " + udpPacketRegisterResponse.toString());
            sendDataUnicast(sourceInetAddress, udpPacketRegisterResponse);

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

    private class UdpHandlerBroadcast implements Runnable {

        @Override
        public void run() {
            LOG.info("udp broadcast receiver thread running");

            DatagramSocket socket = null;
            try {
                LOG.info("setting up udp receiver on broadcast ip: " + applicationConfig.getNetworkServerBroadcast());
                socket = new DatagramSocket(31337, InetAddress.getByName(applicationConfig.getNetworkServerBroadcast()));
            } catch (UnknownHostException | SocketException ex) {
                LOG.error("cannot setup broadcast udp receiver: " + ex.getMessage(), ex);
                throw new RuntimeException(ex.getMessage());
            }

            LOG.info("send out request registration broadcast");
            requestRegistrationBroadcast();

            while (run) {
                try {
                    byte data[] = new byte[4096];
                    DatagramPacket packet = new DatagramPacket(data, data.length);
                    LOG.debug("waiting for broadcast packet");
                    socket.receive(packet);
                    LOG.debug("got broadcast packet: " + new String(packet.getData(), Charset.defaultCharset()).trim());
                    if (!packetQueue.offer(packet)) {
                        LOG.error("cannot offer packet to queue! " + packet.toString());
                    }
                } catch (Exception ex) {
                    LOG.error("error during broadcast handler run: " + ex.getMessage(), ex);
                }
            }
        }
    }

    private class UdpHandlerUnicast implements Runnable {

        @Override
        public void run() {
            LOG.info("udp unicast receiver thread running");

            DatagramSocket socket = null;
            try {
                LOG.info("setting up udp receiver on unicast ip: " + applicationConfig.getNetworkServerIp());
                socket = new DatagramSocket(31337, InetAddress.getByName(applicationConfig.getNetworkServerIp()));
            } catch (UnknownHostException | SocketException ex) {
                LOG.error("cannot setup unicast udp receiver: " + ex.getMessage(), ex);
                throw new RuntimeException(ex.getMessage());
            }

            while (run) {
                try {
                    byte data[] = new byte[4096];
                    DatagramPacket packet = new DatagramPacket(data, data.length);
                    LOG.debug("waiting for unicast packet");
                    socket.receive(packet);
                    LOG.debug("got unicast packet: " + new String(packet.getData(), Charset.defaultCharset()).trim());
                    if (!packetQueue.offer(packet)) {
                        LOG.error("cannot offer packet to queue! " + packet.toString());
                    }
                } catch (Exception ex) {
                    LOG.error("error during unicast handler run: " + ex.getMessage(), ex);
                }
            }
        }
    }

    @SuppressFBWarnings(value = "REC_CATCH_EXCEPTION", justification = "catch exception to make sure all types of exceptions are catched and the loop is not ended in this cases")
    public void processIncomingPacket(DatagramPacket packet) {
        LOG.debug("processing incoming packet");
        ObjectMapper mapper = new ObjectMapper();
        try {
            // test if address is from any local address, if so skip this as we sent it out
            if (testLocalAddress(packet.getAddress())) {
                LOG.debug("packet from any local address, skipping");
                return;
            }

            lastPacketReceived = System.currentTimeMillis();

            String packetString = new String(packet.getData(), Charset.defaultCharset()).trim();
            if (!isValidJson(packetString)) {
                LOG.debug("invalid json string, skipping");
                return;
            }
            JsonNode rootNode = mapper.readValue(packetString, JsonNode.class);
            PacketType packetType;
            try {
                packetType = PacketType.valueOf(rootNode.path("type").asText().toUpperCase());
            } catch (IllegalArgumentException ex) {
                LOG.debug("cannot get packettype, skipping");
                return;
            }
            LOG.debug("packet type is " + packetType);

            switch (packetType) {
                case REGISTER32:
                    UdpPacketRegister udpPacketRegister = mapper.readValue(packet.getData(), UdpPacketRegister.class);
                    udpPacketRegister.setPacketType(packetType);
                    processRegister(udpPacketRegister, packet.getAddress());
                    break;
                case REGISTERLED:
                    UdpPacketRegister udpPacketRegisterLed = mapper.readValue(packet.getData(), UdpPacketRegister.class);
                    udpPacketRegisterLed.setPacketType(packetType);
                    processRegisterLed(udpPacketRegisterLed, packet.getAddress());
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
                case SCAN:
                    UdpPacketScan udpPacketScan = mapper.readValue(packet.getData(), UdpPacketScan.class);
                    udpPacketScan.setPacketType(packetType);
                    processScan(udpPacketScan, packet.getAddress());
                    break;
                case CALIBRATIONDONE:
                    LOG.info("got calibration packet");
                    UdpPacketCalibrationDone udpPacketCalibrationDone = mapper.readValue(packet.getData(), UdpPacketCalibrationDone.class);
                    udpPacketCalibrationDone.setPacketType(packetType);
                    processCalibrationDone(udpPacketCalibrationDone, packet.getAddress());
                    break;
                case MESSAGE:
                    LOG.info("got message packet");
                    UdpPacketMessage udpPacketMessage = mapper.readValue(packet.getData(), UdpPacketMessage.class);
                    udpPacketMessage.setPacketType(packetType);
                    processMessage(udpPacketMessage, packet.getAddress());
                    break;
                case BATTERY_LOW:
                    LOG.info("got battery low packet");
                    UdpPacketBatteryLow udpPacketBatteryLow = mapper.readValue(packet.getData(), UdpPacketBatteryLow.class);
                    udpPacketBatteryLow.setPacketType(packetType);
                    processBatteryLow(udpPacketBatteryLow, packet.getAddress());
                    break;
                case BATTERY_SHUTDOWN:
                    LOG.info("got battery shutdown packet");
                    UdpPacketBatteryShutdown udpPacketBatteryShutdown = mapper.readValue(packet.getData(), UdpPacketBatteryShutdown.class);
                    udpPacketBatteryShutdown.setPacketType(packetType);
                    processBatteryShutdown(udpPacketBatteryShutdown, packet.getAddress());
                    break;
                default:
                    LOG.error("unknown packet type: " + packetType);
                    break;
            }

        } catch (Exception ex) {
            LOG.error("error during handler run: " + ex.getMessage(), ex);
        }
    }

    @Override
    public void run() {
        LOG.debug("udphandler thread running, starting subthreads");

        broadcastThread = new Thread(new UdpHandlerBroadcast(), "BroadcastUdpHandler");
        unicastThread = new Thread(new UdpHandlerUnicast(), "UnicastUdpHandler");
        broadcastThread.start();
        unicastThread.start();

        while (run) {
            try {
                LOG.debug("waiting for next packet from queue");
                DatagramPacket datagramPacket = packetQueue.take();
                LOG.debug("got next packet from queue -> processing");
                processIncomingPacket(datagramPacket);
            } catch (InterruptedException ex) {
                LOG.info("interrupted while taking packet from packetQueue");
                run = false;
            }
        }
    }

    private void processLap(UdpPacketLap udpPacketLap, InetAddress address) {
        if (!nodeService.hasNode(udpPacketLap.getChipid())) {
            LOG.info("got lap from non registered node, try to get node registration");
            requestRegistration(address);
        } else {
            LOG.debug("got lap: " + udpPacketLap.toString());
            raceLogicHandler.addLap(udpPacketLap.getChipid(), udpPacketLap.getDuration(), udpPacketLap.getRssi());
        }
    }

    @Scheduled(fixedDelay = 10000L)
    public void sendUdpStatus() {
        String status = "down";
        if (this.run && this.thr != null && this.thr.isAlive()
                && this.broadcastThread != null && this.broadcastThread.isAlive()
                && this.unicastThread != null && this.unicastThread.isAlive()) {
            status = "up";
            if (System.currentTimeMillis() > (this.lastPacketReceived + 10 * 60 * 1000)) {
                status += " (no msg)";
            }
        }
        webSocketController.sendStatusMessage(status);
    }

    private void requestRegistration(InetAddress address) {
        LOG.info("sending request registration unicast to " + address.toString());
        sendDataBroadcast(new UdpPacketRegisterRequest());
    }

    private void requestRegistrationBroadcast() {
        LOG.info("sending request registration broadcast");
        sendDataBroadcast(new UdpPacketRegisterRequest());
    }

    public void sendDataBroadcast(UdpPacket data) {
        LOG.debug("send data broadcast: " + data.toString());
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            sendDataBroadcast(objectMapper.writeValueAsString(data));
        } catch (JsonProcessingException ex) {
            LOG.error("cannot stringify data: " + ex.getMessage(), ex);
            throw new RuntimeException("cannot stringify data: " + ex.getMessage());
        }
    }

    public void sendDataBroadcast(String data) {
        LOG.debug("sending broadcast: " + data);
        byte dataBuf[] = new byte[1024];
        try {
            DatagramPacket packet = new DatagramPacket(dataBuf, dataBuf.length, InetAddress.getByName(applicationConfig.getNetworkServerBroadcast()), 31337);
            packet.setData(data.getBytes(Charset.defaultCharset()));
            DatagramSocket datagramSocket = new DatagramSocket();
            datagramSocket.send(packet);
        } catch (IOException ex) {
            LOG.error("cannot send data broadcast: " + ex.getMessage(), ex);
        }
    }

    public void sendDataUnicast(InetAddress address, UdpPacket data) {
        LOG.debug("send data unicast to " + address.toString() + ": " + data.toString());
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            sendDataUnicast(address, objectMapper.writeValueAsString(data));
        } catch (JsonProcessingException ex) {
            LOG.error("cannot stringify data: " + ex.getMessage(), ex);
            throw new RuntimeException("cannot stringify data: " + ex.getMessage());
        }
    }

    public void sendDataUnicast(InetAddress address, String data) {
        LOG.debug("sending unicast to " + address.toString() + ": " + data);
        byte dataBuf[] = new byte[1024];
        try {
            DatagramPacket packet = new DatagramPacket(dataBuf, dataBuf.length, address, 31337);
            packet.setData(data.getBytes(Charset.defaultCharset()));
            DatagramSocket datagramSocket = new DatagramSocket();
            datagramSocket.send(packet);
        } catch (IOException ex) {
            LOG.error("cannot send data unicast: " + ex.getMessage(), ex);
        }
    }

    private void processCalibrationDone(UdpPacketCalibrationDone udpPacketCalibrationDone, InetAddress address) {
        if (!nodeService.hasNode(udpPacketCalibrationDone.getChipid())) {
            LOG.info("got calibration done from non registered node");
            requestRegistration(address);
        } else {
            audioService.speakCalibrationDone(udpPacketCalibrationDone.getChipid().toString());
            webSocketController.sendAlertMessage(WebSocketController.WarningMessageTypes.INFO, "calibration done", "calibration done for node " + udpPacketCalibrationDone.getChipid().toString());
        }
    }

    private void processMessage(UdpPacketMessage udpPacketMessage,  InetAddress address) {
        if (!nodeService.hasNode(udpPacketMessage.getChipid())) {
            LOG.info("got message from non registered node");
            requestRegistration(address);
        } else {
            audioService.speak(udpPacketMessage.getMessage());
        }
    }

    private void processBatteryLow(UdpPacketBatteryLow udpPacketBatteryLow, InetAddress address) {
        if (!nodeService.hasNode(udpPacketBatteryLow.getChipid())) {
            LOG.info("got battery low from non registered node");
            requestRegistration(address);
        } else {
            audioService.speakBatteryLow();
            webSocketController.sendAlertMessage(WebSocketController.WarningMessageTypes.WARNING, "battery low", "battery of node " + udpPacketBatteryLow.getChipid().toString() + " is almost empty (" + String.format("%.1f", udpPacketBatteryLow.getVoltage()) + " Volt)");
        }
    }

    private void processBatteryShutdown(UdpPacketBatteryShutdown udpPacketBatteryShutdown, InetAddress address) {
        if (!nodeService.hasNode(udpPacketBatteryShutdown.getChipid())) {
            LOG.info("got battery shutdown from non registered node");
            requestRegistration(address);
        } else {
            audioService.speakBatteryShutdown();
            webSocketController.sendAlertMessage(WebSocketController.WarningMessageTypes.DANGER, "battery shutdown voltage reached", "battery of node " + udpPacketBatteryShutdown.getChipid().toString() + " is empty, shutting down tracker node", true);
        }
    }

    private void processRegisterLed(UdpPacketRegister udpPacketRegisterLed, InetAddress address) {

        ledService.addInetAddress(address);

        try {
            webSocketController.sendAlertMessage(WebSocketController.WarningMessageTypes.INFO, "led board", "led board successfully registered");
            LOG.info("registered led board: " + address.toString());
        } catch (Exception ex) {
            LOG.error(ex.getMessage(), ex);
        }

        ledService.countdownColor(Color.blue, 5000);
    }

    private void processRssi(UdpPacketRssi udpPacketRssi, InetAddress address) {
        if (!nodeService.hasNode(udpPacketRssi.getChipid())) {
            LOG.info("got rssi from non registered node");
            requestRegistration(address);
        } else {
            LOG.debug("update rssi to " + udpPacketRssi.getRssi() + " of " + udpPacketRssi.getChipid());
            webSocketController.sendRssiMessage(udpPacketRssi.getChipid(), udpPacketRssi.getRssi());
        }
    }

    private void processScan(UdpPacketScan udpPacketScan, InetAddress address) {
        if (!nodeService.hasNode(udpPacketScan.getChipid())) {
            LOG.info("got scan from non registered node");
            requestRegistration(address);
        } else {
            LOG.debug("update rssi for frequency " + udpPacketScan.getFrequency() + " to " + udpPacketScan.getRssi() + " of " + udpPacketScan.getChipid());
            webSocketController.sendScanMessage(udpPacketScan.getChipid(), udpPacketScan.getFrequency(), udpPacketScan.getRssi());
        }
    }

}
