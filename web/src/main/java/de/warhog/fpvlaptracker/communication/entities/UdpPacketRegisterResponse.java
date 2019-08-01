package de.warhog.fpvlaptracker.communication.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import de.warhog.fpvlaptracker.communication.PacketType;
import java.net.InetAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@JsonIgnoreProperties
public class UdpPacketRegisterResponse extends UdpPacket {

    private static final Logger LOG = LoggerFactory.getLogger(UdpPacketRegisterResponse.class);

    private Long chipid;
    private InetAddress hostIp;

    public UdpPacketRegisterResponse() {
        setInternalType();
    }

    public UdpPacketRegisterResponse(Long chipid, InetAddress hostIp) {
        setInternalType();
        this.chipid = chipid;
        this.hostIp = hostIp;
    }

    private void setInternalType() {
        setPacketType(PacketType.REGISTERRESPONSE);
        setType("registerresponse");
    }

    public Long getChipid() {
        return chipid;
    }

    public void setChipid(Long chipid) {
        this.chipid = chipid;
    }

    public InetAddress getHostIp() {
        return hostIp;
    }

    public void setHostIp(InetAddress hostIp) {
        this.hostIp = hostIp;
    }

    @Override
    public String toString() {
        return "UdpPacketRegisterResponse{" + "chipid=" + chipid + ", hostIp=" + hostIp + '}';
    }

}
