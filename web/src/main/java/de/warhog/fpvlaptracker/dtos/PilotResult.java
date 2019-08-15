package de.warhog.fpvlaptracker.dtos;

import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PilotResult {

    private static final Logger LOG = LoggerFactory.getLogger(PilotResult.class);

    private Long chipId;
    private String name;
    private String unmodifiedName;

    public Long getChipId() {
        return chipId;
    }

    public void setChipId(Long chipId) {
        this.chipId = chipId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PilotResult() {
    }

    public PilotResult(Long chipId, String name) {
        this.chipId = chipId;
        this.name = name;
    }

    public String getUnmodifiedName() {
        return unmodifiedName;
    }

    public void setUnmodifiedName(String unmodifiedName) {
        this.unmodifiedName = unmodifiedName;
    }

    @Override
    public String toString() {
        return "PilotResult{" + "chipId=" + chipId + ", name=" + name + ", unmodifiedName=" + unmodifiedName + '}';
    }
    
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 37 * hash + Objects.hashCode(this.name);
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
        final PilotResult other = (PilotResult) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        return true;
    }

    
}
