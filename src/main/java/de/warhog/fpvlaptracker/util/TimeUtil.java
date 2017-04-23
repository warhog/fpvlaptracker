package de.warhog.fpvlaptracker.util;

import de.warhog.fpvlaptracker.service.ConfigService;
import de.warhog.fpvlaptracker.service.ServiceLayerException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
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
        try {
            ZoneOffset zoneOffset = ZoneId.of(configService.getTimezone()).getRules().getOffset(LocalDateTime.now());
            LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(unixTimestamp), zoneOffset);
            return localDateTime;
        } catch (ServiceLayerException ex) {
            LOG.error(ex.getMessage(), ex);
            throw new RuntimeException("cannot convert unix timestamp to localdatetime");
        }
    }

    public Integer localDateTimeToUnix(LocalDateTime localDateTime) {
        try {
            ZoneOffset zoneOffset = ZoneId.of(configService.getTimezone()).getRules().getOffset(localDateTime);
            return Math.toIntExact(localDateTime.toEpochSecond(zoneOffset));
        } catch (ServiceLayerException ex) {
            LOG.error(ex.getMessage(), ex);
            throw new RuntimeException("cannot convert unix timestamp to localdatetime");
        }
    }

}
