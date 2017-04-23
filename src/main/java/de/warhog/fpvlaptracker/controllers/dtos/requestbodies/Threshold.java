package de.warhog.fpvlaptracker.controllers.dtos.requestbodies;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Threshold extends Chipid {

    private static final Logger LOG = LoggerFactory.getLogger(Threshold.class);

    private Integer thresholdLow;
    private Integer thresholdHigh;

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
    
}
