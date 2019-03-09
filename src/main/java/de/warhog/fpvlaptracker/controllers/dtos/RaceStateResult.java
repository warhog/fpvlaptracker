package de.warhog.fpvlaptracker.controllers.dtos;

import de.warhog.fpvlaptracker.entities.RaceState;
import de.warhog.fpvlaptracker.race.RaceType;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RaceStateResult {

    private static final Logger LOG = LoggerFactory.getLogger(RaceStateResult.class);

    private RaceState state;
    private List<LapDataResult> lapData;
    private LocalDateTime startTime;
    private RaceType raceType;
    private Map<String, Long> toplist = new HashMap<>();
    private Map<String, String> typeSpecific = new HashMap<>();

    public Map<String, String> getTypeSpecific() {
        return typeSpecific;
    }
    
    public void addTypeSpecific(final String key, final String value) {
        typeSpecific.put(key, value);
    }
    
    public Map<String, Long> getToplist() {
        return toplist;
    }

    public void setToplist(Map<String, Long> toplist) {
        this.toplist = toplist;
    }

    public RaceState getState() {
        return state;
    }

    public void setState(RaceState state) {
        this.state = state;
    }

    public List<LapDataResult> getLapData() {
        return lapData;
    }

    public void setLapData(List<LapDataResult> lapData) {
        this.lapData = lapData;
    }
    
    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public RaceType getRaceType() {
        return raceType;
    }

    public void setRaceType(RaceType raceType) {
        this.raceType = raceType;
    }
    
}
