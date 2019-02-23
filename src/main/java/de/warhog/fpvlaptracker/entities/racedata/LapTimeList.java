package de.warhog.fpvlaptracker.entities.racedata;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LapTimeList {

    private static final Logger LOG = LoggerFactory.getLogger(LapTimeList.class);

    private final Map<Integer, Duration> laps;

    private Integer currentLap;
    private Integer lastRssi;

    public LapTimeList() {
        currentLap = 1;
        lastRssi = 0;
        laps = new HashMap<>();
    }

    public void addLap(Long duration, Integer rssi) {
        lastRssi = rssi;
        LOG.info("add lap with " + duration + " ms (rssi: " + rssi + ")");
        Duration lap = Duration.of(duration, ChronoUnit.MILLIS);
        laps.put(currentLap, lap);
        currentLap++;
    }

    public Duration getAverageLapDuration() {
        if (laps.isEmpty()) {
            return Duration.ZERO;
        }
        Duration avg = getTotalDuration();
        avg = avg.dividedBy(laps.size());
        return avg;
    }

    public Duration getTotalDuration() {
        if (laps.isEmpty()) {
            return Duration.ZERO;
        }
        Duration total = Duration.ZERO;
        for (Map.Entry<Integer, Duration> entry : laps.entrySet()) {
            total = total.plus(entry.getValue());
        }
        return total;
    }

    public Map<Integer, Duration> getLaps() {
        return new HashMap<>(laps);
    }

    public Integer getCurrentLap() {
        return currentLap;
    }

    public Integer getLastRssi() {
        return lastRssi;
    }

    public Duration getFastestLapDuration() {
        if (laps.isEmpty()) {
            return Duration.ZERO;
        }
        Duration fastest = Duration.of(1, ChronoUnit.HOURS);
        for (Map.Entry<Integer, Duration> entry : laps.entrySet()) {
            if (entry.getValue().compareTo(fastest) < 0) {
                fastest = entry.getValue();
            }
        }
        return fastest;
    }

    public Integer getFastestLap() {
        if (laps.isEmpty()) {
            return 1;
        }
        Integer fastestLap = 1;
        Duration fastest = Duration.of(1, ChronoUnit.HOURS);
        for (Map.Entry<Integer, Duration> entry : laps.entrySet()) {
            if (entry.getValue().compareTo(fastest) < 0) {
                fastest = entry.getValue();
                fastestLap = entry.getKey();
            }
        }
        return fastestLap;
    }

}
