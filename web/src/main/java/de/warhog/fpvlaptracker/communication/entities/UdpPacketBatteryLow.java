package de.warhog.fpvlaptracker.communication.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@JsonIgnoreProperties
public class UdpPacketBatteryLow extends UdpPacket {

    private static final Logger LOG = LoggerFactory.getLogger(UdpPacketBatteryLow.class);

    private Long chipid;
    private Double voltage;

    public Long getChipid() {
        return chipid;
    }

    public void setChipid(Long chipid) {
        this.chipid = chipid;
    }

    public Double getVoltage() {
        return voltage;
    }

    public void setVoltage(Double voltage) {
        this.voltage = voltage;
    }

    @Override
    public String toString() {
        return "UdpPacketBatteryLow{" + "chipid=" + chipid + ", voltage=" + voltage + '}';
    }

}
