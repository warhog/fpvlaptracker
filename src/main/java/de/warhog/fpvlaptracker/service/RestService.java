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
        Rssi rssi = restTemplate.getForObject(buildUrl(participant, "rssi"), Rssi.class);
        return rssi;
    }

    public Data getData(Participant participant) {
        Data data = restTemplate.getForObject(buildUrl(participant, "data"), Data.class);
        return data;
    }

    public RssiMeasure getRssiMeasure(Participant participant) {
        RssiMeasure rssiMeasure = restTemplate.getForObject(buildUrl(participant, "measure"), RssiMeasure.class);
        return rssiMeasure;
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
        StatusResult ret = restTemplate.postForObject(buildUrl(participant, "data?thresholdlow=" + thresholdLow + "&thresholdhigh=" + thresholdHigh), null, StatusResult.class);
        if (!"OK".equals(ret.getStatus())) {
            throw new RuntimeException("cannot set thresholds");
        }
    }

    public void setMinLapTime(Participant participant, Long minLapTime) {
        StatusResult ret = restTemplate.postForObject(buildUrl(participant, "data?minlaptime=" + minLapTime), null, StatusResult.class);
        if (!"OK".equals(ret.getStatus())) {
            throw new RuntimeException("cannot set minimum lap time");
        }
    }

    public void setFrequency(Participant participant, Integer frequency) {
        StatusResult ret = restTemplate.postForObject(buildUrl(participant, "data?frequency=" + frequency), null, StatusResult.class);
        if (!"OK".equals(ret.getStatus())) {
            throw new RuntimeException("cannot set frequency");
        }
    }

}
