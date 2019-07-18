package de.warhog.fpvlaptracker.entities;

import java.time.Duration;

public class ParticipantExtraData {

    private String state = "";
    private Duration duration = Duration.ZERO;

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    @Override
    public String toString() {
        return "ParticipantExtraData{" + "state=" + state + ", duration=" + duration + '}';
    }

}
