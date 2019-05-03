package de.warhog.fpvlaptracker.race;

import de.warhog.fpvlaptracker.entities.ToplistEntry;
import java.util.Comparator;
import org.apache.commons.lang3.builder.CompareToBuilder;

public class ToplistRoundBasedComparator implements Comparator<ToplistEntry> {

    @Override
    public int compare(ToplistEntry o1, ToplistEntry o2) {
        return new CompareToBuilder()
                .append(o2.getLaps(), o1.getLaps())
                .append(o1.getTotalLapTime(), o2.getTotalLapTime())
                .toComparison();
    }

}