package de.warhog.fpvlaptracker.race;

import de.warhog.fpvlaptracker.entities.RaceState;
import de.warhog.fpvlaptracker.controllers.WebSocketController;
import de.warhog.fpvlaptracker.entities.Participant;
import de.warhog.fpvlaptracker.entities.racedata.LapStorage;
import de.warhog.fpvlaptracker.service.AudioService;
import de.warhog.fpvlaptracker.service.RestService;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RaceLogicHandler {

    private static final Logger LOG = LoggerFactory.getLogger(RaceLogicHandler.class);

    private IRaceLogic raceLogic = null;
    private RaceType raceType = RaceType.ROUND_BASED;

    @Autowired
    ParticipantsList participantsList;

    @Autowired
    WebSocketController webSocketController;

    @Autowired
    RestService restService;

    @Autowired
    AudioService audioService;

    @Autowired
    RaceLogicFixedTime raceLogicFixedTime;

    @Autowired
    RaceLogicRoundBased raceLogicRoundBased;

    @Autowired
    LapStorage lapStorage;
    
    public void init(RaceType raceType) {
        this.raceType = raceType;
        if (this.raceType == RaceType.ROUND_BASED) {
            raceLogic = raceLogicRoundBased;
        } else if (this.raceType == RaceType.FIXED_TIME) {
            raceLogic = raceLogicFixedTime;
        }
        raceLogic.init();
    }
    
    public void setNumberOfLaps(Integer numberOfLaps) {
        raceLogicRoundBased.setNumberOfLaps(numberOfLaps);
    }
    
    public Integer getNumberOfLaps() {
        return raceLogicRoundBased.getNumberOfLaps();
    }

    private void fillRaceLogic() {
        if (raceLogic == null) {
            raceLogic = raceLogicRoundBased;
        }
    }
    
    public void addParticipant(Participant participant) {
        fillRaceLogic();
        if (raceLogic.isRunning()) {
            throw new IllegalStateException("cannot add participants to running race");
        }
        participantsList.addParticipant(participant);
    }

    public void removeParticipant(Participant participant) {
        fillRaceLogic();
        if (raceLogic.isRunning()) {
            throw new IllegalStateException("cannot remove participants from running race");
        }
        participantsList.removeParticipant(participant);
        if (!participantsList.hasParticipants()) {
            LOG.info("no participants anymore, setting state to waiting");
            raceLogic.setState(RaceState.WAITING);
        }
    }

    public void startRace() {
        fillRaceLogic();
        LOG.info("starting race");
        lapStorage.repopulate();
        checkParticipantsStillAvailable();
        raceLogic.startRace();
        setStartTime(LocalDateTime.now());
    }
    
    public Map<String, Long> getToplist() {
        return raceLogic.getToplist();
    }

    public void stopRace() {
        fillRaceLogic();
        LOG.info("stopping race");
        raceLogic.stopRace();
    }

    public void addLap(Long chipId, Long duration, Integer rssi) {
        fillRaceLogic();
        LOG.debug("add lap", chipId, duration, rssi);
        raceLogic.addLap(chipId, duration, rssi);
    }

    public RaceState getState() {
        fillRaceLogic();
        return raceLogic.getState();
    }
    
    public RaceType getRaceType() {
        return raceType;
    }

    public void setState(RaceState state) {
        fillRaceLogic();
        raceLogic.setState(state);
    }

    public LocalDateTime getStartTime() {
        fillRaceLogic();
        return raceLogic.getStartTime();
    }

    private void setStartTime(LocalDateTime startTime) {
        fillRaceLogic();
        raceLogic.setStartTime(startTime);
    }
    
    @Scheduled(fixedDelay = 60000L)
    public void checkParticipantsStillAvailable() {
        fillRaceLogic();
        LOG.debug("checking for non-existing participants");
        if (!raceLogic.isRunning()) {
            for (Participant participant : participantsList.getParticipants()) {
                try {
                    if (restService.checkAvailability(participant.getIp())) {
                        LOG.debug("participant with chipid " + participant.getChipId() + " found");
                    } else {
                        LOG.info("participant with chipid " + participant.getChipId() + " not found, removing");
                        participantsList.removeParticipant(participant);
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
