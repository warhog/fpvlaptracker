package de.warhog.fpvlaptracker.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ApplicationConfig {

    private static final Logger LOG = LoggerFactory.getLogger(ApplicationConfig.class);
    
    @Value("${audio.playLocal}")
    private Boolean audioPlayLocal;

    @Value("${shutdown.machine}")
    private Boolean shutdownMachine;
    
    @Value("${admin.password}")
    private String adminPassword;

    public Boolean isAudioPlayLocal() {
        return audioPlayLocal;
    }

    public Boolean isShutdownMachine() {
        return shutdownMachine;
    }

    public String getAdminPassword() {
        return adminPassword;
    }
    
}
