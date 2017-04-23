package de.warhog.fpvlaptracker.controllers.dtos;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StatusResult {

    private static final Logger LOG = LoggerFactory.getLogger(StatusResult.class);

    private String status;
    private String message;

    public StatusResult(String status) {
        this.status = status;
    }

    public StatusResult(String status, String message) {
        this.status = status;
        this.message = message;
    }
    
    public StatusResult(Status status) {
        this.status = status.name();
    }

    public StatusResult(Status status, String message) {
        this.status = status.name();
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "StatusResult{" + "status=" + status + ", message=" + message + '}';
    }
    
    public enum Status {
        OK,
        NOK
    }
    
}
