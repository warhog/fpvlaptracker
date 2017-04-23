package de.warhog.fpvlaptracker.comm.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@JsonIgnoreProperties
public class Data {

    private static final Logger LOG = LoggerFactory.getLogger(Data.class);

    private Integer thresholdLow = 0;
    private Integer thresholdHigh = 0;
    private Long minLapTime = 0L;
    private Integer rssi = 0;
    private Integer frequency = 0;

    public Integer getThresholdLow() {
        return thresholdLow;
    }

    public void setThresholdLow(Integer thresholdLow) {
        this.thresholdLow = thresholdLow;
    }

    public Integer getThresholdHigh() {
        return thresholdHigh;
    }

    public void setThresholdHigh(Integer thresholdHigh) {
        this.thresholdHigh = thresholdHigh;
    }

    public Long getMinLapTime() {
        return minLapTime;
    }

    public void setMinLapTime(Long minLapTime) {
        this.minLapTime = minLapTime;
    }

    public Integer getRssi() {
        return rssi;
    }

    public void setRssi(Integer rssi) {
        this.rssi = rssi;
    }

    public Integer getFrequency() {
        return frequency;
    }

    public void setFrequency(Integer frequency) {
        this.frequency = frequency;
    }

    @Override
    public String toString() {
        return "Data{" + "thresholdLow=" + thresholdLow + ", thresholdHigh=" + thresholdHigh + ", minLapTime=" + minLapTime + ", rssi=" + rssi + ", frequency=" + frequency + '}';
    }
    
}
