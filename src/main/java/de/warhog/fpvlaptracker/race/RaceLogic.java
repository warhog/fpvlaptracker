package de.warhog.fpvlaptracker.race;

import de.warhog.fpvlaptracker.entities.RaceState;
import de.warhog.fpvlaptracker.controllers.WebSocketController;
import de.warhog.fpvlaptracker.entities.ParticipantRaceData;
import de.warhog.fpvlaptracker.entities.Participant;
import de.warhog.fpvlaptracker.service.AudioService;
import de.warhog.fpvlaptracker.service.ConfigService;
import de.warhog.fpvlaptracker.service.ParticipantsService;
import de.warhog.fpvlaptracker.service.RaceDbService;
import de.warhog.fpvlaptracker.service.RestService;
import de.warhog.fpvlaptracker.service.ServiceLayerException;
import de.warhog.fpvlaptracker.util.TimeUtil;
import java.time.LocalDateTime;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RaceLogic {

    private static final Logger LOG = LoggerFactory.getLogger(RaceLogic.class);

    private LocalDateTime startTime = null;
    private Integer numberOfLaps = 10;
    private RaceState state = RaceState.WAITING;
    private Integer currentRaceId = null;

    @Autowired
    private ParticipantsList participantRaceService;

    @Autowired
    private ConfigService configService;

    @Autowired
    private RaceDbService racesDbService;

    @Autowired
    private ParticipantsService participantsService;

    @Autowired
    private AudioService audioService;

    @Autowired
    private WebSocketController webSocketController;

    @Autowired
    private RestService restService;

    @Autowired
    private TimeUtil timeUtil;

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
                    racesDbService.setState(currentRaceId, RaceState.FINISHED);
                } catch (ServiceLayerException ex) {
                    LOG.error("failed to stop current race: " + ex.getMessage(), ex);
                    throw new RuntimeException("failed to stop current race");
                }
            }
        }
        if (!participantRaceService.hasParticipants()) {
            throw new IllegalStateException("no participants");
        }
        checkParticipantsStillAvailable();
        LOG.info("initialize new race");
        setState(RaceState.GETREADY);
        setStartTime(LocalDateTime.now());
        try {
            currentRaceId = racesDbService.createRace(numberOfLaps, state);
            racesDbService.setStartTime(currentRaceId, timeUtil.localDateTimeToUnix(LocalDateTime.now()));
            LOG.debug("created new race id: " + currentRaceId);
        } catch (ServiceLayerException ex) {
            LOG.error("failed to store new race: " + ex.getMessage(), ex);
            throw new RuntimeException("failed to store new race");
        }
//        participantRaceService.resetParticipantsData();
    }

    public void addParticipant(Participant participant) {
        if (isRunning()) {
            throw new IllegalStateException("cannot add participants to running race");
        }
        participantRaceService.addParticipant(participant);
    }

    public void removeParticipant(Participant participant) {
        if (isRunning()) {
            throw new IllegalStateException("cannot remove participants from running race");
        }
        participantRaceService.removeParticipant(participant);
        if (!participantRaceService.hasParticipants()) {
            LOG.info("no participants anymore, setting state to waiting");
            setState(RaceState.WAITING);
        }
    }

    private void startRace() {
        LOG.info("starting race");
        audioService.playStart();
        setState(RaceState.RUNNING);
        setStartTime(LocalDateTime.now());
        if (currentRaceId != null) {
            try {
                racesDbService.setStartTime(currentRaceId, timeUtil.localDateTimeToUnix(LocalDateTime.now()));
                racesDbService.setState(currentRaceId, state);
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
            racesDbService.setState(currentRaceId, state);
        } catch (ServiceLayerException ex) {
            LOG.error("failed to stop race: " + ex.getMessage(), ex);
            throw new RuntimeException("failed to stop race");
        }
    }

    public void addLap(Long chipId, Long duration, Integer rssi) {
        LOG.debug("add lap", chipId, duration, rssi);
        boolean raceStartedInThisLap = false;
        boolean oneParticipantReachedEnd = false;
        String participantEndedName = "";
        if (!isRunning()) {
            LOG.info("cannot add lap when race is not running, chipid: " + chipId);
            if (participantsService.hasParticipant(chipId)) {
                audioService.speakInvalidLap(participantsService.getParticipant(chipId).getName());
            }
            return;
        }
        if (getState() == RaceState.GETREADY) {
            LOG.info("starting race");
            startRace();
            raceStartedInThisLap = true;
        }
        Participant participant = participantRaceService.getParticipantByChipId(chipId);
//        ParticipantRaceData data = participantRaceService.getParticipantRaceData(participant);
//        if (data == null) {
//            throw new RuntimeException("no data for participant found: " + participant.toString());
//        }
//        if (data.hasEnded(numberOfLaps)) {
//            LOG.info("participant already ended race " + participant.getName());
//            audioService.speakAlreadyDone(participant.getName());
//        } else {
//            if (currentRaceId != null) {
//                try {
//                    racesDbService.addLap(currentRaceId, chipId, data.getCurrentLap(), duration.intValue());
//                } catch (ServiceLayerException ex) {
//                    LOG.error(ex.getMessage(), ex);
//                    throw new RuntimeException("cannot add lap");
//                }
//            }
//            data.addLap(duration, rssi);
//            if (data.hasEnded(numberOfLaps)) {
//                LOG.info("participant reached lap limit " + participant.getName());
//                oneParticipantReachedEnd = true;
//                participantEndedName = participant.getName();
//            } else if (!raceStartedInThisLap) {
//                audioService.playLap();
//            }
//
//        }

        // test if all of the participants has reached the lap limit
//        if (participantRaceService.checkEnded(numberOfLaps)) {
//            LOG.info("race ended");
//            audioService.speakFinished();
//            stopRace();
//        } else {
//            if (oneParticipantReachedEnd) {
//                audioService.speakParticipantEnded(participantEndedName);
//            }
//        }

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
        webSocketController.sendRaceStateChangedMessage(state);
    }

    @Scheduled(fixedDelay = 60000L)
    public void checkParticipantsStillAvailable() {
        LOG.debug("checking for non-existing participants");
        if (!isRunning()) {
            for (Participant participant : participantsService.getAllParticipants()) {
                try {
                    if (restService.checkAvailability(participant.getIp())) {
                        LOG.debug("participant with chipid " + participant.getChipId() + " found");
                    } else {
                        LOG.info("participant with chipid " + participant.getChipId() + " not found, removing");
                        participantsService.removeParticipant(participant);
                        webSocketController.sendNewParticipantMessage(participant.getChipId());
                        audioService.speakUnregistered(participant.getName());
                    }
                } catch (Exception ex) {
                    LOG.error("cannot check availability for " + participant.toString());
                }
            }
        } else {
            LOG.debug("race is running, skipping check");
        }
    }

}
