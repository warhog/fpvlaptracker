package de.warhog.fpvlaptracker.controllers.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import de.warhog.fpvlaptracker.dtos.ChartResult;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChartResultSerializer extends StdSerializer<ChartResult> {

    private static final Logger LOG = LoggerFactory.getLogger(ChartResultSerializer.class);

    public ChartResultSerializer() {
        this(null);
    }

    public ChartResultSerializer(Class<ChartResult> t) {
        super(t);
    }

    @Override
    public void serialize(ChartResult value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();
        
        gen.writeArrayFieldStart("pilots");
        for (String name : value.getPilots()) {
            gen.writeString(name);
        }
        gen.writeEndArray();
        
        gen.writeObjectField("lapTimes", value.getLapTimes());
        
        gen.writeEndObject();
    }

}
