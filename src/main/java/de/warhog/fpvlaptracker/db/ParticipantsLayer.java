package de.warhog.fpvlaptracker.db;

import de.warhog.fpvlaptracker.jooq.Tables;
import de.warhog.fpvlaptracker.jooq.tables.records.ParticipantsRecord;
import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ParticipantsLayer {

    /* db layout
    CREATE TABLE PUBLIC.PARTICIPANTS (
        CHIPID INTEGER NOT NULL,
        NAME VARCHAR(255),
        PRIMARY KEY (CHIPID),
        UNIQUE KEY chipid_unique (CHIPID)
    );
     */
    private static final Logger LOG = LoggerFactory.getLogger(ParticipantsLayer.class);

    @Autowired
    private Db db;

    public void createOrUpdateParticipant(Integer chipId, String name) throws DbLayerException {
        DSLContext dslContext = db.connectDatabase();
        ParticipantsRecord rec = dslContext.selectFrom(Tables.PARTICIPANTS).where(Tables.PARTICIPANTS.CHIPID.equal(chipId)).limit(1).fetchOne();
        if (rec == null) {
            ParticipantsRecord pr = dslContext.newRecord(Tables.PARTICIPANTS);
            pr.setChipid(chipId);
            pr.setName(name);
            pr.store();
        } else {
            rec.setName(name);
            rec.store();
        }
        db.closeDatabase();
    }

    public ParticipantsRecord getParticipantForChipId(Integer chipId) throws DbLayerException {
        DSLContext dslContext = db.connectDatabase();
        ParticipantsRecord rec = dslContext.selectFrom(Tables.PARTICIPANTS).where(Tables.PARTICIPANTS.CHIPID.equal(chipId)).limit(1).fetchOne();
        if (rec != null) {
            db.closeDatabase();
            return rec;
        }
        db.closeDatabase();
        throw new DbLayerException("cannot find participant with chipid " + chipId);
    }

}
