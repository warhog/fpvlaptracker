package de.warhog.fpvlaptracker.service;

import de.warhog.fpvlaptracker.entities.Rssi;
import de.warhog.fpvlaptracker.entities.ParticipantDeviceData;
import java.net.InetAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class RestService {

    private static final Logger LOG = LoggerFactory.getLogger(RestService.class);

    private final RestTemplate restTemplate = new RestTemplate();

    private String buildUrl(InetAddress ipAddress, String uri) {
        return "http://" + ipAddress.getHostAddress() + "/" + uri;
    }

    public Rssi getRssi(InetAddress ipAddress) {
        return restTemplate.getForObject(buildUrl(ipAddress, "rssi"), Rssi.class);
    }

    public ParticipantDeviceData getDeviceData(InetAddress ipAddress) {
        return restTemplate.getForObject(buildUrl(ipAddress, "devicedata"), ParticipantDeviceData.class);
    }
    
    public String rebootDevice(InetAddress ipAddress) {
        return restTemplate.getForObject(buildUrl(ipAddress, "reboot"), String.class);
    }
    
    public String postDeviceData(InetAddress ipAddress, ParticipantDeviceData deviceData) {
        String ret = restTemplate.postForObject(buildUrl(ipAddress, "devicedata"), deviceData, String.class);
        if (ret.trim().contains("NOK")) {
            throw new RuntimeException("cannot set devicedata");
        }
        return ret;
    }

}
