package de.warhog.fpvlaptracker.controllers.dtos.requestbodies;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MinLapTime extends Chipid {

    private static final Logger LOG = LoggerFactory.getLogger(MinLapTime.class);
    
    private Integer minlaptime;

    public Integer getMinlaptime() {
        return minlaptime;
    }

    public void setMinlaptime(Integer minlaptime) {
        this.minlaptime = minlaptime;
    }

}
