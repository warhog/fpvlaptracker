package de.warhog.fpvlaptracker.util;

import de.warhog.fpvlaptracker.service.ConfigService;
import de.warhog.fpvlaptracker.service.ServiceLayerException;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TimeUtil {

    private static final Logger LOG = LoggerFactory.getLogger(TimeUtil.class);

    @Autowired
    private ConfigService configService;

    public LocalDateTime unixToLocalDateTime(Integer unixTimestamp) {
        ZoneOffset zoneOffset = ZoneId.of(configService.getTimezone()).getRules().getOffset(LocalDateTime.now());
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(unixTimestamp), zoneOffset);
        return localDateTime;
    }

    public Integer localDateTimeToUnix(LocalDateTime localDateTime) {
        ZoneOffset zoneOffset = ZoneId.of(configService.getTimezone()).getRules().getOffset(localDateTime);
        return Math.toIntExact(localDateTime.toEpochSecond(zoneOffset));
    }

    public String getUnixTimestampInIsoFormat(Integer startTime) {
        ZoneOffset zoneOffset = ZoneId.of(configService.getTimezone()).getRules().getOffset(LocalDateTime.now());
        LocalDateTime date = LocalDateTime.ofInstant(Instant.ofEpochSecond(startTime), zoneOffset);
        return date.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

}
