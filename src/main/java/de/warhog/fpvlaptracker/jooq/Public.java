/*
 * This file is generated by jOOQ.
*/
package de.warhog.fpvlaptracker.jooq;


import de.warhog.fpvlaptracker.jooq.tables.Config;
import de.warhog.fpvlaptracker.jooq.tables.Laps;
import de.warhog.fpvlaptracker.jooq.tables.Participants;
import de.warhog.fpvlaptracker.jooq.tables.Races;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Generated;

import org.jooq.Catalog;
import org.jooq.Sequence;
import org.jooq.Table;
import org.jooq.impl.SchemaImpl;


/**
 * This class is generated by jOOQ.
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.9.0"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Public extends SchemaImpl {

    private static final long serialVersionUID = -675038193;

    /**
     * The reference instance of <code>PUBLIC</code>
     */
    public static final Public PUBLIC = new Public();

    /**
     * The table <code>PUBLIC.CONFIG</code>.
     */
    public final Config CONFIG = de.warhog.fpvlaptracker.jooq.tables.Config.CONFIG;

    /**
     * The table <code>PUBLIC.RACES</code>.
     */
    public final Races RACES = de.warhog.fpvlaptracker.jooq.tables.Races.RACES;

    /**
     * The table <code>PUBLIC.LAPS</code>.
     */
    public final Laps LAPS = de.warhog.fpvlaptracker.jooq.tables.Laps.LAPS;

    /**
     * The table <code>PUBLIC.PARTICIPANTS</code>.
     */
    public final Participants PARTICIPANTS = de.warhog.fpvlaptracker.jooq.tables.Participants.PARTICIPANTS;

    /**
     * No further instances allowed
     */
    private Public() {
        super("PUBLIC", null);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Catalog getCatalog() {
        return DefaultCatalog.DEFAULT_CATALOG;
    }

    @Override
    public final List<Sequence<?>> getSequences() {
        List result = new ArrayList();
        result.addAll(getSequences0());
        return result;
    }

    private final List<Sequence<?>> getSequences0() {
        return Arrays.<Sequence<?>>asList(
            Sequences.SYSTEM_SEQUENCE_ACBE0DED_AB09_47F1_ABB9_38D83FDEF82A,
            Sequences.SYSTEM_SEQUENCE_FD7D9474_1D79_4002_AF15_F82B395DC783);
    }

    @Override
    public final List<Table<?>> getTables() {
        List result = new ArrayList();
        result.addAll(getTables0());
        return result;
    }

    private final List<Table<?>> getTables0() {
        return Arrays.<Table<?>>asList(
            Config.CONFIG,
            Races.RACES,
            Laps.LAPS,
            Participants.PARTICIPANTS);
    }
}
