package de.warhog.fpvlaptracker.race;

import de.warhog.fpvlaptracker.entities.RaceState;
import de.warhog.fpvlaptracker.controllers.WebSocketController;
import de.warhog.fpvlaptracker.entities.Participant;
import de.warhog.fpvlaptracker.entities.racedata.FixedTimeRaceParticipantData;
import de.warhog.fpvlaptracker.service.AudioService;
import de.warhog.fpvlaptracker.service.RestService;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RaceLogicHandler {

    private static final Logger LOG = LoggerFactory.getLogger(RaceLogicHandler.class);

    private LocalDateTime startTime = null;
//    private IRaceLogic raceLogic = null;
    private RaceState state = RaceState.WAITING;
    
    @Autowired
    ParticipantsList participantsList;
    
    @Autowired
    WebSocketController webSocketController;
    
    @Autowired
    RestService restService;
    
    @Autowired
    AudioService audioService;
    
    @Autowired
    RaceLogicFixedTime raceLogic;
    
    public void init(IRaceLogic raceLogic) {
//        this.raceLogic = raceLogic;
        this.raceLogic.init();
    }

    public void addParticipant(Participant participant) {
        if (isRunning()) {
            throw new IllegalStateException("cannot add participants to running race");
        }
        participantsList.addParticipant(participant);
    }

    public void removeParticipant(Participant participant) {
        if (isRunning()) {
            throw new IllegalStateException("cannot remove participants from running race");
        }
        participantsList.removeParticipant(participant);
        if (!participantsList.hasParticipants()) {
            LOG.info("no participants anymore, setting state to waiting");
            setState(RaceState.WAITING);
        }
    }

    public void startRace() {
        LOG.info("starting race");
        raceLogic.startRace();
        setStartTime(LocalDateTime.now());
    }

    public void stopRace() {
        LOG.info("stopping race");
        raceLogic.stopRace();
    }

    public void addLap(Long chipId, Long duration, Integer rssi) {
        LOG.debug("add lap", chipId, duration, rssi);
//        raceLogic.addLap(chipId, duration, rssi);
//        audioService.playLap();
        webSocketController.sendNewLapMessage(chipId);
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    private void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public Boolean isRunning() {
        return getState() == RaceState.GETREADY || getState() == RaceState.RUNNING;
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
//            for (Participant participant : participantsService.getAllParticipants()) {
//                try {
//                    if (restService.checkAvailability(participant.getIp())) {
//                        LOG.debug("participant with chipid " + participant.getChipId() + " found");
//                    } else {
//                        LOG.info("participant with chipid " + participant.getChipId() + " not found, removing");
//                        participantsService.removeParticipant(participant);
//                        webSocketController.sendNewParticipantMessage(participant.getChipId());
//                        audioService.speakUnregistered(participant.getName());
//                    }
//                } catch (Exception ex) {
//                    LOG.error("cannot check availability for " + participant.toString());
//                }
//            }
        } else {
            LOG.debug("race is running, skipping check");
        }
    }

}
