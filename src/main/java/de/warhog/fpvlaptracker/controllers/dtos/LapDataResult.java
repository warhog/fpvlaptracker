package de.warhog.fpvlaptracker.controllers.dtos;

import de.warhog.fpvlaptracker.entities.Participant;
import de.warhog.fpvlaptracker.entities.racedata.LapTimeList;
import java.util.HashMap;
import java.util.Map;

public class LapDataResult {

    private Participant participant;
    private LapTimeList lapTimeList;
    private Map<Integer, Boolean> lapValidity = new HashMap<>();

    public Map<Integer, Boolean> getLapValidity() {
        return lapValidity;
    }

    public void setLapValidity(Map<Integer, Boolean> lapValidity) {
        this.lapValidity = lapValidity;
    }

    public Participant getParticipant() {
        return participant;
    }

    public void setParticipant(Participant participant) {
        this.participant = participant;
    }

    public LapTimeList getLapTimeList() {
        return lapTimeList;
    }

    public void setLapTimeList(LapTimeList lapTimeList) {
        this.lapTimeList = lapTimeList;
    }

    @Override
    public String toString() {
        return "LapDataResult{" + "participant=" + participant + ", lapTimeList=" + lapTimeList + '}';
    }

}
