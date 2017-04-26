package de.warhog.fpvlaptracker.communication.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@JsonIgnoreProperties
public class RssiMeasure {

    private static final Logger LOG = LoggerFactory.getLogger(RssiMeasure.class);

    private Integer maxFreq;
    private Integer maxRssi;
    private List<RssiMeasureEntry> channels;

    public Integer getMaxFreq() {
        return maxFreq;
    }

    public void setMaxFreq(Integer maxFreq) {
        this.maxFreq = maxFreq;
    }

    public Integer getMaxRssi() {
        return maxRssi;
    }

    public void setMaxRssi(Integer maxRssi) {
        this.maxRssi = maxRssi;
    }

    public List<RssiMeasureEntry> getChannels() {
        return channels;
    }

    public void setChannels(List<RssiMeasureEntry> channels) {
        this.channels = channels;
    }

    @Override
    public String toString() {
        return "RssiMeasure{" + "maxFreq=" + maxFreq + ", maxRssi=" + maxRssi + ", channels=" + channels + '}';
    }

}
