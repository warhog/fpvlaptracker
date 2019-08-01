package de.warhog.fpvlaptracker.communication.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import de.warhog.fpvlaptracker.communication.PacketType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@JsonIgnoreProperties
public class UdpPacketRegisterRequest extends UdpPacket {

    private static final Logger LOG = LoggerFactory.getLogger(UdpPacketRegisterRequest.class);

    public UdpPacketRegisterRequest() {
        setPacketType(PacketType.REGISTERREQUEST);
        setType("registerrequest");
    }

    @Override
    public String toString() {
        return "UdpPacketRegisterRequest{" + '}';
    }

}
