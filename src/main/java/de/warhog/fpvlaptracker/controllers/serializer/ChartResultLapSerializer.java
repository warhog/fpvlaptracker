package de.warhog.fpvlaptracker.controllers.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import de.warhog.fpvlaptracker.controllers.dtos.ChartResultLap;
import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChartResultLapSerializer extends StdSerializer<ChartResultLap> {

    private static final Logger LOG = LoggerFactory.getLogger(ChartResultLapSerializer.class);

    public ChartResultLapSerializer() {
        this(null);
    }

    public ChartResultLapSerializer(Class<ChartResultLap> t) {
        super(t);
    }

    @Override
    public void serialize(ChartResultLap value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("lap", value.getLap());
        Map<Integer, Duration> times = value.getTimes();
        for (Map.Entry<Integer, Duration> entry : times.entrySet()) {
            gen.writeNumberField(entry.getKey().toString(), entry.getValue().toMillis());
        }
        gen.writeEndObject();
    }

}
