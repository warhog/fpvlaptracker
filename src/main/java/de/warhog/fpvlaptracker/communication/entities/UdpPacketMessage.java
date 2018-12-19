package de.warhog.fpvlaptracker.communication.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@JsonIgnoreProperties
public class UdpPacketMessage extends UdpPacket {

    private static final Logger LOG = LoggerFactory.getLogger(UdpPacketMessage.class);

    private Long chipid;
    private String message;

    public Long getChipid() {
        return chipid;
    }

    public void setChipid(Long chipid) {
        this.chipid = chipid;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "UdpPacketMessage{" + "chipid=" + chipid + ", message=" + message + '}';
    }

}
