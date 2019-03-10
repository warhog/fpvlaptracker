package de.warhog.fpvlaptracker.db;

import de.warhog.fpvlaptracker.jooq.Tables;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class Db {

    private static final Logger LOG = LoggerFactory.getLogger(Db.class);

    private Connection conn;
    private static final String DATABASE_NAME = "./flt.h2";

    @SuppressFBWarnings(value = "DMI_EMPTY_DB_PASSWORD", justification = "local only h2 database needs no password")
    public DSLContext connectDatabase() throws DbLayerException {
        try {
            conn = DriverManager.getConnection("jdbc:h2:file:" + DATABASE_NAME, "sa", "");
            return DSL.using(conn, SQLDialect.H2);
        } catch (SQLException ex) {
            LOG.error(ex.getMessage(), ex);
            throw new DbLayerException("cannot connect to database");
        }
    }

    public void closeDatabase() throws DbLayerException {
        try {
            conn.close();
        } catch (SQLException ex) {
            throw new DbLayerException("cannot close database");
        }
    }

    @SuppressFBWarnings(value = "DMI_EMPTY_DB_PASSWORD", justification = "local only h2 database needs no password")
    public static void createDatabase() {

        Connection conn = null;
        try {
            conn = DriverManager.getConnection("jdbc:h2:file:" + DATABASE_NAME, "sa", "");
            DSLContext dslContext = DSL.using(conn, SQLDialect.H2);

            LOG.info("create table config if not existing");
            dslContext.createTableIfNotExists(Tables.CONFIG)
                    .column(Tables.CONFIG.CONFIG_KEY, SQLDataType.VARCHAR.length(255))
                    .column(Tables.CONFIG.CONFIG_VALUE, SQLDataType.VARCHAR.length(4096))
                    .constraints(
                            DSL.constraint("pk_config").primaryKey(Tables.CONFIG.CONFIG_KEY),
                            DSL.constraint("uq_config").unique(Tables.CONFIG.CONFIG_KEY)
                    )
                    .execute();

            LOG.info("create table races if not existing");
            dslContext.createTableIfNotExists(Tables.RACES)
                    .column(Tables.RACES.ID, SQLDataType.INTEGER.nullable(false).identity(true))
                    .column(Tables.RACES.STARTTIME, SQLDataType.INTEGER.nullable(true))
                    .column(Tables.RACES.LAPS, SQLDataType.INTEGER.nullable(true))
                    .column(Tables.RACES.STATE, SQLDataType.VARCHAR.length(255).nullable(false))
                    .constraints(
                            DSL.constraint("pk_races").primaryKey(Tables.RACES.ID),
                            DSL.constraint("uq_races").unique(Tables.RACES.ID)
                    )
                    .execute();

            LOG.info("create table laps if not existing");
            dslContext.createTableIfNotExists(Tables.LAPS)
                    .column(Tables.LAPS.ID, SQLDataType.INTEGER.nullable(false).identity(true))
                    .column(Tables.LAPS.RACEID, SQLDataType.INTEGER.nullable(false))
                    .column(Tables.LAPS.CHIPID, SQLDataType.BIGINT.nullable(false))
                    .column(Tables.LAPS.LAP, SQLDataType.INTEGER.nullable(false))
                    .column(Tables.LAPS.DURATION, SQLDataType.INTEGER.nullable(false))
                    .constraints(
                            DSL.constraint("pk_laps").primaryKey(Tables.LAPS.ID),
                            DSL.constraint("uq_laps").unique(Tables.LAPS.ID)
                    )
                    .execute();

            LOG.info("create table participants if not existing");
            dslContext.createTableIfNotExists(Tables.PARTICIPANTS)
                    .column(Tables.PARTICIPANTS.CHIPID, SQLDataType.BIGINT.nullable(false))
                    .column(Tables.PARTICIPANTS.NAME, SQLDataType.VARCHAR(255).nullable(false))
                    .constraints(
                            DSL.constraint("pk_participants").primaryKey(Tables.PARTICIPANTS.CHIPID),
                            DSL.constraint("uq_participants").unique(Tables.PARTICIPANTS.CHIPID)
                    )
                    .execute();

            LOG.info("create table profiles if not existing");
            dslContext.createTableIfNotExists(Tables.PROFILES)
                    .column(Tables.PROFILES.CHIPID, SQLDataType.BIGINT.nullable(false))
                    .column(Tables.PROFILES.NAME, SQLDataType.VARCHAR(256).nullable(false))
                    .column(Tables.PROFILES.DATA, SQLDataType.VARCHAR(4096).nullable(false))
                    .constraints(
                            DSL.constraint("pk_profiles").primaryKey(Tables.PROFILES.CHIPID, Tables.PROFILES.NAME)
                    )
                    .execute();

        } catch (SQLException ex) {
            LOG.error(ex.getMessage(), ex);
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ex) {
                LOG.error(ex.getMessage(), ex);
            }
        }

    }
}
