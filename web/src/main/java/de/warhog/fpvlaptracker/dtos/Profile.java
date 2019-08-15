package de.warhog.fpvlaptracker.dtos;

import java.util.Objects;

public class Profile {

    private String name;
    private Long chipId;
    private String data;

    public Profile() {
    }

    public Profile(String name, Long chipId, String data) {
        this.name = name;
        this.chipId = chipId;
        this.data = data;
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

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "Profile{" + "name=" + name + ", chipId=" + chipId + ", data=" + data + '}';
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + Objects.hashCode(this.name);
        hash = 67 * hash + Objects.hashCode(this.chipId);
        hash = 67 * hash + Objects.hashCode(this.data);
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
        final Profile other = (Profile) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.chipId, other.chipId)) {
            return false;
        }
        return true;
    }

    
}
