package de.warhog.fpvlaptracker.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DbLayerException extends Exception {

    private static final Logger LOG = LoggerFactory.getLogger(DbLayerException.class);

    public DbLayerException(String message) {
        super(message);
    }

}
