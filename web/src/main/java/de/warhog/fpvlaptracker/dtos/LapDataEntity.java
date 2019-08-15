package de.warhog.fpvlaptracker.dtos;

import de.warhog.fpvlaptracker.entities.Pilot;
import de.warhog.fpvlaptracker.entities.LapTimeList;

public class LapDataEntity {

    private Pilot pilot;
    private LapTimeList lapTimeList;

    public LapDataEntity() {
        this.lapTimeList = new LapTimeList();
    }

    public Pilot getPilot() {
        return pilot;
    }

    public void setPilot(Pilot pilot) {
        this.pilot = pilot;
    }

    public LapTimeList getLapTimeList() {
        return lapTimeList;
    }

    public void setLapTimeList(LapTimeList lapTimeList) {
        this.lapTimeList = lapTimeList;
    }

    @Override
    public String toString() {
        return "LapDataResult{" + "pilot=" + pilot + ", lapTimeList=" + lapTimeList + '}';
    }

}
