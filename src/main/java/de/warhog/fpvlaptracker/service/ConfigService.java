package de.warhog.fpvlaptracker.service;

import de.warhog.fpvlaptracker.db.ConfigLayer;
import de.warhog.fpvlaptracker.db.DbLayerException;
import de.warhog.fpvlaptracker.jooq.tables.records.ConfigRecord;
import java.time.ZoneId;
import java.util.TimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ConfigService {

    private static final Logger LOG = LoggerFactory.getLogger(ConfigService.class);

    @Autowired
    private ConfigLayer dbLayer;

    public void createOrUpdateKey(String key, String value) throws ServiceLayerException {
        try {
            dbLayer.createOrUpdateKey(key, value);
        } catch (DbLayerException ex) {
            throw new ServiceLayerException(ex);
        }
    }

    public String getValueForKey(String key) throws ServiceLayerException {
        try {
            ConfigRecord cr = dbLayer.getConfigRecordForKey(key);
            return cr.getConfigValue();
        } catch (DbLayerException ex) {
            throw new ServiceLayerException(ex);
        }
    }
    
    public Integer getIntegerValueForKey(String key) throws ServiceLayerException {
        try {
            ConfigRecord cr = dbLayer.getConfigRecordForKey(key);
            return Integer.parseInt(cr.getConfigValue());
        } catch (DbLayerException ex) {
            throw new ServiceLayerException(ex);
        } catch (NumberFormatException ex) {
            throw new ServiceLayerException("invalid number: " + ex.getMessage());
        }
    }
    
    public Integer getNumberOfLaps() throws ServiceLayerException {
        return getIntegerValueForKey("numberOfLaps");
    }
    
    public void setNumberOfLaps(Integer numberOfLaps) throws ServiceLayerException {
        createOrUpdateKey("numberOfLaps", numberOfLaps.toString());
    }
    
    public void setTimezone(String timezone) throws ServiceLayerException {
        createOrUpdateKey("timezone", timezone);
    }
    
    public String getTimezone() throws ServiceLayerException {
        try {
            String timezone = getValueForKey("timezone");
            return timezone;
        } catch (ServiceLayerException ex) {
            String timeZone = TimeZone.getTimeZone(ZoneId.systemDefault()).getID();
            LOG.info("no timezone defined, fall back to " + timeZone);
            return timeZone;
        }
    }
}
