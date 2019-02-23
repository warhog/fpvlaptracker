package de.warhog.fpvlaptracker.entities.racedata;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FixedTimeRaceParticipantData {

    private static final Logger LOG = LoggerFactory.getLogger(FixedTimeRaceParticipantData.class);

    public enum ParticipantState {
        WAITING_FOR_START,
        WAITING_FOR_FIRST_PASS,
        STARTED,
        LAST_LAP,
        FINISHED,
        INVALID
    };

    private LapTimeList lapTimeList = new LapTimeList();
    private ParticipantState state = ParticipantState.WAITING_FOR_START;

    public FixedTimeRaceParticipantData() {
    }

    public FixedTimeRaceParticipantData(LapTimeList lapTimeList, ParticipantState state) {
        this.lapTimeList = lapTimeList;
        this.state = state;
    }

    public LapTimeList getLapTimeList() {
        return lapTimeList;
    }

    public void setLapTimeList(LapTimeList lapTimeList) {
        this.lapTimeList = lapTimeList;
    }

    public ParticipantState getState() {
        return state;
    }

    public void setState(ParticipantState state) {
        this.state = state;
    }
    
    public boolean isStateWaitingForStart() {
        return state == ParticipantState.WAITING_FOR_START;
    }

    public boolean isStateWaitingForFirstPass() {
        return state == ParticipantState.WAITING_FOR_FIRST_PASS;
    }

    public boolean isStateStarted() {
        return state == ParticipantState.STARTED;
    }

    public boolean isStateLastLap() {
        return state == ParticipantState.LAST_LAP;
    }

    public boolean isStateFinished() {
        return state == ParticipantState.FINISHED;
    }

    public boolean isStateInvalid() {
        return state == ParticipantState.INVALID;
    }

}
