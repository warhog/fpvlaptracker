package de.warhog.fpvlaptracker.race;

import de.warhog.fpvlaptracker.entities.RaceState;
import de.warhog.fpvlaptracker.controllers.WebSocketController;
import de.warhog.fpvlaptracker.controllers.dtos.RaceStateResult;
import de.warhog.fpvlaptracker.entities.Participant;
import de.warhog.fpvlaptracker.entities.ToplistEntry;
import de.warhog.fpvlaptracker.service.AudioService;
import de.warhog.fpvlaptracker.service.ConfigService;
import de.warhog.fpvlaptracker.service.RestService;
import de.warhog.fpvlaptracker.service.ServiceLayerException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
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
    ParticipantsRaceList participantsRaceList;

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
    
    @Autowired
    ConfigService configService;

    @PostConstruct
    public void postConstruct() {
        init(RaceType.ROUND_BASED);
        raceLogic = raceLogicRoundBased;
    }

    public void init(RaceType raceType) {
        this.raceType = raceType;
        if (this.raceType == RaceType.ROUND_BASED) {
            raceLogic = raceLogicRoundBased;
        } else if (this.raceType == RaceType.FIXED_TIME) {
            raceLogic = raceLogicFixedTime;
        }
        raceLogic.init();
    }

    public void addParticipant(Participant participant) {
        if (raceLogic.isRunning()) {
            throw new IllegalStateException("cannot add participants to running race");
        }
        participantsRaceList.addParticipant(participant);
    }

    public void removeParticipant(Participant participant) {
        if (raceLogic.isRunning()) {
            throw new IllegalStateException("cannot remove participants from running race");
        }
        participantsRaceList.removeParticipant(participant);
        if (!participantsRaceList.hasParticipants()) {
            LOG.info("no participants anymore, setting state to waiting");
            raceLogic.setState(RaceState.WAITING);
        }
    }

    public void startRace() {
        LOG.info("starting race");
        lapStorage.repopulate();
        checkParticipantsStillAvailable();
        raceLogic.startRace();
        setStartTime(LocalDateTime.now());
    }

    public List<ToplistEntry> getToplist() {
        return raceLogic.getToplist();
    }

    public void stopRace() {
        LOG.info("stopping race");
        raceLogic.stopRace();
        audioService.speakRaceAborted();
    }

    public void addLap(Long chipId, Long duration, Integer rssi) {
        LOG.debug("add lap", chipId, duration, rssi);
        raceLogic.addLap(chipId, duration, rssi);
    }

    public RaceState getState() {
        return raceLogic.getState();
    }

    public RaceType getRaceType() {
        return raceType;
    }

    public void setState(RaceState state) {
        raceLogic.setState(state);
    }

    public LocalDateTime getStartTime() {
        return raceLogic.getStartTime();
    }

    private void setStartTime(LocalDateTime startTime) {
        raceLogic.setStartTime(startTime);
    }

    @Scheduled(fixedDelay = 60000L)
    public void checkParticipantsStillAvailable() {
        LOG.debug("checking for non-existing participants");
        if (!raceLogic.isRunning()) {
            for (Participant participant : participantsRaceList.getParticipants()) {
                try {
                    if (restService.checkAvailability(participant.getIp())) {
                        LOG.debug("participant with chipid " + participant.getChipId() + " found");
                    } else {
                        LOG.info("participant with chipid " + participant.getChipId() + " not found, removing");
                        participantsRaceList.removeParticipant(participant);
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
    
    public RaceStateResult getRaceData() {
        RaceStateResult rsr = new RaceStateResult();
        rsr.setState(raceLogic.getState());
        rsr.setLapData(lapStorage.getLapDataExtended());
        rsr.setToplist(raceLogic.getToplist());
        rsr.setStartTime(raceLogic.getStartTime());
        rsr.setRaceType(getRaceType());
        rsr.setParticipantExtraData(raceLogic.getParticipantExtraData());
        try {
            if (getRaceType() == RaceType.ROUND_BASED) {
                rsr.addTypeSpecific("maxLaps", configService.getNumberOfLaps().toString());
            } else if (getRaceType() == RaceType.FIXED_TIME) {
                rsr.addTypeSpecific("startInterval", configService.getStartInterval().toString());
                rsr.addTypeSpecific("raceDuration", configService.getRaceDuration().toString());
                rsr.addTypeSpecific("overtimeDuration", configService.getOvertimeDuration().toString());
            }
            rsr.addTypeSpecific("preparationTime", configService.getPreparationDuration().toString());
        } catch (ServiceLayerException ex) {
            LOG.error("cannot get data: " + ex.getMessage(), ex);
        }
        return rsr;
    }

}
