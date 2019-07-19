package de.warhog.fpvlaptracker.race;

import de.warhog.fpvlaptracker.entities.ParticipantExtraData;
import de.warhog.fpvlaptracker.entities.RaceState;
import de.warhog.fpvlaptracker.entities.ToplistEntry;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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

    List<ToplistEntry> getToplist();

    Map<Long, ParticipantExtraData> getParticipantExtraData();

}