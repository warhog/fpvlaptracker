package de.warhog.fpvlaptracker.service;

import de.warhog.fpvlaptracker.race.entities.Participant;
import de.warhog.fpvlaptracker.race.entities.ParticipantRaceData;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ParticipantRaceService {

    private static final Logger LOG = LoggerFactory.getLogger(ParticipantRaceService.class);

    private final Map<Participant, ParticipantRaceData> participants = new HashMap<>();

    public boolean hasParticipants() {
        return !participants.isEmpty();
    }

    public void resetParticipantsData() {
        for (Map.Entry<Participant, ParticipantRaceData> entry : participants.entrySet()) {
            // reset the data fro every participant
            entry.setValue(new ParticipantRaceData());
        }
    }

    public void addParticipant(final Participant participant) {
        participants.put(participant, new ParticipantRaceData());
    }

    public void removeParticipant(final Participant participant) {
        if (participants.containsKey(participant)) {
            participants.remove(participant);
        } else {
            LOG.debug("participant not in race " + participant.getChipId());
        }
    }

    public Participant getParticipantByChipId(final Long chipId) {
        for (Map.Entry<Participant, ParticipantRaceData> entry : participants.entrySet()) {
            Participant participant = entry.getKey();
            if (participant.getChipId().equals(chipId)) {
                return participant;
            }
        }
        throw new RuntimeException("participant for chipid not found " + chipId);
    }

    public ParticipantRaceData getParticipantRaceData(final Participant participant) {
        if (participants.containsKey(participant)) {
            return participants.get(participant);
        } else {
            throw new RuntimeException("participant not in race");
        }
    }

    public boolean checkEnded(final Integer numberOfLaps) {
        for (Map.Entry<Participant, ParticipantRaceData> entry : participants.entrySet()) {
            Participant participant = entry.getKey();
            if (entry.getValue().getCurrentLap() <= numberOfLaps) {
                LOG.debug("participant has not completed yet: " + participant.getName() + ", numberOfLaps: " + numberOfLaps + ", currentLap: " + entry.getValue().getCurrentLap());
                return false;
            }
        }
        return true;
    }

    public Map<Integer, Duration> getParticipantLapTimes(final Participant participant) {
        if (participants.containsKey(participant)) {
            ParticipantRaceData participantRaceData = participants.get(participant);
            return participantRaceData.getLaps();
        }
        throw new IllegalArgumentException("participant not found: " + participant.toString());
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
        List<Participant> ret = new ArrayList<>();
        participants.entrySet().forEach((entry) -> {
            ret.add(entry.getKey());
        });
        return ret;
    }

}
