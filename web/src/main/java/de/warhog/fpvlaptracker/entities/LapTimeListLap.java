package de.warhog.fpvlaptracker.entities;

import java.time.Duration;
import java.util.Objects;

public class LapTimeListLap {

    private Integer lap = 0;
    private Duration duration = Duration.ZERO;
    private Boolean invalid = false;
    private Integer rssi = 0;

    public LapTimeListLap() {
    }

    public LapTimeListLap(Integer lap, Duration duration, Boolean invalid, Integer rssi) {
        this.lap = lap;
        this.duration = duration;
        this.invalid = invalid;
        this.rssi = rssi;
    }

    public Integer getLap() {
        return lap;
    }

    public void setLap(Integer lap) {
        this.lap = lap;
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public Boolean getInvalid() {
        return invalid;
    }
    
    public Boolean isValid() {
        return !invalid;
    }

    public void setInvalid(Boolean invalid) {
        this.invalid = invalid;
    }

    public Integer getRssi() {
        return rssi;
    }

    public void setRssi(Integer rssi) {
        this.rssi = rssi;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + Objects.hashCode(this.lap);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final LapTimeListLap other = (LapTimeListLap) obj;
        if (!Objects.equals(this.lap, other.lap)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "LapTimeListLap{" + "lap=" + lap + ", duration=" + duration + ", invalid=" + invalid + ", rssi=" + rssi + '}';
    }

}
