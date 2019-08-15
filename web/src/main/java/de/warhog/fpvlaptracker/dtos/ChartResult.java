package de.warhog.fpvlaptracker.dtos;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import de.warhog.fpvlaptracker.controllers.serializer.ChartResultSerializer;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@JsonSerialize(using = ChartResultSerializer.class)
public class ChartResult {

    private static final Logger LOG = LoggerFactory.getLogger(ChartResult.class);

    private final List<String> pilots = new ArrayList<>();
    private final List<ChartResultLap> lapTimes = new ArrayList<>();

    public List<String> getPilots() {
        return pilots;
    }

    public List<ChartResultLap> getLapTimes() {
        return lapTimes;
    }

    public void addPilot(String name) {
        pilots.add(name);
    }

    public void addLapTimes(String name, Integer lap, Duration duration) {
        for (ChartResultLap chartResultLap : lapTimes) {
            if (Objects.equals(chartResultLap.getLap(), lap)) {
                chartResultLap.addLapTime(name, duration);
                return;
            }
        }
        ChartResultLap chartResultLap = new ChartResultLap();
        chartResultLap.setLap(lap);
        chartResultLap.addLapTime(name, duration);
        lapTimes.add(chartResultLap);
    }
}
