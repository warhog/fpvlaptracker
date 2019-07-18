package de.warhog.fpvlaptracker.entities;

import de.warhog.fpvlaptracker.service.RestService;
import java.net.InetAddress;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Participant {

    private static final Logger LOG = LoggerFactory.getLogger(Participant.class);

    private String name;
    private Long chipId;
    private InetAddress ip;
    private ParticipantDeviceData participantDeviceData = new ParticipantDeviceData();
    private boolean valid = true;

    public Participant(String name, Long chipId, InetAddress ip) {
        this.name = name;
        this.chipId = chipId;
        this.ip = ip;
    }

    public ParticipantDeviceData getParticipantDeviceData() {
        return participantDeviceData;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public void setParticipantDeviceData(ParticipantDeviceData participantDeviceData) {
        this.participantDeviceData = participantDeviceData;
        this.participantDeviceData.setIpAddress(getIp().getHostAddress());
        this.participantDeviceData.setParticipantName(getName());
    }
    
    public void loadParticipantDeviceDataFromUnit(RestService restService) {
        try {
            setParticipantDeviceData(restService.getDeviceData(getIp()));
        } catch (final Exception ex) {
            LOG.error("cannot load participant device data", ex);
        }
    }

    public void sendParticipantDeviceDataToUnit(RestService restService) {
        try {
            restService.postDeviceData(getIp(), getParticipantDeviceData());
        } catch (final Exception ex) {
            LOG.error("cannot send participant device data", ex);
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getChipId() {
        return chipId;
    }

    public void setChipId(Long chipId) {
        this.chipId = chipId;
    }

    public InetAddress getIp() {
        return ip;
    }

    public void setIp(InetAddress ip) {
        this.ip = ip;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + Objects.hashCode(this.chipId);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Participant other = (Participant) obj;
        if (!Objects.equals(this.chipId, other.chipId)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return name;
    }
    
    
}
