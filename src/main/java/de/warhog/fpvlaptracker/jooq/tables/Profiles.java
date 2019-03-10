/*
 * This file is generated by jOOQ.
 */
package de.warhog.fpvlaptracker.jooq.tables;


import de.warhog.fpvlaptracker.jooq.Indexes;
import de.warhog.fpvlaptracker.jooq.Keys;
import de.warhog.fpvlaptracker.jooq.Public;
import de.warhog.fpvlaptracker.jooq.tables.records.ProfilesRecord;

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
        "jOOQ version:3.11.8"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Profiles extends TableImpl<ProfilesRecord> {

    private static final long serialVersionUID = -147901724;

    /**
     * The reference instance of <code>PUBLIC.PROFILES</code>
     */
    public static final Profiles PROFILES = new Profiles();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<ProfilesRecord> getRecordType() {
        return ProfilesRecord.class;
    }

    /**
     * The column <code>PUBLIC.PROFILES.CHIPID</code>.
     */
    public final TableField<ProfilesRecord, Long> CHIPID = createField("CHIPID", org.jooq.impl.SQLDataType.BIGINT.nullable(false), this, "");

    /**
     * The column <code>PUBLIC.PROFILES.NAME</code>.
     */
    public final TableField<ProfilesRecord, String> NAME = createField("NAME", org.jooq.impl.SQLDataType.VARCHAR(256).nullable(false), this, "");

    /**
     * The column <code>PUBLIC.PROFILES.DATA</code>.
     */
    public final TableField<ProfilesRecord, String> DATA = createField("DATA", org.jooq.impl.SQLDataType.VARCHAR(4096).nullable(false), this, "");

    /**
     * Create a <code>PUBLIC.PROFILES</code> table reference
     */
    public Profiles() {
        this(DSL.name("PROFILES"), null);
    }

    /**
     * Create an aliased <code>PUBLIC.PROFILES</code> table reference
     */
    public Profiles(String alias) {
        this(DSL.name(alias), PROFILES);
    }

    /**
     * Create an aliased <code>PUBLIC.PROFILES</code> table reference
     */
    public Profiles(Name alias) {
        this(alias, PROFILES);
    }

    private Profiles(Name alias, Table<ProfilesRecord> aliased) {
        this(alias, aliased, null);
    }

    private Profiles(Name alias, Table<ProfilesRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""));
    }

    public <O extends Record> Profiles(Table<O> child, ForeignKey<O, ProfilesRecord> key) {
        super(child, key, PROFILES);
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
        return Arrays.<Index>asList(Indexes.PRIMARY_KEY_F);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UniqueKey<ProfilesRecord> getPrimaryKey() {
        return Keys.PK_PROFILES;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UniqueKey<ProfilesRecord>> getKeys() {
        return Arrays.<UniqueKey<ProfilesRecord>>asList(Keys.PK_PROFILES);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Profiles as(String alias) {
        return new Profiles(DSL.name(alias), this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Profiles as(Name alias) {
        return new Profiles(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public Profiles rename(String name) {
        return new Profiles(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public Profiles rename(Name name) {
        return new Profiles(name, null);
    }
}
