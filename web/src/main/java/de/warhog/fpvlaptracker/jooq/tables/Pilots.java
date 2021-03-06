/*
 * This file is generated by jOOQ.
 */
package de.warhog.fpvlaptracker.jooq.tables;


import de.warhog.fpvlaptracker.jooq.Indexes;
import de.warhog.fpvlaptracker.jooq.Keys;
import de.warhog.fpvlaptracker.jooq.Public;
import de.warhog.fpvlaptracker.jooq.tables.records.PilotsRecord;

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
public class Pilots extends TableImpl<PilotsRecord> {

    private static final long serialVersionUID = -1868519974;

    /**
     * The reference instance of <code>PUBLIC.PILOTS</code>
     */
    public static final Pilots PILOTS = new Pilots();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<PilotsRecord> getRecordType() {
        return PilotsRecord.class;
    }

    /**
     * The column <code>PUBLIC.PILOTS.NAME</code>.
     */
    public final TableField<PilotsRecord, String> NAME = createField("NAME", org.jooq.impl.SQLDataType.VARCHAR(255).nullable(false), this, "");

    /**
     * The column <code>PUBLIC.PILOTS.CHIPID</code>.
     */
    public final TableField<PilotsRecord, Long> CHIPID = createField("CHIPID", org.jooq.impl.SQLDataType.BIGINT, this, "");

    /**
     * Create a <code>PUBLIC.PILOTS</code> table reference
     */
    public Pilots() {
        this(DSL.name("PILOTS"), null);
    }

    /**
     * Create an aliased <code>PUBLIC.PILOTS</code> table reference
     */
    public Pilots(String alias) {
        this(DSL.name(alias), PILOTS);
    }

    /**
     * Create an aliased <code>PUBLIC.PILOTS</code> table reference
     */
    public Pilots(Name alias) {
        this(alias, PILOTS);
    }

    private Pilots(Name alias, Table<PilotsRecord> aliased) {
        this(alias, aliased, null);
    }

    private Pilots(Name alias, Table<PilotsRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""));
    }

    public <O extends Record> Pilots(Table<O> child, ForeignKey<O, PilotsRecord> key) {
        super(child, key, PILOTS);
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
        return Arrays.<Index>asList(Indexes.PRIMARY_KEY_8);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UniqueKey<PilotsRecord> getPrimaryKey() {
        return Keys.CONSTRAINT_8;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UniqueKey<PilotsRecord>> getKeys() {
        return Arrays.<UniqueKey<PilotsRecord>>asList(Keys.CONSTRAINT_8);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Pilots as(String alias) {
        return new Pilots(DSL.name(alias), this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Pilots as(Name alias) {
        return new Pilots(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public Pilots rename(String name) {
        return new Pilots(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public Pilots rename(Name name) {
        return new Pilots(name, null);
    }
}
