package de.warhog.fpvlaptracker.controllers.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import de.warhog.fpvlaptracker.dtos.ChartResultLap;
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
        Map<String, Duration> times = value.getTimes();
        for (Map.Entry<String, Duration> entry : times.entrySet()) {
            gen.writeNumberField(entry.getKey(), entry.getValue().toMillis() / 1000.0);
        }
        gen.writeEndObject();
    }

}
