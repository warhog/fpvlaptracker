package de.warhog.fpvlaptracker.configuration;

import java.util.ArrayList;
import org.springframework.stereotype.Component;

@Component
public class NodeVersionConfig {
    
    ArrayList<String> supportedNodeVersions = new ArrayList<>();

    public NodeVersionConfig() {
        // 0.0.0 is dev version
        supportedNodeVersions.add("FLT32-R0.0.0");
        supportedNodeVersions.add("FLT32-R3.2.0");
        supportedNodeVersions.add("FLT32-R3.3.0");
    }
    
    public boolean isSupportedNodeVersion(final String version) {
        return supportedNodeVersions.contains(version);
    }
    
    public ArrayList<String> getSupportedNodeVersions() {
        return new ArrayList<>(supportedNodeVersions);
    }
    
}
