/*
 * This file is generated by jOOQ.
 */
package de.warhog.fpvlaptracker.jooq.tables;


import de.warhog.fpvlaptracker.jooq.Indexes;
import de.warhog.fpvlaptracker.jooq.Keys;
import de.warhog.fpvlaptracker.jooq.Public;
import de.warhog.fpvlaptracker.jooq.tables.records.PersistentLoginsRecord;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Generated;

import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Index;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Schema;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.TableImpl;


/**
 * This class is generated by jOOQ.
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.11.11"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class PersistentLogins extends TableImpl<PersistentLoginsRecord> {

    private static final long serialVersionUID = 338533681;

    /**
     * The reference instance of <code>PUBLIC.PERSISTENT_LOGINS</code>
     */
    public static final PersistentLogins PERSISTENT_LOGINS = new PersistentLogins();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<PersistentLoginsRecord> getRecordType() {
        return PersistentLoginsRecord.class;
    }

    /**
     * The column <code>PUBLIC.PERSISTENT_LOGINS.USERNAME</code>.
     */
    public final TableField<PersistentLoginsRecord, String> USERNAME = createField("USERNAME", org.jooq.impl.SQLDataType.VARCHAR(255).nullable(false), this, "");

    /**
     * The column <code>PUBLIC.PERSISTENT_LOGINS.SERIES</code>.
     */
    public final TableField<PersistentLoginsRecord, String> SERIES = createField("SERIES", org.jooq.impl.SQLDataType.VARCHAR(64).nullable(false), this, "");

    /**
     * The column <code>PUBLIC.PERSISTENT_LOGINS.TOKEN</code>.
     */
    public final TableField<PersistentLoginsRecord, String> TOKEN = createField("TOKEN", org.jooq.impl.SQLDataType.VARCHAR(64).nullable(false), this, "");

    /**
     * The column <code>PUBLIC.PERSISTENT_LOGINS.LAST_USED</code>.
     */
    public final TableField<PersistentLoginsRecord, Timestamp> LAST_USED = createField("LAST_USED", org.jooq.impl.SQLDataType.TIMESTAMP.precision(6).nullable(false), this, "");

    /**
     * Create a <code>PUBLIC.PERSISTENT_LOGINS</code> table reference
     */
    public PersistentLogins() {
        this(DSL.name("PERSISTENT_LOGINS"), null);
    }

    /**
     * Create an aliased <code>PUBLIC.PERSISTENT_LOGINS</code> table reference
     */
    public PersistentLogins(String alias) {
        this(DSL.name(alias), PERSISTENT_LOGINS);
    }

    /**
     * Create an aliased <code>PUBLIC.PERSISTENT_LOGINS</code> table reference
     */
    public PersistentLogins(Name alias) {
        this(alias, PERSISTENT_LOGINS);
    }

    private PersistentLogins(Name alias, Table<PersistentLoginsRecord> aliased) {
        this(alias, aliased, null);
    }

    private PersistentLogins(Name alias, Table<PersistentLoginsRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""));
    }

    public <O extends Record> PersistentLogins(Table<O> child, ForeignKey<O, PersistentLoginsRecord> key) {
        super(child, key, PERSISTENT_LOGINS);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Schema getSchema() {
        return Public.PUBLIC;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Index> getIndexes() {
        return Arrays.<Index>asList(Indexes.PRIMARY_KEY_A);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UniqueKey<PersistentLoginsRecord> getPrimaryKey() {
        return Keys.CONSTRAINT_A;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UniqueKey<PersistentLoginsRecord>> getKeys() {
        return Arrays.<UniqueKey<PersistentLoginsRecord>>asList(Keys.CONSTRAINT_A);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PersistentLogins as(String alias) {
        return new PersistentLogins(DSL.name(alias), this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PersistentLogins as(Name alias) {
        return new PersistentLogins(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public PersistentLogins rename(String name) {
        return new PersistentLogins(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public PersistentLogins rename(Name name) {
        return new PersistentLogins(name, null);
    }
}
