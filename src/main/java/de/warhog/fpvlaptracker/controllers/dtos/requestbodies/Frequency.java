package de.warhog.fpvlaptracker.controllers.dtos.requestbodies;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Frequency extends Chipid {

    private static final Logger LOG = LoggerFactory.getLogger(Frequency.class);
    
    private Integer frequency;

    public Integer getFrequency() {
        return frequency;
    }

    public void setFrequency(Integer frequency) {
        this.frequency = frequency;
    }

}
