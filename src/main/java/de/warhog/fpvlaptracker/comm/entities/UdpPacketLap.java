package de.warhog.fpvlaptracker.comm.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@JsonIgnoreProperties
public class UdpPacketLap extends UdpPacket {

    private static final Logger LOG = LoggerFactory.getLogger(UdpPacketLap.class);

    private Integer chipid;
    private Long duration;
    private Integer rssi;

    public Integer getChipid() {
        return chipid;
    }

    public void setChipid(Integer chipid) {
        this.chipid = chipid;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public Integer getRssi() {
        return rssi;
    }

    public void setRssi(Integer rssi) {
        this.rssi = rssi;
    }

    @Override
    public String toString() {
        return "UdpPacketLap{" + "chipid=" + chipid + ", duration=" + duration + ", rssi=" + rssi + '}';
    }

}
