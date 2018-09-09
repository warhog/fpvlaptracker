package de.warhog.fpvlaptracker.db;

import de.warhog.fpvlaptracker.race.entities.RaceState;
import de.warhog.fpvlaptracker.jooq.Tables;
import de.warhog.fpvlaptracker.jooq.tables.records.LapsRecord;
import de.warhog.fpvlaptracker.jooq.tables.records.RacesRecord;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.jooq.DSLContext;
import org.jooq.Record4;
import org.jooq.Result;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RaceLayer {

    private static final Logger LOG = LoggerFactory.getLogger(RaceLayer.class);

    /*
    CREATE TABLE PUBLIC.RACES (
        ID INT NOT NULL auto_increment,
        STARTTIME INT,
        LAPS INT NOT NULL,
        STATE VARCHAR(255) NOT NULL,
        PRIMARY KEY (ID),
        UNIQUE KEY raceid_unique (ID)
    )
    
    CREATE TABLE PUBLIC.LAPS (
        ID INT NOT NULL auto_increment,
        RACEID INT NOT NULL,
        CHIPID INT NOT NULL,
        LAP INT NOT NULL,
        DURATION INT NOT NULL,
        PRIMARY KEY (ID),
        UNIQUE KEY lapid_unique (ID)
    )
     */
    @Autowired
    private Db db;

    public Integer createRace(Integer laps, RaceState state) throws DbLayerException {
        DSLContext dslContext = db.connectDatabase();
        RacesRecord raceRecord = dslContext.newRecord(Tables.RACES);
        raceRecord.setLaps(laps);
        raceRecord.setState(state.name());
        raceRecord.store();
        db.closeDatabase();
        return raceRecord.getId();
    }

    public RacesRecord getRaceRecordForId(Integer id) throws DbLayerException {
        DSLContext dslContext = db.connectDatabase();
        RacesRecord rec = dslContext.selectFrom(Tables.RACES).where(Tables.RACES.ID.equal(id)).limit(1).fetchOne();
        if (rec != null) {
            db.closeDatabase();
            return rec;
        }
        db.closeDatabase();
        throw new DbLayerException("cannot find data for id " + id);
    }

    public void setState(Integer id, RaceState state) throws DbLayerException {
        DSLContext dslContext = db.connectDatabase();
        RacesRecord rec = dslContext.selectFrom(Tables.RACES).where(Tables.RACES.ID.equal(id)).limit(1).fetchOne();
        if (rec != null) {
            rec.setState(state.name());
            rec.store();
            db.closeDatabase();
            return;
        }
        db.closeDatabase();
        throw new DbLayerException("cannot find data for id " + id);
    }

    public void setStartTime(Integer id, Integer startTime) throws DbLayerException {
        DSLContext dslContext = db.connectDatabase();
        RacesRecord rec = dslContext.selectFrom(Tables.RACES).where(Tables.RACES.ID.equal(id)).limit(1).fetchOne();
        if (rec != null) {
            rec.setStarttime(startTime);
            rec.store();
            db.closeDatabase();
            return;
        }
        db.closeDatabase();
        throw new DbLayerException("cannot find data for id " + id);
    }

    public Integer addLap(Integer raceId, Long chipId, Integer lap, Integer duration) throws DbLayerException {
        DSLContext dslContext = db.connectDatabase();
        LapsRecord lapRecord = dslContext.newRecord(Tables.LAPS);
        lapRecord.setRaceid(raceId);
        lapRecord.setChipid(chipId);
        lapRecord.setLap(lap);
        lapRecord.setDuration(duration);
        lapRecord.store();
        db.closeDatabase();
        return lapRecord.getId();
    }

    public List<Long> getRaceParticipants(Integer raceId) throws DbLayerException {
        List<Long> ret = new ArrayList<>();
        DSLContext dslContext = db.connectDatabase();
        Result<LapsRecord> rec = dslContext.selectFrom(Tables.LAPS).where(Tables.LAPS.RACEID.equal(raceId)).fetch();
        if (rec != null && rec.size() > 0) {
            for (LapsRecord record : rec) {
                ret.add(record.getChipid());
            }
            db.closeDatabase();
            return ret;
        }
        db.closeDatabase();
        throw new DbLayerException("cannot find data for id " + raceId);
    }

    public List<LapsRecord> getLaps(Integer raceId, Long chipId) throws DbLayerException {
        List<LapsRecord> ret = new ArrayList<>();
        DSLContext dslContext = db.connectDatabase();
        List<LapsRecord> rec = dslContext.selectFrom(Tables.LAPS).where(Tables.LAPS.RACEID.equal(raceId)).and(Tables.LAPS.CHIPID.equal(chipId)).orderBy(Tables.LAPS.LAP).fetch();
        for (LapsRecord record : rec) {
            ret.add(record);
        }
        return ret;
    }

    public List<RacesRecord> getRaces() throws DbLayerException {
        List<RacesRecord> ret = new ArrayList<>();
        DSLContext dslContext = db.connectDatabase();
        Result<RacesRecord> rec = dslContext.selectFrom(Tables.RACES).orderBy(Tables.RACES.STARTTIME.desc()).fetch();
        for (RacesRecord record : rec) {
            ret.add(record);
        }
        return ret;
    }

    public Result<Record4<Long, Integer, BigDecimal, String>> getToplist(Integer startTime) throws DbLayerException {
        LOG.debug("start time: " + startTime);
        DSLContext dslContext = db.connectDatabase();
        Result<Record4<Long, Integer, BigDecimal, String>> records = dslContext
                .select(
                        Tables.LAPS.CHIPID,
                        DSL.count(Tables.LAPS.ID).as("NROFLAPS"),
                        DSL.sum(Tables.LAPS.DURATION).as("TOTALDURATION"),
                        Tables.PARTICIPANTS.NAME
                ).from(Tables.LAPS)
                .join(Tables.PARTICIPANTS)
                .on(Tables.PARTICIPANTS.CHIPID.equal(Tables.LAPS.CHIPID))
                .join(Tables.RACES)
                .on(Tables.RACES.ID.equal(Tables.LAPS.RACEID))
                .where(Tables.RACES.STARTTIME.greaterOrEqual(startTime).and(Tables.LAPS.LAP.notEqual(0)))
                .groupBy(Tables.LAPS.CHIPID)
                .fetch();
        LOG.debug(records.toString());
        return records;
    }

}
