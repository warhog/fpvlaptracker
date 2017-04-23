package de.warhog.fpvlaptracker.race.entities;

import de.warhog.fpvlaptracker.jooq.tables.records.LapsRecord;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParticipantRaceData {

    private static final Logger LOG = LoggerFactory.getLogger(ParticipantRaceData.class);

    private Integer currentLap;
    private Map<Integer, Duration> laps = new HashMap<>();
    private Integer lastRssi;

    public void fillLapDataFromDatabase(List<LapsRecord> laps) {
        this.laps.clear();
        for (LapsRecord lap : laps) {
            if (lap.getLap() == 0) {
                // is warmup lap, skip
                LOG.debug("skipping warmup lap (lap 0)");
            } else {
                Duration duration = Duration.ZERO;
                duration = duration.plusMillis(lap.getDuration());
                this.laps.put(lap.getLap(), duration);
            }
        }
    }

    public ParticipantRaceData() {
        currentLap = 0;
        laps = new HashMap<>();
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

    public Boolean hasEnded(Integer numberOfLaps) {
        return currentLap > numberOfLaps;
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
    
    public void addLap(Long duration, Integer rssi) {
        lastRssi = rssi;
        if (currentLap == 0) {
            // first lap
            LOG.info("first lap, ignore");
            currentLap = 1;
        } else {
            LOG.info("add lap with " + duration + " ms");
            Duration lap = Duration.of(duration, ChronoUnit.MILLIS);
            laps.put(currentLap, lap);
            currentLap++;
        }
    }

    @Override
    public String toString() {
        return "ParticipantRaceStats{" + "currentLap=" + currentLap + ", laps=" + laps + '}';
    }

}
