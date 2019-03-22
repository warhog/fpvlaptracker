/*
 * This file is generated by jOOQ.
 */
package de.warhog.fpvlaptracker.jooq.tables.records;


import de.warhog.fpvlaptracker.jooq.tables.Profiles;

import javax.annotation.Generated;

import org.jooq.Field;
import org.jooq.Record2;
import org.jooq.Record3;
import org.jooq.Row3;
import org.jooq.impl.UpdatableRecordImpl;


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
public class ProfilesRecord extends UpdatableRecordImpl<ProfilesRecord> implements Record3<Long, String, String> {

    private static final long serialVersionUID = 792174759;

    /**
     * Setter for <code>PUBLIC.PROFILES.CHIPID</code>.
     */
    public ProfilesRecord setChipid(Long value) {
        set(0, value);
        return this;
    }

    /**
     * Getter for <code>PUBLIC.PROFILES.CHIPID</code>.
     */
    public Long getChipid() {
        return (Long) get(0);
    }

    /**
     * Setter for <code>PUBLIC.PROFILES.NAME</code>.
     */
    public ProfilesRecord setName(String value) {
        set(1, value);
        return this;
    }

    /**
     * Getter for <code>PUBLIC.PROFILES.NAME</code>.
     */
    public String getName() {
        return (String) get(1);
    }

    /**
     * Setter for <code>PUBLIC.PROFILES.DATA</code>.
     */
    public ProfilesRecord setData(String value) {
        set(2, value);
        return this;
    }

    /**
     * Getter for <code>PUBLIC.PROFILES.DATA</code>.
     */
    public String getData() {
        return (String) get(2);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Record2<Long, String> key() {
        return (Record2) super.key();
    }

    // -------------------------------------------------------------------------
    // Record3 type implementation
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Row3<Long, String, String> fieldsRow() {
        return (Row3) super.fieldsRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Row3<Long, String, String> valuesRow() {
        return (Row3) super.valuesRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Long> field1() {
        return Profiles.PROFILES.CHIPID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field2() {
        return Profiles.PROFILES.NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field3() {
        return Profiles.PROFILES.DATA;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long component1() {
        return getChipid();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String component2() {
        return getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String component3() {
        return getData();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long value1() {
        return getChipid();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value2() {
        return getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value3() {
        return getData();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ProfilesRecord value1(Long value) {
        setChipid(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ProfilesRecord value2(String value) {
        setName(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ProfilesRecord value3(String value) {
        setData(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ProfilesRecord values(Long value1, String value2, String value3) {
        value1(value1);
        value2(value2);
        value3(value3);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached ProfilesRecord
     */
    public ProfilesRecord() {
        super(Profiles.PROFILES);
    }

    /**
     * Create a detached, initialised ProfilesRecord
     */
    public ProfilesRecord(Long chipid, String name, String data) {
        super(Profiles.PROFILES);

        set(0, chipid);
        set(1, name);
        set(2, data);
    }
}