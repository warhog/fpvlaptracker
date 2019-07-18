package de.warhog.fpvlaptracker.entities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FixedTimeRaceParticipantData {

    private static final Logger LOG = LoggerFactory.getLogger(FixedTimeRaceParticipantData.class);

    public enum ParticipantState {
        WAITING_FOR_START("waiting for start"),
        WAITING_FOR_FIRST_PASS("waiting for first pass"),
        STARTED("started"),
        LAST_LAP("last lap"),
        FINISHED("finished"),
        INVALID("invalid");

        private final String text;
        private ParticipantState(String text) {
            this.text = text;
        }
        public String getText() {
            return text;
        }
    };

    private ParticipantState state = ParticipantState.WAITING_FOR_START;

    public FixedTimeRaceParticipantData() {
    }

    public FixedTimeRaceParticipantData(ParticipantState state) {
        this.state = state;
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
