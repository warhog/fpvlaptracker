package de.warhog.fpvlaptracker.communication.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import de.warhog.fpvlaptracker.communication.PacketType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@JsonIgnoreProperties
public class UdpPacket {

    private static final Logger LOG = LoggerFactory.getLogger(UdpPacket.class);

    private String type;
    private PacketType packetType;

    public PacketType getPacketType() {
        return packetType;
    }

    public void setPacketType(PacketType packetType) {
        this.packetType = packetType;
    }
    
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "UdpPacket{" + "type=" + type + '}';
    }

}
