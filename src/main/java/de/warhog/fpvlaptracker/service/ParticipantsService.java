package de.warhog.fpvlaptracker.service;

import de.warhog.fpvlaptracker.entities.Participant;
import de.warhog.fpvlaptracker.race.RaceLogicHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ParticipantsService {

    private static final Logger LOG = LoggerFactory.getLogger(ParticipantsService.class);

    @Autowired
    private RaceLogicHandler race;

    private final List<Participant> participants = new ArrayList<>();

    public List<Participant> getAllParticipants() {
        return new ArrayList<>(participants);
    }

    public void addParticipant(Participant participant) {
        if (participants.contains(participant)) {
            throw new IllegalArgumentException("participant already existing");
        }
        participants.add(participant);
    }

    public Participant getParticipant(Long chipId) {
        for (Participant participant : participants) {
            if (Objects.equals(participant.getChipId(), chipId)) {
                return participant;
            }
        }
        throw new IllegalArgumentException("participant with chipid not found: " + chipId.toString());
    }

    public Boolean hasParticipant(Participant participant) {
        return participants.contains(participant);
    }

    public Boolean hasParticipant(Long chipId) {
        for (Participant participant : participants) {
            if (Objects.equals(participant.getChipId(), chipId)) {
                return true;
            }
        }
        return false;
    }

    public void clearParticipants() {
        participants.clear();
    }

    public void removeParticipant(Participant participant) {
        if (participants.contains(participant)) {
            race.removeParticipant(participant);
            participants.remove(participant);
        }
    }

}
