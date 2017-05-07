package de.warhog.fpvlaptracker.communication.entities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StatusResult {

    private static final Logger LOG = LoggerFactory.getLogger(StatusResult.class);
    private String status;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "StatusResult{" + "status=" + status + '}';
    }
    
}
