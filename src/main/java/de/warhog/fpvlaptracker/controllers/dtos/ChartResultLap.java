package de.warhog.fpvlaptracker.controllers.dtos;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import de.warhog.fpvlaptracker.controllers.serializer.ChartResultLapSerializer;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@JsonSerialize(using = ChartResultLapSerializer.class)
public class ChartResultLap {

    private static final Logger LOG = LoggerFactory.getLogger(ChartResultLap.class);

    private Integer lap;
    private final Map<Long, Duration> times = new HashMap<>();

    public Integer getLap() {
        return lap;
    }

    public void setLap(Integer lap) {
        this.lap = lap;
    }

    public Map<Long, Duration> getTimes() {
        return new HashMap<>(times);
    }

    public void addLapTime(Long chipId, Duration duration) {
        times.put(chipId, duration);
    }

}
