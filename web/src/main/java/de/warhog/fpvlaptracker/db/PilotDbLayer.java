package de.warhog.fpvlaptracker.db;

import de.warhog.fpvlaptracker.entities.Pilot;
import de.warhog.fpvlaptracker.jooq.Tables;
import de.warhog.fpvlaptracker.jooq.tables.records.PilotsRecord;
import de.warhog.fpvlaptracker.service.NodesService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jooq.DSLContext;
import org.jooq.Result;
import org.jooq.exception.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PilotDbLayer {

    /* db layout
    CREATE TABLE PUBLIC.PILOTS (
        NAME VARCHAR(255) NOT NULL,
        CHIPID INTEGER,
        PRIMARY KEY (NAME),
        UNIQUE KEY name_unique (NAME)
    );
     */
    private static final Logger LOG = LoggerFactory.getLogger(PilotDbLayer.class);

    @Autowired
    private Db db;
    
    @Autowired
    private NodesService nodesService;
    
    public List<Pilot> getPilots() throws DbLayerException {
        DSLContext dslContext = db.connectDatabase();
        Result<PilotsRecord> rec = dslContext.selectFrom(Tables.PILOTS).fetch();
        final List<Pilot> result = new ArrayList<>();
        if (rec != null) {
            for (PilotsRecord pr : rec) {
                Pilot pilot = new Pilot();
                pilot.setName(pr.getName());
                pilot.setChipId(pr.getChipid());
                if (pr.getChipid() != null) {
                    pilot.setNode(nodesService.getNode(pr.getChipid()));
                }
                result.add(pilot);
            }
        }
        db.closeDatabase();
        return result;
    }

    public void createOrUpdatePilot(String name, Long chipId) throws DbLayerException {
        DSLContext dslContext = db.connectDatabase();
        PilotsRecord rec = dslContext.selectFrom(Tables.PILOTS).where(Tables.PILOTS.NAME.equal(name)).limit(1).fetchOne();
        if (rec == null) {
            PilotsRecord pr = dslContext.newRecord(Tables.PILOTS);
            pr.setName(name);
            pr.setChipid(chipId);
            pr.store();
        } else {
            rec.setChipid(chipId);
            rec.store();
        }
        db.closeDatabase();
    }

    public PilotsRecord getPilotRecord(String name) throws DbLayerException {
        DSLContext dslContext = db.connectDatabase();
        PilotsRecord rec = dslContext.selectFrom(Tables.PILOTS).where(Tables.PILOTS.NAME.equal(name)).limit(1).fetchOne();
        if (rec != null) {
            db.closeDatabase();
            return rec;
        }
        db.closeDatabase();
        throw new DbLayerException("cannot find pilot with name " + name);
    }

    public void deletePilot(String name) throws DbLayerException {
        DSLContext dslContext = db.connectDatabase();
        PilotsRecord rec = dslContext.selectFrom(Tables.PILOTS).where(Tables.PILOTS.NAME.equal(name)).limit(1).fetchOne();
        if (rec != null) {
            rec.delete();
        }
        db.closeDatabase();
    }

}
