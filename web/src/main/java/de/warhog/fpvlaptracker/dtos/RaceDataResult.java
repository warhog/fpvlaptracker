package de.warhog.fpvlaptracker.dtos;

import de.warhog.fpvlaptracker.entities.Pilot;
import de.warhog.fpvlaptracker.util.RaceState;
import de.warhog.fpvlaptracker.util.RaceType;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RaceDataResult {

    private static final Logger LOG = LoggerFactory.getLogger(RaceDataResult.class);

    private RaceState state;
    private LocalDateTime startTime;
    private RaceType raceType;
    private List<ToplistEntry> toplist = new ArrayList<>();
    private Map<String, String> typeSpecific = new HashMap<>();
    private List<Pilot> pilots = new ArrayList<>();
    private Map<String, Long> pilotDurations = new HashMap<>();

    public Map<String, String> getTypeSpecific() {
        return typeSpecific;
    }

    public void addTypeSpecific(final String key, final String value) {
        typeSpecific.put(key, value);
    }

    public List<ToplistEntry> getToplist() {
        return toplist;
    }

    public void setToplist(List<ToplistEntry> toplist) {
        this.toplist = toplist;
    }

    public RaceState getState() {
        return state;
    }

    public void setState(RaceState state) {
        this.state = state;
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

    public List<Pilot> getPilots() {
        return pilots;
    }

    public void setPilots(List<Pilot> pilots) {
        this.pilots = pilots;
    }

    public Map<String, Long> getPilotDurations() {
        return pilotDurations;
    }

    public void setPilotDurations(Map<String, Long> pilotDurations) {
        this.pilotDurations = pilotDurations;
    }
    
    public void setTypeSpecific(Map<String, String> raceTypeSpecificData) {
        this.typeSpecific = raceTypeSpecificData;
    }

    @Override
    public String toString() {
        return "RaceDataResult{" + "state=" + state + ", startTime=" + startTime + ", raceType=" + raceType + ", toplist=" + toplist + ", typeSpecific=" + typeSpecific + ", pilots=" + pilots + ", pilotDurations=" + pilotDurations + '}';
    }
    
}
