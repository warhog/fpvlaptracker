package de.warhog.fpvlaptracker.controllers.dtos.requestbodies;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Chipid {

    private static final Logger LOG = LoggerFactory.getLogger(Chipid.class);
    private Integer chipid;

    public Integer getChipid() {
        return chipid;
    }

    public void setChipid(Integer chipid) {
        this.chipid = chipid;
    }

}
