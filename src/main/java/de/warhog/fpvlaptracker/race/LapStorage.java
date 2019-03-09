package de.warhog.fpvlaptracker.race;

import de.warhog.fpvlaptracker.controllers.dtos.LapDataResult;
import de.warhog.fpvlaptracker.entities.LapTimeList;
import de.warhog.fpvlaptracker.entities.Participant;
import de.warhog.fpvlaptracker.entities.Participant;
import de.warhog.fpvlaptracker.race.ParticipantsList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LapStorage {

    private static final Logger LOG = LoggerFactory.getLogger(LapStorage.class);

    @Autowired
    private ParticipantsList participantsList;

    final Map<Long, LapTimeList> lapData = new HashMap<>();

    public void clear() {
        lapData.clear();
    }

    public void addParticipant(final Long chipid) {
        if (!lapData.containsKey(chipid)) {
            LOG.debug("chipid not found in lap store, adding: " + chipid);
            lapData.put(chipid, new LapTimeList());
        }
    }

    public void addLap(final Long chipid, final Long duration, final Integer rssi) {
        addParticipant(chipid);
        LapTimeList ltl = lapData.get(chipid);
        LOG.debug("adding lap with duration " + duration + " and rssi " + rssi);
        ltl.addLap(duration, rssi);
    }

    public void invalidateLap(final Long chipid, final Integer lap, final boolean state) {
        if (!lapData.containsKey(chipid)) {
            LOG.error("chipid not found in lap store: " + chipid);
            throw new RuntimeException("chipid not found");
        }
        LapTimeList ltl = lapData.get(chipid);
        ltl.invalidateLap(lap, state);
    }

    public void toggleLapValidity(final Long chipid, final Integer lap) {
        if (!lapData.containsKey(chipid)) {
            LOG.error("chipid not found in lap store: " + chipid);
            throw new RuntimeException("chipid not found");
        }
        LapTimeList ltl = lapData.get(chipid);
        if (ltl.isLapValid(lap)) {
            LOG.debug("set lap invalid");
            ltl.invalidateLap(lap, true);
        } else {
            LOG.debug("set lap valid");
            ltl.invalidateLap(lap, false);
        }
    }

    public LapTimeList getLapData(final long chipid) {
        if (!lapData.containsKey(chipid)) {
            LOG.error("chipid not found in lap store: " + chipid);
            return new LapTimeList();
        }
        return lapData.get(chipid);
    }

    public HashMap<Participant, LapTimeList> getLapData() {
        final HashMap<Participant, LapTimeList> data = new HashMap<>();
        for (Participant participant : participantsList.getParticipants()) {
            data.put(participant, getLapData(participant.getChipId()));
        }
        return data;
    }

    public Map<Long, LapTimeList> getLapDataWithChipId() {
        return lapData;
    }

    public List<LapDataResult> getLapDataExtended() {
        List<LapDataResult> result = new ArrayList<>();
        for (Participant participant : participantsList.getParticipants()) {
            LapDataResult lapDataResult = new LapDataResult();
            LapTimeList participantLapData = getLapData(participant.getChipId());
            lapDataResult.setLapTimeList(participantLapData);
            lapDataResult.setParticipant(participant);
            lapDataResult.setLapValidity(participantLapData.getInvalidLaps());
            result.add(lapDataResult);
        }
        return result;
    }

    public void repopulate() {
        clear();
        for (Participant participant : participantsList.getParticipants()) {
            addParticipant(participant.getChipId());
        }
    }

}
