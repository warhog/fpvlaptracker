package de.warhog.fpvlaptracker.configuration;

import java.util.ArrayList;
import org.springframework.stereotype.Component;

@Component
public class NodeVersionConfig {
    
    ArrayList<String> supportedNodeVersions = new ArrayList<>();

    public NodeVersionConfig() {
        supportedNodeVersions.add("FLT32-R1.6");
        supportedNodeVersions.add("FLT32-R3.0");
        supportedNodeVersions.add("FLT32-R3.1");
    }
    
    public boolean isSupportedNodeVersion(final String version) {
        return supportedNodeVersions.contains(version);
    }
    
    public ArrayList<String> getSupportedNodeVersions() {
        return new ArrayList<>(supportedNodeVersions);
    }
    
}
