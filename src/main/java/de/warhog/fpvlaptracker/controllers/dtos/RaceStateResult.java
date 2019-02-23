package de.warhog.fpvlaptracker.controllers.dtos;

import de.warhog.fpvlaptracker.entities.Participant;
import de.warhog.fpvlaptracker.entities.RaceState;
import de.warhog.fpvlaptracker.entities.racedata.LapTimeList;
import de.warhog.fpvlaptracker.race.RaceType;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RaceStateResult {

    private static final Logger LOG = LoggerFactory.getLogger(RaceStateResult.class);

    private RaceState state;
    private Map<Participant, LapTimeList> raceData = new HashMap<>();
    private LocalDateTime startTime;
    private Integer maxLaps;
    private RaceType raceType;

    public RaceState getState() {
        return state;
    }

    public void setState(RaceState state) {
        this.state = state;
    }

    public Map<Participant, LapTimeList> getRaceData() {
        return raceData;
    }

    public void setRaceData(Map<Participant, LapTimeList> raceData) {
        this.raceData = raceData;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public Integer getMaxLaps() {
        return maxLaps;
    }

    public void setMaxLaps(Integer maxLaps) {
        this.maxLaps = maxLaps;
    }

    public RaceType getRaceType() {
        return raceType;
    }

    public void setRaceType(RaceType raceType) {
        this.raceType = raceType;
    }
    
}
