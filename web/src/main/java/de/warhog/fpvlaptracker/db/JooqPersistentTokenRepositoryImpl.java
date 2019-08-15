package de.warhog.fpvlaptracker.db;

import de.warhog.fpvlaptracker.jooq.Tables;
import de.warhog.fpvlaptracker.jooq.tables.records.PersistentLoginsRecord;
import java.sql.Timestamp;
import java.util.Date;
import org.slf4j.Logger;
import org.jooq.DSLContext;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.authentication.rememberme.PersistentRememberMeToken;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.stereotype.Component;

@Component
public class JooqPersistentTokenRepositoryImpl implements PersistentTokenRepository {

    private static final Logger LOG = LoggerFactory.getLogger(JooqPersistentTokenRepositoryImpl.class);
    
    @Autowired
    private Db db;

    @Override
    public void createNewToken(PersistentRememberMeToken prmt) {
        LOG.debug("create new token for user " + prmt.getUsername() + "with series " + prmt.getSeries());
        DSLContext dslContext;
        try {
            dslContext = db.connectDatabase();
            PersistentLoginsRecord plr = dslContext.newRecord(Tables.PERSISTENT_LOGINS);
            plr.setUsername(prmt.getUsername());
            plr.setToken(prmt.getTokenValue());
            plr.setSeries(prmt.getSeries());
            plr.setLastUsed(new Timestamp(prmt.getDate().getTime()));
            plr.store();
        } catch (DbLayerException ex) {
            LOG.error("cannot create new token: " + ex.getMessage(), ex);
        } finally {
            try {
                db.closeDatabase();
            } catch (DbLayerException ex) {
                LOG.error("cannot close db: " + ex.getMessage(), ex);
            }
        }
    }

    @Override
    public void updateToken(String series, String tokenValue, Date lastUsed) {
        LOG.debug("update token for series " + series);
        DSLContext dslContext;
        try {
            dslContext = db.connectDatabase();
            PersistentLoginsRecord plr = dslContext.selectFrom(Tables.PERSISTENT_LOGINS).where(Tables.PERSISTENT_LOGINS.SERIES.equal(series)).limit(1).fetchOne();
            if (plr != null) {
                plr.setToken(tokenValue);
                plr.setLastUsed(new Timestamp(lastUsed.getTime()));
                plr.store();
            }
        } catch (DbLayerException ex) {
            LOG.error("cannot update token: " + ex.getMessage(), ex);
        } finally {
            try {
                db.closeDatabase();
            } catch (DbLayerException ex) {
                LOG.error("cannot close db: " + ex.getMessage(), ex);
            }
        }
    }

    @Override
    public PersistentRememberMeToken getTokenForSeries(String series) {
        LOG.debug("get token for series " + series);
        DSLContext dslContext;
        try {
            dslContext = db.connectDatabase();
            PersistentLoginsRecord plr = dslContext.selectFrom(Tables.PERSISTENT_LOGINS).where(Tables.PERSISTENT_LOGINS.SERIES.equal(series)).limit(1).fetchOne();
            if (plr != null) {
                return new PersistentRememberMeToken(plr.getUsername(), plr.getSeries(), plr.getToken(), plr.getLastUsed());
            }
        } catch (DbLayerException ex) {
            LOG.error("cannot get token: " + ex.getMessage(), ex);
        } finally {
            try {
                db.closeDatabase();
            } catch (DbLayerException ex) {
                LOG.error("cannot close db: " + ex.getMessage(), ex);
            }
        }
        return null;
    }

    @Override
    public void removeUserTokens(String username) {
        LOG.debug("remove token for user " + username);
        DSLContext dslContext;
        try {
            dslContext = db.connectDatabase();
            PersistentLoginsRecord plr = dslContext.selectFrom(Tables.PERSISTENT_LOGINS).where(Tables.PERSISTENT_LOGINS.USERNAME.equal(username)).limit(1).fetchOne();
            if (plr != null) {
                plr.delete();
            }
        } catch (DbLayerException ex) {
            LOG.error("cannot remove token: " + ex.getMessage(), ex);
        } finally {
            try {
                db.closeDatabase();
            } catch (DbLayerException ex) {
                LOG.error("cannot close db: " + ex.getMessage(), ex);
            }
        }
    }

}
