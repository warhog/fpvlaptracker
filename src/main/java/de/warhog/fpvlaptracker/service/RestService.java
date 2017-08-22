package de.warhog.fpvlaptracker.service;

import de.warhog.fpvlaptracker.communication.entities.Data;
import de.warhog.fpvlaptracker.communication.entities.Rssi;
import de.warhog.fpvlaptracker.communication.entities.RssiMeasure;
import de.warhog.fpvlaptracker.communication.entities.StatusResult;
import de.warhog.fpvlaptracker.race.entities.Participant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class RestService {

    private static final Logger LOG = LoggerFactory.getLogger(RestService.class);

    private final RestTemplate restTemplate = new RestTemplate();

    private String buildUrl(Participant participant, String uri) {
        return "http://" + participant.getIp().getHostAddress() + "/" + uri;
    }

    public Rssi getRssi(Participant participant) {
        if (participant.isCallable()) {
            Rssi rssi = restTemplate.getForObject(buildUrl(participant, "rssi"), Rssi.class);
            return rssi;
        } else {
            return new Rssi();
        }
    }

    public Data getData(Participant participant) {
        if (participant.isCallable()) {
            Data data = restTemplate.getForObject(buildUrl(participant, "data"), Data.class);
            return data;
        } else {
            return new Data();
        }
    }

    public RssiMeasure getRssiMeasure(Participant participant) {
        if (participant.isCallable()) {
            RssiMeasure rssiMeasure = restTemplate.getForObject(buildUrl(participant, "measure"), RssiMeasure.class);
            return rssiMeasure;
        } else {
            return new RssiMeasure();
        }
    }

    public Long getMinLapTime(Participant participant) {
        Data data = getData(participant);
        return data.getMinLapTime();
    }

    public Integer getThresholdLow(Participant participant) {
        Data data = getData(participant);
        return data.getThresholdLow();
    }

    public Integer getThresholdHigh(Participant participant) {
        Data data = getData(participant);
        return data.getThresholdHigh();
    }

    public void setThresholds(Participant participant, Integer thresholdLow, Integer thresholdHigh) {
        if (participant.isCallable()) {
            StatusResult ret = restTemplate.postForObject(buildUrl(participant, "data?thresholdlow=" + thresholdLow + "&thresholdhigh=" + thresholdHigh), null, StatusResult.class);
            if (!"OK".equals(ret.getStatus())) {
                throw new RuntimeException("cannot set thresholds");
            }
        }
    }

    public void setMinLapTime(Participant participant, Long minLapTime) {
        if (participant.isCallable()) {
            StatusResult ret = restTemplate.postForObject(buildUrl(participant, "data?minlaptime=" + minLapTime), null, StatusResult.class);
            if (!"OK".equals(ret.getStatus())) {
                throw new RuntimeException("cannot set minimum lap time");
            }
        }
    }

    public void setFrequency(Participant participant, Integer frequency) {
        if (participant.isCallable()) {
            StatusResult ret = restTemplate.postForObject(buildUrl(participant, "data?frequency=" + frequency), null, StatusResult.class);
            if (!"OK".equals(ret.getStatus())) {
                throw new RuntimeException("cannot set frequency");
            }
        }
    }

}
