package de.warhog.fpvlaptracker.comm.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@JsonIgnoreProperties
public class RssiMeasureEntry {

    private static final Logger LOG = LoggerFactory.getLogger(RssiMeasureEntry.class);
    
    private Integer freq;
    private Integer rssi;

    public Integer getFreq() {
        return freq;
    }

    public void setFreq(Integer freq) {
        this.freq = freq;
    }

    public Integer getRssi() {
        return rssi;
    }

    public void setRssi(Integer rssi) {
        this.rssi = rssi;
    }

    @Override
    public String toString() {
        return "RssiMeasureEntry{" + "freq=" + freq + ", rssi=" + rssi + '}';
    }
    
}
