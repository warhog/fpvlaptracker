package de.warhog.fpvlaptracker.communication.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@JsonIgnoreProperties
public class UdpPacketRegister extends UdpPacket {

    private static final Logger LOG = LoggerFactory.getLogger(UdpPacketRegister.class);

    private Integer chipid;
    private Long ip;

    public Integer getChipid() {
        return chipid;
    }

    public void setChipid(Integer chipid) {
        this.chipid = chipid;
    }

    public Long getIp() {
        return ip;
    }

    public void setIp(Long ip) {
        this.ip = ip;
    }

    @Override
    public String toString() {
        return "UdpPacketRegister{" + "chipid=" + chipid + ", ip=" + ip + '}';
    }

}
