package de.warhog.fpvlaptracker.controllers.dtos.requestbodies;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Name extends Chipid {

    private static final Logger LOG = LoggerFactory.getLogger(Name.class);

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
}
