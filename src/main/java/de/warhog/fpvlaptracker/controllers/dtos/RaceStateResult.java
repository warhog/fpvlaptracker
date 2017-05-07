package de.warhog.fpvlaptracker.controllers.dtos;

import de.warhog.fpvlaptracker.race.entities.Participant;
import de.warhog.fpvlaptracker.race.entities.ParticipantRaceData;
import de.warhog.fpvlaptracker.race.entities.RaceState;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RaceStateResult {

    private static final Logger LOG = LoggerFactory.getLogger(RaceStateResult.class);

    private RaceState state;
    private Map<Participant, ParticipantRaceData> raceData = new HashMap<>();
    private LocalDateTime startTime;
    private Integer maxLaps;

    public RaceState getState() {
        return state;
    }

    public void setState(RaceState state) {
        this.state = state;
    }

    public Map<Participant, ParticipantRaceData> getRaceData() {
        return raceData;
    }

    public void setRaceData(Map<Participant, ParticipantRaceData> raceData) {
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

}
