package de.warhog.fpvlaptracker.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@JsonIgnoreProperties
public class Rssi {

    private static final Logger LOG = LoggerFactory.getLogger(Rssi.class);

    private Integer rssi = 0;

    public Integer getRssi() {
        return rssi;
    }

    public void setRssi(Integer rssi) {
        this.rssi = rssi;
    }

    @Override
    public String toString() {
        return "Rssi{" + "rssi=" + rssi + '}';
    }

}
