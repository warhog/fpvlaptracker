package de.warhog.fpvlaptracker.controllers.dtos;

import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ToplistResult {
    
    private static final Logger LOG = LoggerFactory.getLogger(ToplistResult.class);
    
    private Long chipId;
    private Integer numberOfTotalLaps;
    private Duration totalDuration;
    private String name;

    public Long getChipId() {
        return chipId;
    }

    public void setChipId(Long chipId) {
        this.chipId = chipId;
    }

    public Integer getNumberOfTotalLaps() {
        return numberOfTotalLaps;
    }

    public void setNumberOfTotalLaps(Integer numberOfTotalLaps) {
        this.numberOfTotalLaps = numberOfTotalLaps;
    }

    public Duration getTotalDuration() {
        return totalDuration;
    }

    public void setTotalDuration(Duration totalDuration) {
        this.totalDuration = totalDuration;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "ToplistResult{" + "chipId=" + chipId + ", numberOfTotalLaps=" + numberOfTotalLaps + ", totalDuration=" + totalDuration + ", name=" + name + '}';
    }

}
