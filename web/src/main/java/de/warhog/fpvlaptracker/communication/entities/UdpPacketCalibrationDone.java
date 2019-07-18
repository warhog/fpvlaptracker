package de.warhog.fpvlaptracker.communication.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@JsonIgnoreProperties
public class UdpPacketCalibrationDone extends UdpPacket {

    private static final Logger LOG = LoggerFactory.getLogger(UdpPacketCalibrationDone.class);

    private Long chipid;

    public Long getChipid() {
        return chipid;
    }

    public void setChipid(Long chipid) {
        this.chipid = chipid;
    }

    @Override
    public String toString() {
        return "UdpPacketCalibrationDone{" + "chipid=" + chipid + '}';
    }

}
