package de.warhog.fpvlaptracker.communication.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@JsonIgnoreProperties
public class UdpPacketRssi extends UdpPacket {

    private static final Logger LOG = LoggerFactory.getLogger(UdpPacketRssi.class);

    private Long chipid;
    private Integer rssi;

    public Long getChipid() {
        return chipid;
    }

    public void setChipid(Long chipid) {
        this.chipid = chipid;
    }

    public Integer getRssi() {
        return rssi;
    }

    public void setRssi(Integer rssi) {
        this.rssi = rssi;
    }

    @Override
    public String toString() {
        return "UdpPacketLap{" + "chipid=" + chipid + ", rssi=" + rssi + '}';
    }

}
