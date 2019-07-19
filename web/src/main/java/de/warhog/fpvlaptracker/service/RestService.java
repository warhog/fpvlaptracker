package de.warhog.fpvlaptracker.service;

import de.warhog.fpvlaptracker.entities.DeviceStates;
import de.warhog.fpvlaptracker.entities.Rssi;
import de.warhog.fpvlaptracker.entities.ParticipantDeviceData;
import de.warhog.fpvlaptracker.entities.Result;
import java.net.InetAddress;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class RestService {

    private static final Logger LOG = LoggerFactory.getLogger(RestService.class);

    private String buildUrl(InetAddress ipAddress, String uri) {
        return "http://" + ipAddress.getHostAddress() + "/" + uri;
    }

    private RestTemplate restTemplate = null;

    private RestTemplate getRestTemplate() {
        if (restTemplate == null) {
            RestTemplateBuilder restTemplateBuilder = new RestTemplateBuilder();
            restTemplate = restTemplateBuilder
                    .setConnectTimeout(Duration.ofSeconds(5L))
                    .setReadTimeout(Duration.ofSeconds(5L))
                    .build();
        }
        return restTemplate;
    }

    public boolean checkAvailability(InetAddress ipAddress) {
        try {
            getRestTemplate().getForObject(buildUrl(ipAddress, "rssi"), Rssi.class);
            return true;
        } catch (RestClientException ex) {
            LOG.error("cannot reach " + ipAddress.getHostAddress(), ex);
        }
        return false;
    }

    public Rssi getRssi(InetAddress ipAddress) {
        try {
            return getRestTemplate().getForObject(buildUrl(ipAddress, "rssi"), Rssi.class);
        } catch (Exception ex) {
            LOG.error("cannot get rssi", ex);
            return new Rssi();
        }
    }

    public ParticipantDeviceData getDeviceData(InetAddress ipAddress) {
        try {
            return getRestTemplate().getForObject(buildUrl(ipAddress, "devicedata"), ParticipantDeviceData.class);
        } catch (Exception ex) {
            LOG.error("cannot get device data", ex);
            return new ParticipantDeviceData();
        }
    }

    public String rebootDevice(InetAddress ipAddress) {
        try {
            return getRestTemplate().getForObject(buildUrl(ipAddress, "reboot"), String.class);
        } catch (Exception ex) {
            LOG.error("cannot reboot device", ex);
            return "NOK";
        }
    }

    public String postDeviceData(InetAddress ipAddress, ParticipantDeviceData deviceData) {
        try {
            LOG.debug("posting device data " + deviceData.toString());
            String ret = getRestTemplate().postForObject(buildUrl(ipAddress, "devicedata"), deviceData, String.class);
            if (ret != null) {
                if (ret.trim().contains("NOK")) {
                    throw new RuntimeException("cannot set devicedata");
                }
            }
            return ret;
        } catch (Exception ex) {
            LOG.error("cannot post device data: " + ex.getMessage(), ex);
            return "NOK";
        }
    }

    public Result setState(InetAddress ipAddress, String state) {
        try {
            DeviceStates deviceStateFound = null;
            for (DeviceStates deviceState : DeviceStates.values()) {
                if (deviceState.name().toUpperCase().equals(state.toUpperCase())) {
                    deviceStateFound = deviceState;
                    break;
                }
            }
            if (deviceStateFound == null) {
                LOG.error("device state not found: " + state);
                throw new RuntimeException("invalid state given: " + state);
            }
            return getRestTemplate().getForObject(buildUrl(ipAddress, "setstate") + "?state={state}", Result.class, state.toUpperCase());
        } catch (Exception ex) {
            LOG.error("cannot set state: " + ex.getMessage(), ex);
            return new Result("NOK");
        }
    }

}