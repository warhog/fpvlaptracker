package de.warhog.fpvlaptracker.db;

import de.warhog.fpvlaptracker.jooq.Tables;
import de.warhog.fpvlaptracker.jooq.tables.records.ConfigRecord;
import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ConfigLayer {

    /* db layout
    CREATE TABLE PUBLIC.CONFIG (
        CONFIG_KEY VARCHAR(255) NOT NULL,
        CONFIG_VALUE TEXT,
        PRIMARY KEY (CONFIG_KEY),
        UNIQUE KEY configkey_unique (CONFIG_KEY)
    );
    */
    private static final Logger LOG = LoggerFactory.getLogger(ConfigLayer.class);

    @Autowired
    private Db db;
    
    public void createOrUpdateKey(String key, String value) throws DbLayerException {
        DSLContext dslContext = db.connectDatabase();
        ConfigRecord rec = dslContext.selectFrom(Tables.CONFIG).where(Tables.CONFIG.CONFIG_KEY.equal(key)).limit(1).fetchOne();
        if (rec == null) {
            ConfigRecord configRecord = dslContext.newRecord(Tables.CONFIG);
            configRecord.setConfigKey(key);
            configRecord.setConfigValue(value);
            configRecord.store();
        } else {
            rec.setConfigValue(value);
            rec.store();
        }
        db.closeDatabase();
    }

    public ConfigRecord getConfigRecordForKey(String key) throws DbLayerException {
        DSLContext dslContext = db.connectDatabase();
        ConfigRecord rec = dslContext.selectFrom(Tables.CONFIG).where(Tables.CONFIG.CONFIG_KEY.equal(key)).limit(1).fetchOne();
        if (rec != null) {
            db.closeDatabase();
            return rec;
        }
        db.closeDatabase();
        throw new DbLayerException("cannot find data for key " + key);
    }

    public boolean hasConfigRecordForKey(String key) throws DbLayerException {
        DSLContext dslContext = db.connectDatabase();
        ConfigRecord rec = dslContext.selectFrom(Tables.CONFIG).where(Tables.CONFIG.CONFIG_KEY.equal(key)).limit(1).fetchOne();
//        db.closeDatabase();
        return rec != null;
    }

}
