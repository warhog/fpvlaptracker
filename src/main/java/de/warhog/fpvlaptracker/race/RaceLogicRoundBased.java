package de.warhog.fpvlaptracker.race;

import de.warhog.fpvlaptracker.controllers.WebSocketController;
import de.warhog.fpvlaptracker.entities.Participant;
import de.warhog.fpvlaptracker.entities.RaceState;
import de.warhog.fpvlaptracker.entities.racedata.LapTimeList;
import de.warhog.fpvlaptracker.service.AudioService;
import de.warhog.fpvlaptracker.service.ConfigService;
import de.warhog.fpvlaptracker.service.ServiceLayerException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RaceLogicRoundBased implements IRaceLogic {

    private static final Logger LOG = LoggerFactory.getLogger(RaceLogicRoundBased.class);

    @Autowired
    AudioService audioService;

    @Autowired
    ParticipantsList participantsList;

    @Autowired
    WebSocketController webSocketController;

    @Autowired
    ConfigService configService;

    Map<Long, LapTimeList> participantData = new HashMap<>();
    RaceState state = RaceState.WAITING;
    Integer numberOfLaps = 10;
    private LocalDateTime startTime = null;
    private Countdown countdownRunnable = null;
    private Thread countdownThread = null;

    class Countdown implements Runnable {

        @Override
        public void run() {
            LOG.debug("entering run()");
            try {
                audioService.speakPleasePrepareForRace();
                Thread.sleep(10000);
                audioService.speakNumberThree();
                Thread.sleep(1000);
                audioService.speakNumberTwo();
                Thread.sleep(1000);
                audioService.speakNumberOne();
                Thread.sleep(1000);
                setState(RaceState.GETREADY);
                audioService.speakGo();
            } catch (InterruptedException ex) {
                LOG.info("interrupted during run");
            }
            LOG.debug("exiting run()");
        }
    }

    @Override
    public void init() {
        LOG.debug("init");
        try {
            numberOfLaps = configService.getNumberOfLaps();
        } catch (ServiceLayerException ex) {
            LOG.debug("cannot get number of laps, staying with default value");
        }
    }

    @Override
    public LocalDateTime getStartTime() {
        return startTime;
    }

    @Override
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    @Override
    public Boolean isRunning() {
        return getState() == RaceState.GETREADY || getState() == RaceState.RUNNING;
    }

    @Override
    public RaceState getState() {
        return state;
    }

    @Override
    public void setState(final RaceState state) {
        this.state = state;
        webSocketController.sendRaceStateChangedMessage(state);
    }

    public void setNumberOfLaps(final Integer numberOfLaps) {
        this.numberOfLaps = numberOfLaps;
    }
    
    public Integer getNumberOfLaps() {
        return this.numberOfLaps;
    }

    @Override
    public void startRace() {
        if (isRunning()) {
            stopRace();
        }
        if (!participantsList.hasParticipants()) {
            throw new IllegalStateException("no participants");
        }
        LOG.info("initialize new race");
        setState(RaceState.PREPARE);
        LOG.info("start race");
        participantData.clear();
        for (Participant participant : participantsList.getParticipants()) {
            participantData.put(participant.getChipId(), new LapTimeList());
        }

        if (countdownRunnable != null) {
            countdownThread.interrupt();
            try {
                LOG.debug("waiting for thread join");
                countdownThread.join();
                LOG.debug("thread joined");
            } catch (InterruptedException ex) {
                LOG.debug("interrupted during join");
            }
            countdownRunnable = null;
            countdownThread = null;
        }
        countdownRunnable = new Countdown();
        countdownThread = new Thread(countdownRunnable);
        countdownThread.start();
    }

    @Override
    public void stopRace() {
        LOG.info("stopping race");
        setState(RaceState.FINISHED);

        if (countdownRunnable != null) {
            countdownThread.interrupt();
            try {
                LOG.debug("waiting for thread join");
                countdownThread.join();
                LOG.debug("thread joined");
            } catch (InterruptedException ex) {
                LOG.debug("interrupted during join");
            }
            countdownRunnable = null;
            countdownThread = null;
        }

    }

    @Override
    public void addLap(Long chipId, Long duration, Integer rssi) {
        LOG.debug("add lap", chipId, duration, rssi);
        if (!participantData.containsKey(chipId)) {
            LOG.info("participant not found: " + chipId);
            return;
        }
        Participant participant = participantsList.getParticipantByChipId(chipId);
        String name = participant.getName();
        
        boolean raceStartedInThisLap = false;
        boolean oneParticipantReachedEnd = false;
        String participantEndedName = "";

        if (!isRunning()) {
            LOG.info("cannot add lap when race is not running, chipid: " + chipId);
            if (participantsList.hasParticipant(chipId)) {
                audioService.speakInvalidLap(name);
            }
            return;
        }
        
        if (getState() == RaceState.GETREADY) {
            LOG.info("starting race");
            setState(RaceState.RUNNING);
            audioService.playStart();
            raceStartedInThisLap = true;
        }
        
        if (!participantData.containsKey(chipId)) {
            throw new RuntimeException("no data for participant found: " + participant.toString());
        }
        
        LapTimeList data = participantData.get(chipId);

        if (data.getCurrentLap() > numberOfLaps) {
            LOG.info("participant already ended race " + name);
            audioService.speakAlreadyDone(name);
        } else {
            if (data.getCurrentLap() > numberOfLaps) {
                LOG.info("participant reached lap limit " + name);
                oneParticipantReachedEnd = true;
                participantEndedName = name;
            } else if (!raceStartedInThisLap) {
                data.addLap(duration, rssi);
                audioService.playLap();
            }

        }

        // test if all of the participants has reached the lap limit
        if (checkEnded()) {
            LOG.info("race ended");
            audioService.speakFinished();
            stopRace();
        } else {
            if (oneParticipantReachedEnd) {
                audioService.speakParticipantEnded(participantEndedName);
            }
        }
    }

    public boolean checkEnded() {
        for (Map.Entry<Long, LapTimeList> entry : participantData.entrySet()) {
            String name = participantsList.getParticipantByChipId(entry.getKey()).getName();
            if (entry.getValue().getCurrentLap() <= numberOfLaps) {
                LOG.debug("participant has not completed yet: " + name + ", numberOfLaps: " + numberOfLaps + ", currentLap: " + entry.getValue().getCurrentLap());
                return false;
            }
        }
        return true;
    }

    @Override
    public LapTimeList getLapData(Long chipId) {
        if (participantData.containsKey(chipId)) {
            return participantData.get(chipId);
        }
        return new LapTimeList();
    }
}
