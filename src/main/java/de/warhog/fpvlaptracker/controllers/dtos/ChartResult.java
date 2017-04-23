package de.warhog.fpvlaptracker.controllers.dtos;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import de.warhog.fpvlaptracker.controllers.serializer.ChartResultSerializer;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@JsonSerialize(using = ChartResultSerializer.class)
public class ChartResult {

    private static final Logger LOG = LoggerFactory.getLogger(ChartResult.class);

    private final Map<Integer, String> participants = new HashMap<>();
    private final List<ChartResultLap> lapTimes = new ArrayList<>();

    public Map<Integer, String> getParticipants() {
        return participants;
    }

    public List<ChartResultLap> getLapTimes() {
        return lapTimes;
    }

    public void addParticipant(Integer chipId, String name) {
        participants.put(chipId, name);
    }

    public void addLapTimes(Integer lap, Integer chipId, Duration duration) {
        for (ChartResultLap chartResultLap : lapTimes) {
            if (Objects.equals(chartResultLap.getLap(), lap)) {
                chartResultLap.addLapTime(chipId, duration);
                return;
            }
        }
        ChartResultLap chartResultLap = new ChartResultLap();
        chartResultLap.setLap(lap);
        chartResultLap.addLapTime(chipId, duration);
        lapTimes.add(chartResultLap);
    }
}
