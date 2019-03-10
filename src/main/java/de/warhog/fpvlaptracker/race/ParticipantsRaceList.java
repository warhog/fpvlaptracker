package de.warhog.fpvlaptracker.race;

import de.warhog.fpvlaptracker.entities.Participant;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ParticipantsRaceList {

    private static final Logger LOG = LoggerFactory.getLogger(ParticipantsRaceList.class);

    private final List<Participant> participants = new ArrayList<>();

    public boolean hasParticipants() {
        return !participants.isEmpty();
    }

    public void addParticipant(final Participant participant) {
        participants.add(participant);
    }

    public void removeParticipant(final Participant participant) {
        if (participants.contains(participant)) {
            participants.remove(participant);
        } else {
            LOG.debug("participant not in race " + participant.getChipId());
        }
    }

    public Participant getParticipantByChipId(final Long chipId) {
        for (Participant participant : participants) {
            if (participant.getChipId().equals(chipId)) {
                return participant;
            }
        }
        throw new RuntimeException("participant for chipid not found " + chipId);
    }

    public boolean hasParticipant(final Long chipId) {
        try {
            getParticipantByChipId(chipId);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public List<Participant> getParticipants() {
        return new ArrayList<>(participants);
    }

}
