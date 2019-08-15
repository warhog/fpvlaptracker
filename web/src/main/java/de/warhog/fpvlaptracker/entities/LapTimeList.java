package de.warhog.fpvlaptracker.entities;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LapTimeList {

    private static final Logger LOG = LoggerFactory.getLogger(LapTimeList.class);

    private final List<LapTimeListLap> laps;
    private Integer currentLap;
    private Integer lastRssi;

    public LapTimeList() {
        currentLap = 0;
        lastRssi = 0;
        laps = new ArrayList<>();
    }

    public void reset() {
        this.laps.clear();
        this.lastRssi = 0;
        this.currentLap = 0;
    }
    
    public LapTimeListLap getLap(Integer lap) {
        LapTimeListLap result = null;
        for (LapTimeListLap lapTimeListLap : laps) {
            if (Objects.equals(lapTimeListLap.getLap(), lap)) {
                result = lapTimeListLap;
            }
        }
        if (result == null) {
            throw new IllegalArgumentException("lap not found: " + lap);
        }
        return result;
    }
    
    public void invalidateLap(Integer lap, boolean invalid) {
        LapTimeListLap lapTimeListLap = getLap(lap);
        lapTimeListLap.setInvalid(invalid);
    }

    public boolean isLapValid(Integer lap) {
        LapTimeListLap lapTimeListLap = getLap(lap);
        return !lapTimeListLap.getInvalid();
    }

    public boolean isNoValidLapAvailable() {
        return numberOfInvalidLaps() == laps.size();
    }

    public Integer numberOfInvalidLaps() {
        Integer result = 0;
        for (LapTimeListLap lapTimeListLap : laps) {
            if (lapTimeListLap.getInvalid()) {
                result++;
            }
        }
        return result;
    }

    public void addLap(Long duration, Integer rssi, boolean ignoreFirstLap) {
        LOG.debug("addLap(" + duration + ", " + rssi + ", " + ignoreFirstLap + ")");
        if (ignoreFirstLap && currentLap == 0) {
            LOG.info("first lap, ignore");
            currentLap = 1;
        } else {
            if (!ignoreFirstLap && currentLap == 0) {
                currentLap = 1;
            }
            lastRssi = rssi;
            LOG.info("add lap with " + duration + " ms (rssi: " + rssi + ")");
            Duration lapDuration = Duration.of(duration, ChronoUnit.MILLIS);
            laps.add(new LapTimeListLap(currentLap, lapDuration, false, rssi));
            currentLap++;
        }
    }

    public void addLap(Long duration, Integer rssi) {
        addLap(duration, rssi, false);
    }

    public Duration getAverageLapDuration() {
        if (laps.isEmpty() || isNoValidLapAvailable()) {
            return Duration.ZERO;
        }
        Duration avg = getTotalDuration();
        avg = avg.dividedBy(laps.size() - numberOfInvalidLaps());
        return avg;
    }

    public Duration getTotalDuration() {
        if (laps.isEmpty() || isNoValidLapAvailable()) {
            return Duration.ZERO;
        }
        Duration total = Duration.ZERO;
        for (LapTimeListLap lapTimeListLap : laps) {
            if (lapTimeListLap.isValid()) {
                total = total.plus(lapTimeListLap.getDuration());
            }
        }
        return total;
    }

    public List<LapTimeListLap> getLaps() {
        return new ArrayList<>(laps);
    }

    public Integer getTotalLaps() {
        Integer totalLaps = 0;
        totalLaps = laps.stream().filter((lapTimeListLap) -> (lapTimeListLap.isValid())).map((_item) -> 1).reduce(totalLaps, Integer::sum);
        return totalLaps;
    }

    public Integer getCurrentLap() {
        return currentLap;
    }

    public Integer getLastRssi() {
        return lastRssi;
    }

    public Duration getFastestLapDuration() {
        if (laps.isEmpty() || isNoValidLapAvailable()) {
            return Duration.ZERO;
        }
        Duration fastest = Duration.of(1, ChronoUnit.HOURS);
        for (LapTimeListLap lapTimeListLap : laps) {
            if (lapTimeListLap.isValid()) {
                if (lapTimeListLap.getDuration().compareTo(fastest) < 0) {
                    fastest = lapTimeListLap.getDuration();
                }
            }
        }
        return fastest;
    }

    public Integer getFastestLap() {
        if (laps.isEmpty() || isNoValidLapAvailable()) {
            return 1;
        }
        Integer fastestLap = 1;
        Duration fastest = Duration.of(1, ChronoUnit.HOURS);

        for (LapTimeListLap lapTimeListLap : laps) {
            if (lapTimeListLap.isValid()) {
                if (lapTimeListLap.getDuration().compareTo(fastest) < 0) {
                    fastest = lapTimeListLap.getDuration();
                    fastestLap = lapTimeListLap.getLap();
                }
            }
        }
        return fastestLap;
    }

    @Override
    public String toString() {
        return "LapTimeList{" + "laps=" + laps + ", currentLap=" + currentLap + ", lastRssi=" + lastRssi + '}';
    }

}
