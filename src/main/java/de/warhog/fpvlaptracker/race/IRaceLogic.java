package de.warhog.fpvlaptracker.race;

import de.warhog.fpvlaptracker.entities.RaceState;
import de.warhog.fpvlaptracker.entities.racedata.LapTimeList;
import java.time.LocalDateTime;

public interface IRaceLogic {

    void init();

    void startRace();

    void stopRace();

    void addLap(Long chipId, Long duration, Integer rssi);

    Boolean isRunning();

    RaceState getState();

    void setState(RaceState state);

    LocalDateTime getStartTime();

    void setStartTime(LocalDateTime startTime);
    
    LapTimeList getLapData(Long chipId);
}
