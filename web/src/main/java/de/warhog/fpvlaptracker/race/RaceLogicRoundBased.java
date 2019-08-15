package de.warhog.fpvlaptracker.race;

import de.warhog.fpvlaptracker.util.RaceState;
import de.warhog.fpvlaptracker.controllers.WebSocketController;
import de.warhog.fpvlaptracker.entities.Pilot;
import de.warhog.fpvlaptracker.entities.LapTimeList;
import de.warhog.fpvlaptracker.dtos.ToplistEntry;
import de.warhog.fpvlaptracker.service.AudioService;
import de.warhog.fpvlaptracker.service.ConfigService;
import de.warhog.fpvlaptracker.service.LedService;
import de.warhog.fpvlaptracker.service.PilotsService;
import de.warhog.fpvlaptracker.service.ServiceLayerException;
import de.warhog.fpvlaptracker.util.PilotState;
import java.awt.Color;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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
    PilotsService pilotsService;

    @Autowired
    WebSocketController webSocketController;

    @Autowired
    ConfigService configService;

    @Autowired
    LedService ledService;

    private RaceState state = RaceState.WAITING;
    private Integer numberOfLaps = 10;
    private Integer preparationDuration = 10;
    private LocalDateTime startTime = null;
    private Countdown countdownRunnable = null;
    private Thread countdownThread = null;

    @Override
    public Map<String, Long> getDurations() {
        return new HashMap<String, Long>();
    }

    class Countdown implements Runnable {

        @Override
        public void run() {
            LOG.debug("entering run()");
            try {
                ledService.expandColor(Color.BLUE, 150);
                audioService.speakPleasePrepareForRace();
                Thread.sleep(preparationDuration * 1000);
                ledService.countdownColor(Color.RED, 500);
                audioService.speakNumberThree();
                Thread.sleep(2000);
                ledService.countdownColor(Color.RED, 500);
                audioService.speakNumberTwo();
                Thread.sleep(2000);
                ledService.countdownColor(Color.RED, 500);
                audioService.speakNumberOne();
                Thread.sleep(2000);
                ledService.countdownColor(Color.GREEN, 5000);
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
            preparationDuration = configService.getPreparationDuration();
        } catch (ServiceLayerException ex) {
            LOG.error("cannot get settings, init with default value: " + ex.getMessage(), ex);
        }
    }
    
    @Override
    public Map<String, String> getRaceTypeSpecificData() {
        Map<String, String> result = new HashMap<>();
        result.put("preparationDuration", this.preparationDuration.toString());
        result.put("numberOfLaps", this.numberOfLaps.toString());
        return result;
    }

    @Override
    public List<ToplistEntry> getToplist() {
        List<ToplistEntry> toplist = new ArrayList<>();
        for (Pilot pilot : pilotsService.getPilotsWithNodes()) {
            if (pilot.isReady()) {
                Integer laps = pilot.getLapTimeList().getTotalLaps();
                Duration duration = pilot.getLapTimeList().getTotalDuration();
                ToplistEntry toplistEntry = new ToplistEntry(pilot.getName(), duration.toMillis(), laps);
                if (laps > 0 && duration != Duration.ZERO && pilot.isReady()) {
                    toplist.add(toplistEntry);
                }
            }
        }
        Collections.sort(toplist, new ToplistRoundBasedComparator());
        return toplist;
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
        if (!pilotsService.hasValidPilots()) {
            throw new IllegalStateException("no pilots");
        }
        LOG.info("initialize new race");
        setState(RaceState.PREPARE);
        LOG.info("start race");

        stopCountdownThread();
        countdownRunnable = new Countdown();
        countdownThread = new Thread(countdownRunnable, "RaceLogicRoundBasedCountdown");
        countdownThread.start();
    }

    @Override
    public void stopRace() {
        LOG.info("stopping race");
        setState(RaceState.FINISHED);
        stopCountdownThread();
        ledService.blinkColor(Color.RED, 1000);
    }

    private void stopCountdownThread() {
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
        LOG.debug("add lap, " + chipId + ", " + duration + ", " + rssi);
        Pilot pilot = pilotsService.getPilot(chipId);
        String name = pilot.getName();

        boolean raceStartedInThisLap = false;
        boolean onePilotReachedEnd = false;
        String pilotEndedName = "";

        if (getState() == RaceState.PREPARE) {
            LOG.info("false start chipid: " + chipId);
            audioService.speakFalseStartPilot(name);
            audioService.speakRaceAborted();
            webSocketController.sendAlertMessage(WebSocketController.WarningMessageTypes.WARNING, "early start", name + " was starting too early!", true);
            setState(RaceState.FAULT);
            stopCountdownThread();
            ledService.blinkColor(Color.RED, 250);
            pilot.setValid(false);
            return;
        }

        if (!pilot.isReady()) {
            webSocketController.sendAlertMessage(WebSocketController.WarningMessageTypes.INFO, "invalid lap", "invalid lap for pilot " + name, false);
            audioService.speakInvalidLap(name);
            ledService.countdownColor(Color.RED, 100);
            return;
        }

        if (!isRunning()) {
            LOG.info("cannot add lap when race is not running, pilot: " + pilot.getName());
            webSocketController.sendAlertMessage(WebSocketController.WarningMessageTypes.INFO, "invalid lap", "invalid lap for pilot " + name, false);
            audioService.speakInvalidLap(name);
            ledService.countdownColor(Color.RED, 100);
            return;
        }

        if (getState() == RaceState.GETREADY) {
            LOG.info("starting race");
            setState(RaceState.RUNNING);
            audioService.playStart();
            raceStartedInThisLap = true;
            ledService.countdownColor(Color.GREEN, 100);
            pilot.setState(PilotState.WAITING_FOR_FIRST_PASS);
        }

        LapTimeList lapTimeList = pilot.getLapTimeList();
        Integer currentLap = lapTimeList.getCurrentLap();
        if (currentLap > numberOfLaps) {
            LOG.info("pilot already ended race " + name);
            audioService.speakAlreadyDone(name);
            ledService.countdownColor(Color.RED, 100);
        } else {
            lapTimeList.addLap(duration, rssi, true);
            if (currentLap >= numberOfLaps) {
                LOG.info("pilot reached lap limit " + name);
                onePilotReachedEnd = true;
                pilotEndedName = name;
                pilot.setState(PilotState.FINISHED);
            } else if (!raceStartedInThisLap) {
                audioService.playLap();
                ledService.countdownColor(Color.GREEN, 100);
                pilot.setState(PilotState.STARTED);
            }
        }

        // test if all of the pilots has reached the lap limit
        if (checkEnded()) {
            LOG.info("race ended");
            audioService.speakFinished();
            stopRace();
        } else {
            if (onePilotReachedEnd) {
                audioService.speakPilotEnded(pilotEndedName);
            }
        }
    }

    public boolean checkEnded() {
        for (Pilot pilot : pilotsService.getPilotsWithNodes()) {
            if (!pilot.isReady()) {
                continue;
            }
            String name = pilot.getName();
            LapTimeList lapTimeList = pilot.getLapTimeList();
            if (lapTimeList.getCurrentLap() <= numberOfLaps) {
                LOG.debug("pilot has not completed yet: " + name + ", numberOfLaps: " + numberOfLaps + ", currentLap: " + lapTimeList.getCurrentLap());
                return false;
            }
        }
        return true;
    }

}
