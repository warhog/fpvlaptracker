/*
 * This file is generated by jOOQ.
 */
package de.warhog.fpvlaptracker.jooq.tables.records;


import de.warhog.fpvlaptracker.jooq.tables.Pilots;

import javax.annotation.Generated;

import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record2;
import org.jooq.Row2;
import org.jooq.impl.UpdatableRecordImpl;


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
public class PilotsRecord extends UpdatableRecordImpl<PilotsRecord> implements Record2<String, Long> {

    private static final long serialVersionUID = 844496899;

    /**
     * Setter for <code>PUBLIC.PILOTS.NAME</code>.
     */
    public PilotsRecord setName(String value) {
        set(0, value);
        return this;
    }

    /**
     * Getter for <code>PUBLIC.PILOTS.NAME</code>.
     */
    public String getName() {
        return (String) get(0);
    }

    /**
     * Setter for <code>PUBLIC.PILOTS.CHIPID</code>.
     */
    public PilotsRecord setChipid(Long value) {
        set(1, value);
        return this;
    }

    /**
     * Getter for <code>PUBLIC.PILOTS.CHIPID</code>.
     */
    public Long getChipid() {
        return (Long) get(1);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Record1<String> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record2 type implementation
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Row2<String, Long> fieldsRow() {
        return (Row2) super.fieldsRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Row2<String, Long> valuesRow() {
        return (Row2) super.valuesRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field1() {
        return Pilots.PILOTS.NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Long> field2() {
        return Pilots.PILOTS.CHIPID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String component1() {
        return getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long component2() {
        return getChipid();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value1() {
        return getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long value2() {
        return getChipid();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PilotsRecord value1(String value) {
        setName(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PilotsRecord value2(Long value) {
        setChipid(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PilotsRecord values(String value1, Long value2) {
        value1(value1);
        value2(value2);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached PilotsRecord
     */
    public PilotsRecord() {
        super(Pilots.PILOTS);
    }

    /**
     * Create a detached, initialised PilotsRecord
     */
    public PilotsRecord(String name, Long chipid) {
        super(Pilots.PILOTS);

        set(0, name);
        set(1, chipid);
    }
}
