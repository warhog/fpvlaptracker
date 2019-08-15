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

import javax.annotation.Generated;

import org.jooq.Index;
import org.jooq.OrderField;
import org.jooq.impl.Internal;


/**
 * A class modelling indexes of tables of the <code>PUBLIC</code> schema.
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.11.11"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Indexes {

    // -------------------------------------------------------------------------
    // INDEX definitions
    // -------------------------------------------------------------------------

    public static final Index PRIMARY_KEY_7 = Indexes0.PRIMARY_KEY_7;
    public static final Index PRIMARY_KEY_9 = Indexes0.PRIMARY_KEY_9;
    public static final Index PRIMARY_KEY_A = Indexes0.PRIMARY_KEY_A;
    public static final Index PRIMARY_KEY_8 = Indexes0.PRIMARY_KEY_8;
    public static final Index PRIMARY_KEY_F = Indexes0.PRIMARY_KEY_F;
    public static final Index PRIMARY_KEY_4 = Indexes0.PRIMARY_KEY_4;

    // -------------------------------------------------------------------------
    // [#1459] distribute members to avoid static initialisers > 64kb
    // -------------------------------------------------------------------------

    private static class Indexes0 {
        public static Index PRIMARY_KEY_7 = Internal.createIndex("PRIMARY_KEY_7", Config.CONFIG, new OrderField[] { Config.CONFIG.CONFIG_KEY }, true);
        public static Index PRIMARY_KEY_9 = Internal.createIndex("PRIMARY_KEY_9", Laps.LAPS, new OrderField[] { Laps.LAPS.ID }, true);
        public static Index PRIMARY_KEY_A = Internal.createIndex("PRIMARY_KEY_A", PersistentLogins.PERSISTENT_LOGINS, new OrderField[] { PersistentLogins.PERSISTENT_LOGINS.SERIES }, true);
        public static Index PRIMARY_KEY_8 = Internal.createIndex("PRIMARY_KEY_8", Pilots.PILOTS, new OrderField[] { Pilots.PILOTS.NAME }, true);
        public static Index PRIMARY_KEY_F = Internal.createIndex("PRIMARY_KEY_F", Profiles.PROFILES, new OrderField[] { Profiles.PROFILES.CHIPID, Profiles.PROFILES.NAME }, true);
        public static Index PRIMARY_KEY_4 = Internal.createIndex("PRIMARY_KEY_4", Races.RACES, new OrderField[] { Races.RACES.ID }, true);
    }
}
