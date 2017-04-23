package de.warhog.fpvlaptracker.race;

import de.warhog.fpvlaptracker.controllers.WebSocketController;
import de.warhog.fpvlaptracker.race.entities.ParticipantRaceData;
import de.warhog.fpvlaptracker.race.entities.Participant;
import de.warhog.fpvlaptracker.service.AudioService;
import de.warhog.fpvlaptracker.service.ConfigService;
import de.warhog.fpvlaptracker.service.ParticipantsService;
import de.warhog.fpvlaptracker.service.RaceDbService;
import de.warhog.fpvlaptracker.service.ServiceLayerException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RaceLogic {

    private static final Logger LOG = LoggerFactory.getLogger(RaceLogic.class);

    private final Map<Participant, ParticipantRaceData> participants = new HashMap<>();

    private LocalDateTime startTime = null;
    private Integer numberOfLaps = 10;
    private RaceState state = RaceState.WAITING;
    private Integer currentRaceId = null;

    @Autowired
    private ConfigService configService;

    @Autowired
    private RaceDbService racesService;

    @Autowired
    private ParticipantsService participantsService;

    @Autowired
    private AudioService audioService;

    @Autowired
    private WebSocketController webSocketController;

    @PostConstruct
    public void init() {
        try {
            numberOfLaps = configService.getNumberOfLaps();
        } catch (ServiceLayerException ex) {
            LOG.debug("cannot get number of laps, staying with default value");
        }

    }

    public void initializeNewRace() {
        if (isRunning()) {
            stopRace();
            if (currentRaceId != null) {
                try {
                    racesService.setState(currentRaceId, RaceState.FINISHED);
                } catch (ServiceLayerException ex) {
                    LOG.error("failed to stop current race: " + ex.getMessage(), ex);
                    throw new RuntimeException("failed to stop current race");
                }
            }
        }
        if (participants.isEmpty()) {
            throw new IllegalStateException("no participants");
        }
        participantsService.checkParticipantsStillAvailable();
        LOG.info("initialize new race");
        setState(RaceState.GETREADY);
        setStartTime(LocalDateTime.now());
        try {
            currentRaceId = racesService.createRace(numberOfLaps, state);
            ZoneOffset zoneOffset = ZoneId.of(configService.getTimezone()).getRules().getOffset(LocalDateTime.now());
            racesService.setStartTime(currentRaceId, Math.toIntExact(LocalDateTime.now().toEpochSecond(zoneOffset)));
            LOG.debug("created new race id: " + currentRaceId);
        } catch (ServiceLayerException ex) {
            LOG.error("failed to store new race: " + ex.getMessage(), ex);
            throw new RuntimeException("failed to store new race");
        }
        for (Map.Entry<Participant, ParticipantRaceData> entry : participants.entrySet()) {
            // reset the data fro every participant
            entry.setValue(new ParticipantRaceData());
        }

    }

    public void addParticipant(Participant participant) {
        if (isRunning()) {
            throw new IllegalStateException("cannot add participants to running race");
        }
        participants.put(participant, new ParticipantRaceData());
    }

    public HashMap<Participant, ParticipantRaceData> getParticipants() {
        return new HashMap<>(participants);
    }

    public void removeParticipant(Participant participant) {
        if (isRunning()) {
            throw new IllegalStateException("cannot remove participants from running race");
        }
        if (participants.containsKey(participant)) {
            participants.remove(participant);
        } else {
            LOG.debug("participant not in race " + participant.getChipId());
        }
        if (participants.isEmpty()) {
            LOG.info("no participants anymore, setting state to waiting");
            setState(RaceState.WAITING);
        }
    }

    private void startRace() {
        LOG.info("starting race");
        audioService.playStart();
        webSocketController.sendAudioRaceStartedMessage();
        setState(RaceState.RUNNING);
        setStartTime(LocalDateTime.now());
        if (currentRaceId != null) {
            try {
                ZoneOffset zoneOffset = ZoneId.of(configService.getTimezone()).getRules().getOffset(LocalDateTime.now());
                racesService.setStartTime(currentRaceId, Math.toIntExact(LocalDateTime.now().toEpochSecond(zoneOffset)));
                racesService.setState(currentRaceId, state);
            } catch (ServiceLayerException ex) {
                LOG.error("failed to start race: " + ex.getMessage(), ex);
                throw new RuntimeException("failed to start race");
            }
        }
    }

    public void stopRace() {
        LOG.info("stopping race");
        setState(RaceState.FINISHED);
        try {
            racesService.setState(currentRaceId, state);
        } catch (ServiceLayerException ex) {
            LOG.error("failed to stop race: " + ex.getMessage(), ex);
            throw new RuntimeException("failed to stop race");
        }
    }

    public Boolean hasParticipant(Integer chipId) {
        try {
            findParticipant(chipId);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private Participant findParticipant(Integer chipId) {
        for (Map.Entry<Participant, ParticipantRaceData> entry : participants.entrySet()) {
            Participant participant = entry.getKey();
            if (participant.getChipId().equals(chipId)) {
                return participant;
            }
        }
        throw new RuntimeException("participant for chipid not found " + chipId);
    }

    public void addLap(Integer chipId, Long duration, Integer rssi) {
        LOG.debug("add lap", chipId, duration, rssi);
        boolean raceStartedInThisLap = false;
        if (!isRunning()) {
            LOG.info("cannot add lap when race is not running, chipid: " + chipId);
            audioService.playInvalidLap();
            webSocketController.sendAudioInvalidLapMessage();
            return;
        }
        if (getState() == RaceState.GETREADY) {
            LOG.info("starting race");
            startRace();
            raceStartedInThisLap = true;
        }
        Participant participant = findParticipant(chipId);
        ParticipantRaceData data = participants.get(participant);
        if (data == null) {
            throw new RuntimeException("no data for participant found: " + participant.toString());
        }
        if (data.hasEnded(numberOfLaps)) {
            LOG.info("participant already ended race " + participant.getName());
            audioService.playInvalidLap();
            webSocketController.sendAudioInvalidLapMessage();
        } else {
            if (currentRaceId != null) {
                try {
                    racesService.addLap(currentRaceId, chipId, data.getCurrentLap(), duration.intValue());
                } catch (ServiceLayerException ex) {
                    LOG.error(ex.getMessage(), ex);
                    throw new RuntimeException("cannot add lap");
                }
            }
            data.addLap(duration, rssi);
            if (data.hasEnded(numberOfLaps)) {
                LOG.info("participant reached lap limit " + participant.getName());
                audioService.playParticipantEnded();
                webSocketController.sendAudioParticipantEndedMessage();
            } else if (!raceStartedInThisLap) {
                audioService.playLap();
                webSocketController.sendAudioLapMessage();
            }

        }

        // test if all of the participants has reached the lap limit
        if (checkEnded()) {
            LOG.info("race ended");
            audioService.playFinished();
            webSocketController.sendAudioRaceEndedMessage();
            stopRace();
        }

    }

    private Boolean checkEnded() {
        for (Map.Entry<Participant, ParticipantRaceData> entry : participants.entrySet()) {
            Participant participant = entry.getKey();
            if (entry.getValue().getCurrentLap() <= numberOfLaps) {
                LOG.debug("participant has not completed yet: " + participant.getName());
                return false;
            }
        }
        return true;
    }

    public HashMap<Participant, ParticipantRaceData> getLaps() {
        return new HashMap<>(participants);
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    private void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public Integer getNumberOfLaps() {
        return numberOfLaps;
    }

    public Map<Integer, Duration> getParticipantLapTimes(Participant participant) {
        if (participants.containsKey(participant)) {
            ParticipantRaceData participantRaceData = participants.get(participant);
            return participantRaceData.getLaps();
        }
        throw new IllegalArgumentException("participant not found: " + participant.toString());
    }

    public Boolean isRunning() {
        return getState() == RaceState.GETREADY || getState() == RaceState.RUNNING;
    }

    public void setNumberOfLaps(Integer numberOfLaps) {
        if (isRunning()) {
            throw new IllegalStateException("race is currently running");
        }
        this.numberOfLaps = numberOfLaps;
    }

    public RaceState getState() {
        return state;
    }

    private void setState(RaceState state) {
        this.state = state;
    }

}
