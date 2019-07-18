package de.warhog.fpvlaptracker.service;

import de.warhog.fpvlaptracker.db.DbLayerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServiceLayerException extends Exception {

    private static final Logger LOG = LoggerFactory.getLogger(ServiceLayerException.class);

    public ServiceLayerException(String message) {
        super(message);
    }

    public ServiceLayerException(DbLayerException ex) {
        super(ex);
    }

}
