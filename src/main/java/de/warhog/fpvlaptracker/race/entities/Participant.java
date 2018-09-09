package de.warhog.fpvlaptracker.race.entities;

import java.net.InetAddress;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Participant {

    private static final Logger LOG = LoggerFactory.getLogger(Participant.class);

    private String name;
    private Long chipId;
    private InetAddress ip;
    private boolean allowFullConfiguration = false;
    private boolean allowConfigureName = false;
    private boolean callable = false;

    public Participant(String name, Long chipId, InetAddress ip) {
        this.name = name;
        this.chipId = chipId;
        this.ip = ip;
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isAllowConfiguration() {
        return allowFullConfiguration;
    }

    public void setAllowFullConfiguration(boolean allowConfiguration) {
        this.allowFullConfiguration = allowConfiguration;
    }

    public boolean isAllowConfigureName() {
        return allowConfigureName;
    }

    public void setAllowConfigureName(boolean allowConfigureName) {
        this.allowConfigureName = allowConfigureName;
    }

    public boolean isCallable() {
        return callable;
    }

    public void setCallable(boolean callable) {
        this.callable = callable;
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
