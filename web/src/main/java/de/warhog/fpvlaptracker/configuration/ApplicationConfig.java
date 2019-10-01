package de.warhog.fpvlaptracker.configuration;

import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ApplicationConfig {

    private static final Logger LOG = LoggerFactory.getLogger(ApplicationConfig.class);

    @Value("${shutdown.machine:false}")
    private Boolean shutdownMachine;

    @Value("${admin.password:Admin}")
    private String adminPassword;

    @Value("${network.server.ip:GET_FROM_wlan0}")
    private String networkServerIp;

    @Value("${network.server.broadcast:GET_FROM_wlan0}")
    private String networkServerBroadcast;

    public Boolean isShutdownMachine() {
        return shutdownMachine;
    }

    public String getAdminPassword() {
        return adminPassword;
    }

    private InterfaceAddress getFrom(String data) {
        String interfaceName = data.replace("GET_FROM_", "");
        LOG.debug("getting network interface: " + interfaceName);
        NetworkInterface networkInterface;
        try {
            networkInterface = NetworkInterface.getByName(interfaceName);
        } catch (SocketException ex) {
            LOG.error("cannot get interface for name: " + interfaceName + ": " + ex.getMessage(), ex);
            throw new RuntimeException("cannot get interface for name: " + interfaceName);
        }
        if (networkInterface == null) {
            LOG.error("cannot get ip addresses for interface name: " + interfaceName);
            throw new RuntimeException("cannot get ip addresses for interface name: " + interfaceName);
        }
        LOG.debug("got data from network interface, getting ip addresses");
        List<InterfaceAddress> interfaceAddresses = networkInterface.getInterfaceAddresses();
        for (InterfaceAddress interfaceAddress : interfaceAddresses) {
            LOG.info(interfaceAddress.toString());
            if (interfaceAddress.getBroadcast() != null && interfaceAddress.getNetworkPrefixLength() == 24) {
                // ipv4
                // TODO currently uses the first available address
                LOG.debug("found first ipv4 address on network interface " + interfaceName + ": " + interfaceAddress.toString());
                return interfaceAddress;
            }
        }
        throw new RuntimeException("no ipv4 address found for network interface " + interfaceName);
    }

    public String getNetworkServerIp() {
        LOG.debug("getNetworkServerIp()");
        if (networkServerIp.startsWith("GET_FROM_")) {
            networkServerIp = getFrom(networkServerIp).getAddress().getHostAddress();
        }
        LOG.debug("network.server.ip=" + networkServerIp);
        return networkServerIp;
    }

    public String getNetworkServerBroadcast() {
        LOG.debug("getNetworkServerBroadcast()");
        if (networkServerBroadcast.startsWith("GET_FROM_")) {
            networkServerBroadcast = getFrom(networkServerBroadcast).getBroadcast().getHostAddress();
        }
        LOG.debug("network.server.broadcast=" + networkServerBroadcast);
        return networkServerBroadcast;
    }

}
