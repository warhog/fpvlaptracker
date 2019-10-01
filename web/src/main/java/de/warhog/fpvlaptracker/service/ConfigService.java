package de.warhog.fpvlaptracker.service;

import de.warhog.fpvlaptracker.db.ConfigDbLayer;
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
    private ConfigDbLayer dbLayer;

    public void createOrUpdateKey(String key, String value) throws ServiceLayerException {
        try {
            dbLayer.createOrUpdateKey(key, value);
        } catch (DbLayerException ex) {
            throw new ServiceLayerException(ex);
        }
    }

    public String getValueForKey(String key) throws ServiceLayerException {
        return getValueForKey(key, null);
    }
    
    public String getValueForKey(String key, String defaultValue) throws ServiceLayerException {
        try {
            if (defaultValue != null && !dbLayer.hasConfigRecordForKey(key)) {
                return defaultValue;
            }
            ConfigRecord cr = dbLayer.getConfigRecordForKey(key);
            return cr.getConfigValue();
        } catch (DbLayerException ex) {
            throw new ServiceLayerException(ex);
        }
    }
    
    public Integer getIntegerValueForKey(String key) throws ServiceLayerException {
        return getIntegerValueForKey(key, null);
    }
    
    public Integer getIntegerValueForKey(String key, Integer defaultValue) throws ServiceLayerException {
        try {
            if (defaultValue != null && !dbLayer.hasConfigRecordForKey(key)) {
                return defaultValue;
            }
            ConfigRecord cr = dbLayer.getConfigRecordForKey(key);
            return Integer.parseInt(cr.getConfigValue());
        } catch (DbLayerException ex) {
            throw new ServiceLayerException(ex);
        } catch (NumberFormatException ex) {
            throw new ServiceLayerException("invalid number: " + ex.getMessage());
        }
    }
    
    public Integer getNumberOfLaps() throws ServiceLayerException {
        return getIntegerValueForKey("numberOfLaps", 10);
    }
    
    public void setNumberOfLaps(Integer numberOfLaps) throws ServiceLayerException {
        createOrUpdateKey("numberOfLaps", numberOfLaps.toString());
    }
    
    public Integer getPreparationDuration() throws ServiceLayerException {
        return getIntegerValueForKey("preparationTime", 10);
    }
    
    public void setPreparationTime(Integer preparationTime) throws ServiceLayerException {
        createOrUpdateKey("preparationTime", preparationTime.toString());
    }
    
    public Integer getStartInterval() throws ServiceLayerException {
        return getIntegerValueForKey("startInterval", 3);
    }
    
    public void setStartInterval(Integer startInterval) throws ServiceLayerException {
        createOrUpdateKey("startInterval", startInterval.toString());
    }
    
    public Integer getRaceDuration() throws ServiceLayerException {
        return getIntegerValueForKey("raceDuration", 120);
    }
    
    public void setRaceDuration(Integer raceDuration) throws ServiceLayerException {
        createOrUpdateKey("raceDuration", raceDuration.toString());
    }
    
    public Integer getOvertimeDuration() throws ServiceLayerException {
        return getIntegerValueForKey("overtimeDuration", 60);
    }
    
    public void setOvertimeDuration(Integer overtimeDuration) throws ServiceLayerException {
        createOrUpdateKey("overtimeDuration", overtimeDuration.toString());
    }
    
    public void setTimezone(String timezone) throws ServiceLayerException {
        createOrUpdateKey("timezone", timezone);
    }
    
    public String getTimezone() {
        String timezone = TimeZone.getTimeZone(ZoneId.systemDefault()).getID();
        try {
            timezone = getValueForKey("timezone");
        } catch (ServiceLayerException ex) {
            LOG.info("no timezone defined, fall back to " + timezone);
        }
        return timezone;
    }
    
    public void setAudioLanguage(String audioLanguage) throws ServiceLayerException {
        createOrUpdateKey("audioLanguage", audioLanguage);
    }
    
    public String getAudioLanguage() {
        String audioLanguage = "en-US";
        try {
            audioLanguage = getValueForKey("audioLanguage");
        } catch (ServiceLayerException ex) {
            LOG.info("no audio language defined, fall back to " + audioLanguage);
        }
        return audioLanguage;
    }
}
