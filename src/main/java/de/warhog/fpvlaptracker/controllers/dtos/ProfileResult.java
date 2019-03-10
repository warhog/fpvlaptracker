package de.warhog.fpvlaptracker.controllers.dtos;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProfileResult {

    private static final Logger LOG = LoggerFactory.getLogger(ProfileResult.class);

    private Long chipId;
    private String name;
    private String data;

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

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "ProfileResult{" + "chipId=" + chipId + ", name=" + name + ", data=" + data + '}';
    }

}
