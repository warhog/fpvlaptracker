package de.warhog.fpvlaptracker.db;

import de.warhog.fpvlaptracker.entities.Profile;
import de.warhog.fpvlaptracker.jooq.Tables;
import de.warhog.fpvlaptracker.jooq.tables.records.ProfilesRecord;
import java.util.ArrayList;
import java.util.List;
import org.jooq.DSLContext;
import org.jooq.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ProfilesLayer {

    /* db layout
    CREATE TABLE PUBLIC.PROFILES (
        CHIPID INTEGER NOT NULL,
        NAME VARCHAR(256),
        DATA VARCHAR(4096),
        PRIMARY KEY (CHIPID),
        UNIQUE KEY chipid_unique (CHIPID)
    );
     */
    private static final Logger LOG = LoggerFactory.getLogger(ProfilesLayer.class);

    @Autowired
    private Db db;

    public void createOrUpdateProfile(Long chipId, String name, String data) throws DbLayerException {
        LOG.debug("create or update profile " + chipId + ": " + name);
        DSLContext dslContext = db.connectDatabase();
        ProfilesRecord rec = dslContext.selectFrom(Tables.PROFILES).where(Tables.PROFILES.CHIPID.equal(chipId)).and(Tables.PROFILES.NAME.equal(name)).limit(1).fetchOne();
        if (rec == null) {
            LOG.debug("create");
            ProfilesRecord pr = dslContext.newRecord(Tables.PROFILES);
            pr.setChipid(chipId);
            pr.setData(data);
            pr.setName(name);
            pr.store();
        } else {
            LOG.debug("update");
            rec.setData(data);
            rec.store();
        }
        db.closeDatabase();
    }

    public List<Profile> getProfilesForChipId(Long chipId) throws DbLayerException {
        DSLContext dslContext = db.connectDatabase();
        Result<ProfilesRecord> rec = dslContext.selectFrom(Tables.PROFILES).where(Tables.PROFILES.CHIPID.equal(chipId)).fetch();
        final List<Profile> result = new ArrayList<>();
        if (rec != null) {
            for (ProfilesRecord pr : rec) {
                result.add(new Profile(pr.getName(), pr.getChipid(), pr.getData()));
            }
        }
        LOG.debug("getProfilesForChipId " + chipId + ": " + result.toString());
        db.closeDatabase();
        return result;
    }

    public Profile getProfileForChipIdWithName(Long chipId, String name) throws DbLayerException {
        DSLContext dslContext = db.connectDatabase();
        ProfilesRecord rec = dslContext.selectFrom(Tables.PROFILES).where(Tables.PROFILES.CHIPID.equal(chipId)).and(Tables.PROFILES.NAME.equal(name)).limit(1).fetchOne();
        if (rec != null) {
            Profile result = new Profile(rec.getName(), rec.getChipid(), rec.getData());
            db.closeDatabase();
            return result;
        }
        db.closeDatabase();
        throw new DbLayerException("cannot find profile with name " + name + " for chipid " + chipId);
    }

    public void deleteProfile(Long chipId, String name) throws DbLayerException {
        DSLContext dslContext = db.connectDatabase();
        ProfilesRecord rec = dslContext.selectFrom(Tables.PROFILES).where(Tables.PROFILES.CHIPID.equal(chipId)).and(Tables.PROFILES.NAME.equal(name)).limit(1).fetchOne();
        if (rec != null) {
            rec.delete();
        }
        db.closeDatabase();
    }

}
