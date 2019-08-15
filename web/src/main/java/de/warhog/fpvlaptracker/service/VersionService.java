package de.warhog.fpvlaptracker.service;

import java.io.IOException;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class VersionService {
    
    private static final Logger LOG = LoggerFactory.getLogger(VersionService.class);
    
    private Properties versionProperties = new Properties();

    public VersionService() {
        try {
            versionProperties.load(this.getClass().getResourceAsStream("/version.properties"));
        } catch (IOException ex) {
            LOG.error("cannot open version properties: " + ex.getMessage(), ex);
        }
    }
    
    public String getVersion() {
        try {
            return versionProperties.getProperty("version", "-");
        } catch (NullPointerException ex) {
            LOG.error("cannot get version from properties: " + ex.getMessage(), ex);
            return "-";
        }
    }
    
}
