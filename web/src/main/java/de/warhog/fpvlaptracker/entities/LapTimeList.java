package de.warhog.fpvlaptracker.entities;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LapTimeList {

    private static final Logger LOG = LoggerFactory.getLogger(LapTimeList.class);

    private final Map<Integer, Duration> laps;
    private final Map<Integer, Boolean> lapValidity;

    private Integer currentLap;
    private Integer lastRssi;

    public LapTimeList() {
        currentLap = 0;
        lastRssi = 0;
        laps = new HashMap<>();
        lapValidity = new HashMap<>();
    }

    public void invalidateLap(Integer lap, boolean validity) {
        lapValidity.put(lap, validity);
    }

    public boolean isLapValid(Integer lap) {
        if (lapValidity.containsKey(lap)) {
            return !lapValidity.get(lap);
        }
        return true;
    }

    public Map<Integer, Boolean> getInvalidLaps() {
        return new HashMap<>(lapValidity);
    }

    public boolean noValidLapAvailable() {
        return numberOfInvalidLaps() == laps.size();
    }

    public Integer numberOfInvalidLaps() {
        // filter only laps that are invalid
        Map<Integer, Boolean> collect = lapValidity.entrySet().stream()
                .filter(x -> x.getValue() == true)
                .collect(Collectors.toMap(x -> x.getKey(), x -> x.getValue()));
        return collect.size();
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
            Duration lap = Duration.of(duration, ChronoUnit.MILLIS);
            laps.put(currentLap, lap);
            currentLap++;
        }
    }

    public void addLap(Long duration, Integer rssi) {
        addLap(duration, rssi, false);
    }

    public Duration getAverageLapDuration() {
        if (laps.isEmpty() || noValidLapAvailable()) {
            return Duration.ZERO;
        }
        Duration avg = getTotalDuration();
        avg = avg.dividedBy(laps.size() - numberOfInvalidLaps());
        return avg;
    }

    public Duration getTotalDuration() {
        if (laps.isEmpty() || noValidLapAvailable()) {
            return Duration.ZERO;
        }
        Duration total = Duration.ZERO;
        for (Map.Entry<Integer, Duration> entry : laps.entrySet()) {
            if (isLapValid(entry.getKey())) {
                total = total.plus(entry.getValue());
            }
        }
        return total;
    }

    public Map<Integer, Duration> getLapsFilterInvalid() {
        Map<Integer, Duration> filtered = laps.entrySet().stream()
                .filter(x -> isLapValid(x.getKey()))
                .collect(Collectors.toMap(x -> x.getKey(), x -> x.getValue()));
        return filtered;
    }

    public Map<Integer, Duration> getLaps() {
        return new HashMap<>(laps);
    }

    public Integer getTotalLaps() {
        Map<Integer, Duration> filtered = laps.entrySet().stream()
                .filter(x -> isLapValid(x.getKey()))
                .collect(Collectors.toMap(x -> x.getKey(), x -> x.getValue()));
        return filtered.size();
    }

    public Integer getCurrentLap() {
        return currentLap;
    }

    public Integer getLastRssi() {
        return lastRssi;
    }

    public Duration getFastestLapDuration() {
        if (laps.isEmpty() || noValidLapAvailable()) {
            return Duration.ZERO;
        }
        Duration fastest = Duration.of(1, ChronoUnit.HOURS);
        for (Map.Entry<Integer, Duration> entry : laps.entrySet()) {
            if (isLapValid(entry.getKey())) {
                if (entry.getValue().compareTo(fastest) < 0) {
                    fastest = entry.getValue();
                }
            }
        }
        return fastest;
    }

    public Integer getFastestLap() {
        if (laps.isEmpty() || noValidLapAvailable()) {
            return 1;
        }
        Integer fastestLap = 1;
        Duration fastest = Duration.of(1, ChronoUnit.HOURS);
        for (Map.Entry<Integer, Duration> entry : laps.entrySet()) {
            if (isLapValid(entry.getKey())) {
                if (entry.getValue().compareTo(fastest) < 0) {
                    fastest = entry.getValue();
                    fastestLap = entry.getKey();
                }
            }
        }
        return fastestLap;
    }

    @Override
    public String toString() {
        return "LapTimeList{" + "laps=" + laps + ", lapValidity=" + lapValidity + ", currentLap=" + currentLap + ", lastRssi=" + lastRssi + '}';
    }

}
