/*
 * This file is generated by jOOQ.
 */
package de.warhog.fpvlaptracker.jooq;


import de.warhog.fpvlaptracker.jooq.tables.Config;
import de.warhog.fpvlaptracker.jooq.tables.Laps;
import de.warhog.fpvlaptracker.jooq.tables.PersistentLogins;
import de.warhog.fpvlaptracker.jooq.tables.Pilots;
import de.warhog.fpvlaptracker.jooq.tables.Profiles;
import de.warhog.fpvlaptracker.jooq.tables.Races;
import de.warhog.fpvlaptracker.jooq.tables.records.ConfigRecord;
import de.warhog.fpvlaptracker.jooq.tables.records.LapsRecord;
import de.warhog.fpvlaptracker.jooq.tables.records.PersistentLoginsRecord;
import de.warhog.fpvlaptracker.jooq.tables.records.PilotsRecord;
import de.warhog.fpvlaptracker.jooq.tables.records.ProfilesRecord;
import de.warhog.fpvlaptracker.jooq.tables.records.RacesRecord;

import javax.annotation.Generated;

import org.jooq.Identity;
import org.jooq.UniqueKey;
import org.jooq.impl.Internal;


/**
 * A class modelling foreign key relationships and constraints of tables of 
 * the <code>PUBLIC</code> schema.
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.11.11"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Keys {

    // -------------------------------------------------------------------------
    // IDENTITY definitions
    // -------------------------------------------------------------------------

    public static final Identity<LapsRecord, Integer> IDENTITY_LAPS = Identities0.IDENTITY_LAPS;
    public static final Identity<RacesRecord, Integer> IDENTITY_RACES = Identities0.IDENTITY_RACES;

    // -------------------------------------------------------------------------
    // UNIQUE and PRIMARY KEY definitions
    // -------------------------------------------------------------------------

    public static final UniqueKey<ConfigRecord> PK_CONFIG = UniqueKeys0.PK_CONFIG;
    public static final UniqueKey<ConfigRecord> UQ_CONFIG = UniqueKeys0.UQ_CONFIG;
    public static final UniqueKey<LapsRecord> PK_LAPS = UniqueKeys0.PK_LAPS;
    public static final UniqueKey<LapsRecord> UQ_LAPS = UniqueKeys0.UQ_LAPS;
    public static final UniqueKey<PersistentLoginsRecord> CONSTRAINT_A = UniqueKeys0.CONSTRAINT_A;
    public static final UniqueKey<PilotsRecord> CONSTRAINT_8 = UniqueKeys0.CONSTRAINT_8;
    public static final UniqueKey<ProfilesRecord> PK_PROFILES = UniqueKeys0.PK_PROFILES;
    public static final UniqueKey<RacesRecord> PK_RACES = UniqueKeys0.PK_RACES;
    public static final UniqueKey<RacesRecord> UQ_RACES = UniqueKeys0.UQ_RACES;

    // -------------------------------------------------------------------------
    // FOREIGN KEY definitions
    // -------------------------------------------------------------------------


    // -------------------------------------------------------------------------
    // [#1459] distribute members to avoid static initialisers > 64kb
    // -------------------------------------------------------------------------

    private static class Identities0 {
        public static Identity<LapsRecord, Integer> IDENTITY_LAPS = Internal.createIdentity(Laps.LAPS, Laps.LAPS.ID);
        public static Identity<RacesRecord, Integer> IDENTITY_RACES = Internal.createIdentity(Races.RACES, Races.RACES.ID);
    }

    private static class UniqueKeys0 {
        public static final UniqueKey<ConfigRecord> PK_CONFIG = Internal.createUniqueKey(Config.CONFIG, "pk_config", Config.CONFIG.CONFIG_KEY);
        public static final UniqueKey<ConfigRecord> UQ_CONFIG = Internal.createUniqueKey(Config.CONFIG, "uq_config", Config.CONFIG.CONFIG_KEY);
        public static final UniqueKey<LapsRecord> PK_LAPS = Internal.createUniqueKey(Laps.LAPS, "pk_laps", Laps.LAPS.ID);
        public static final UniqueKey<LapsRecord> UQ_LAPS = Internal.createUniqueKey(Laps.LAPS, "uq_laps", Laps.LAPS.ID);
        public static final UniqueKey<PersistentLoginsRecord> CONSTRAINT_A = Internal.createUniqueKey(PersistentLogins.PERSISTENT_LOGINS, "CONSTRAINT_A", PersistentLogins.PERSISTENT_LOGINS.SERIES);
        public static final UniqueKey<PilotsRecord> CONSTRAINT_8 = Internal.createUniqueKey(Pilots.PILOTS, "CONSTRAINT_8", Pilots.PILOTS.NAME);
        public static final UniqueKey<ProfilesRecord> PK_PROFILES = Internal.createUniqueKey(Profiles.PROFILES, "pk_profiles", Profiles.PROFILES.CHIPID, Profiles.PROFILES.NAME);
        public static final UniqueKey<RacesRecord> PK_RACES = Internal.createUniqueKey(Races.RACES, "pk_races", Races.RACES.ID);
        public static final UniqueKey<RacesRecord> UQ_RACES = Internal.createUniqueKey(Races.RACES, "uq_races", Races.RACES.ID);
    }
}
