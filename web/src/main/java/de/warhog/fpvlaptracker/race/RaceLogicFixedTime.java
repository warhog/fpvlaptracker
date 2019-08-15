package de.warhog.fpvlaptracker.race;

import de.warhog.fpvlaptracker.util.RaceState;
import de.warhog.fpvlaptracker.controllers.WebSocketController;
import de.warhog.fpvlaptracker.entities.Pilot;
import de.warhog.fpvlaptracker.dtos.ToplistEntry;
import de.warhog.fpvlaptracker.service.AudioService;
import de.warhog.fpvlaptracker.service.ConfigService;
import de.warhog.fpvlaptracker.service.LedService;
import de.warhog.fpvlaptracker.service.PilotsService;
import de.warhog.fpvlaptracker.service.ServiceLayerException;
import de.warhog.fpvlaptracker.util.PilotState;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.awt.Color;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
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
public class RaceLogicFixedTime implements IRaceLogic {

    private static final Logger LOG = LoggerFactory.getLogger(RaceLogicFixedTime.class);

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

    private final Map<Pilot, Instant> raceStartTimes = new HashMap<>();
    private RaceState state = RaceState.WAITING;
    private Integer raceDuration = 120;
    private Integer overtimeDuration = 60;
    private Integer preparationDuration = 10;
    private Integer startInterval = 3;
    private Instant nextStart = Instant.MIN;
    private RaceRunnable raceRunnable = null;
    private Thread thread = null;
    private LocalDateTime startTime = null;

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
    public void setState(RaceState state) {
        if (this.state != state) {
            this.state = state;
            webSocketController.sendRaceStateChangedMessage(state);
        }
    }

    @Override
    public List<ToplistEntry> getToplist() {
        List<ToplistEntry> toplist = new ArrayList<>();
        for (Pilot pilot : pilotsService.getPilotsWithNodes()) {
            Integer laps = pilot.getLapTimeList().getTotalLaps();
            Duration duration = pilot.getLapTimeList().getTotalDuration();
            ToplistEntry toplistEntry = new ToplistEntry(pilot.getName(), duration.toMillis(), laps);
            if (laps > 0 && duration != Duration.ZERO && pilot.isReady()) {
                toplist.add(toplistEntry);
            }
        }

        Collections.sort(toplist, new ToplistFixedTimeComparator());
        return toplist;
    }

    @Override
    public Map<String, Long> getDurations() {
        HashMap<String, Long> durations = new HashMap<>();
        for (Pilot pilot : pilotsService.getPilots()) {
            Duration raceDurationLeft = Duration.ZERO;
            if (pilot.isStateLastLap() || pilot.isStateStarted()) {
                if (raceStartTimes.containsKey(pilot)) {
                    Duration currentRaceDuration = Duration.between(raceStartTimes.get(pilot), Instant.now());
                    raceDurationLeft = Duration.ofSeconds(raceDuration).minus(currentRaceDuration);
                    if (raceDurationLeft.plus(Duration.ofSeconds(overtimeDuration)).isNegative()) {
                        raceDurationLeft = Duration.ofSeconds(-overtimeDuration);
                    }
                }
            } else if (pilot.isStateWaitingForFirstPass() || pilot.isStateWaitingForStart()) {
                raceDurationLeft = Duration.ofSeconds(raceDuration);
            }
            durations.put(pilot.getName(), raceDurationLeft.toMillis());
        }
        return durations;
    }

    public class RaceRunnable implements Runnable {

        Boolean run = true;

        public RaceRunnable() {
            LOG.debug("constructing runnable");
        }

        public void stop() {
            LOG.debug("stopping runnable");
            run = false;
        }

        private void testAllFinished() {
            boolean allFinished = true;
            for (Pilot pilot : pilotsService.getPilotsWithNodes()) {
                if (!pilot.isReady()) {
                    continue;
                } else if (!pilot.isStateFinished() && !pilot.isStateInvalid()) {
//                    LOG.debug("pilot " + pilot.getName() + " not finished yet: " + pilot.getState().getText());
                    allFinished = false;
                }
            }
            if (allFinished) {
                LOG.info("all pilots finished, ending race");
                setState(RaceState.FINISHED);
                audioService.speakFinished();
                stopRace();
            }
        }

        private void startNextPilot() {
            for (Pilot pilot : pilotsService.getPilotsWithNodes()) {
                if (!pilot.isReady()) {
                    continue;
                } else if (pilot.isStateWaitingForStart()) {
                    String name = pilot.getName();
                    // found pilot that is waiting for start -> start that pilot
                    LOG.info("starting pilot " + name);
                    pilot.setState(PilotState.WAITING_FOR_FIRST_PASS);
                    audioService.speakPilotStart(name);
                    ledService.countdownColor(Color.GREEN, 1000);
                    break;
                }
            }
        }

        private void testTimeExceeded() {
            for (Pilot pilot : pilotsService.getPilotsWithNodes()) {
                String name = pilot.getName();
                if (!pilot.isReady()) {
                    continue;
                }
                Duration runDuration = Duration.between(raceStartTimes.get(pilot), Instant.now());
//                LOG.debug("run duration " + runDuration.toString());
                if (pilot.isStateFinished() || pilot.isStateInvalid()) {
                    // pilot already ended run
//                    LOG.debug("pilot " + name + " already ended this run: " + pilot.getState());
                } else {
                    // start testing if regular run time for this pilot is exceeded
                    if (runDuration.compareTo(Duration.ofSeconds(raceDuration)) >= 0) {
                        // test if run duration is exceeding run time + over time
                        if (runDuration.compareTo(Duration.ofSeconds(raceDuration + overtimeDuration)) >= 0) {
                            // maximum time (run duration + overtime duration) reached
                            // -> invalid run
                            LOG.info("pilot exceeded run + overtime duration: " + name + ": " + runDuration.toString());
                            pilot.setState(PilotState.INVALID);
                            audioService.speakTimeOverPilot(name);
                            webSocketController.sendReloadRaceData();
                            ledService.countdownColor(Color.RED, 100);
                        } else {
                            // run time + over time not yet exceeded
                            // but run time is exceeded -> every pilot should have been started
                            // or is marked as invalid
                            if (pilot.isStateStarted()) {
                                // pilot started and exceeded run time -> last lap for pilot
                                LOG.info("pilot exceeded run duration, last possible lap now: " + name + ": " + runDuration.toString());
                                LOG.debug("pilot state: " + pilot.getState());
                                LOG.debug("pilot is in started, setting state last_lap");
                                pilot.setState(PilotState.LAST_LAP);
                                webSocketController.sendReloadRaceData();
                                audioService.speakLastLapPilot(name);
                                ledService.countdownColor(Color.BLUE, 100);
                            } else if (pilot.isStateWaitingForFirstPass() || pilot.isStateWaitingForStart()) {
                                // pilot not started yet
                                LOG.info("pilot not started during run time: " + name);
                                audioService.speakTimeOverPilot(name);
                                pilot.setState(PilotState.INVALID);
                                webSocketController.sendReloadRaceData();
                                ledService.countdownColor(Color.RED, 100);
                            }
                        }
                    }
                }
            }
        }

        @Override
        public void run() {
            LOG.debug("entering racerunnable run()");
            while (run) {
                try {
                    if (!allStarted()) {
                        if (Instant.now().isAfter(nextStart)) {
                            LOG.info("start next pilot");
                            // if race not running yet set it to running now
                            if (getState() != RaceState.RUNNING) {
                                setState(RaceState.RUNNING);
                            }
                            // not all pilots have started yet -> start next non started pilot
                            startNextPilot();
                            nextStart = Instant.now().plus(startInterval, ChronoUnit.SECONDS);
                        }
                    }

                    testTimeExceeded();
                    testAllFinished();

                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    run = false;
                    break;
                }
            }
            LOG.debug("exiting run()");
        }
    }

    @Override
    public void init() {
        LOG.debug("init");
        try {
            raceDuration = configService.getRaceDuration();
            overtimeDuration = configService.getOvertimeDuration();
            preparationDuration = configService.getPreparationDuration();
            startInterval = configService.getStartInterval();
        } catch (ServiceLayerException ex) {
            LOG.error("cannot get settings, init with default value: " + ex.getMessage(), ex);
        }
    }

    @Override
    public Map<String, String> getRaceTypeSpecificData() {
        Map<String, String> result = new HashMap<>();
        result.put("raceDuration", this.raceDuration.toString());
        result.put("overtimeDuration", this.overtimeDuration.toString());
        result.put("preparationDuration", this.preparationDuration.toString());
        result.put("startInterval", this.startInterval.toString());
        return result;
    }

    @SuppressFBWarnings(value = "RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE", justification = "complains about null check on thread but whyever thread.interrupt() made thread null during testing from time to ")
    @Override
    public void startRace() {
        LOG.info("start race");
        if (!pilotsService.hasValidPilots()) {
            throw new IllegalStateException("no pilots");
        }
        raceStartTimes.clear();
        for (Pilot pilot : pilotsService.getPilotsWithNodes()) {
            if (pilot.isReady()) {
                raceStartTimes.put(pilot, Instant.now());
            }
        }
        setState(RaceState.PREPARE);
        nextStart = Instant.now().plusSeconds(preparationDuration);
        if (raceRunnable != null) {
            raceRunnable.stop();
            try {
                LOG.debug("waiting for thread join");
                thread.join(200);
                if (thread.isAlive()) {
                    LOG.debug("thread still alive, interrupting");
                    thread.interrupt();
                    LOG.debug("thread interrupted, trying to join again");
                    if (thread != null) {
                        thread.join();
                    }
                }
                LOG.debug("thread joined");
            } catch (InterruptedException ex) {
                LOG.debug("interrupted during join");
            }
            raceRunnable = null;
            thread = null;
        }
        raceRunnable = new RaceRunnable();
        thread = new Thread(raceRunnable, "RaceLogicFixedTimeRunnable");
        thread.start();
        ledService.expandColor(Color.BLUE, 150);
        audioService.speakPleasePrepareForRace();
    }

    private boolean allStarted() {
        boolean ret = true;
        for (Pilot pilot : pilotsService.getPilotsWithNodes()) {
            if (pilot.isReady() && pilot.isStateWaitingForStart()) {
                ret = false;
            }
        }
//        LOG.debug("allStarted() returns: " + ret);
        return ret;
    }

    @Override
    public void stopRace() {
        LOG.info("stopping race");
        setState(RaceState.FINISHED);
        ledService.blinkColor(Color.RED, 1000);
        if (raceRunnable != null) {
            raceRunnable.stop();
            try {
                LOG.debug("waiting for thread join");
                thread.join();
                LOG.debug("thread joined");
            } catch (InterruptedException ex) {
                LOG.debug("interrupted during join");
            }
            raceRunnable = null;
            thread = null;
        }
    }

    @Override
    public void addLap(Long chipId, Long duration, Integer rssi) {
        Pilot pilot = pilotsService.getPilot(chipId);
        String name = pilot.getName();

        if (!pilot.isReady()) {
            pilot.setState(PilotState.INVALID);
            webSocketController.sendAlertMessage(WebSocketController.WarningMessageTypes.INFO, "invalid lap", "invalid lap for pilot " + name, false);
            audioService.speakInvalidLap(name);
            ledService.countdownColor(Color.RED, 100);
            return;
        }

        if (null == pilot.getState()) {
            LOG.error("invalid state: " + pilot.getState().getText());
        } else {
            switch (pilot.getState()) {
                case WAITING_FOR_START:
                    // false start, pilot is not allowed to start yet
                    LOG.error("false start pilot " + name + ", " + pilot.getNode().getChipId());
                    audioService.speakFalseStartPilot(name);
                    pilot.setState(PilotState.INVALID);
                    webSocketController.sendAlertMessage(WebSocketController.WarningMessageTypes.WARNING, "early start", name + " was starting too early!", true);
                    ledService.countdownColor(Color.RED, 1000);
                    break;
                case WAITING_FOR_FIRST_PASS:
                    // is first pass, dont count the lap
                    audioService.playStart();
                    raceStartTimes.put(pilot, Instant.now());
                    pilot.setState(PilotState.STARTED);
                    ledService.countdownColor(Color.GREEN, 100);
                    break;
                case STARTED:
                    // pilot started -> real lap -> add to laptimelist
                    audioService.playLap();
                    pilot.addLap(duration, rssi);
                    ledService.countdownColor(Color.GREEN, 100);
                    break;
                case LAST_LAP:
                    // this was the last lap
                    pilot.setState(PilotState.FINISHED);
                    pilot.addLap(duration, rssi);
                    audioService.speakPilotEnded(name);
                    ledService.countdownColor(Color.BLUE, 1000);
                    break;
                case FINISHED:
                    // pilot already finished the race
                    audioService.speakAlreadyDone(name);
                    ledService.countdownColor(Color.GREEN, 1000);
                    break;
                case INVALID:
                    // invalid lap (e.g. after false start)
                    audioService.speakInvalidLap(name);
                    ledService.countdownColor(Color.RED, 100);
                    break;
                default:
                    LOG.error("invalid state: " + pilot.getState().getText());
                    break;
            }
        }
    }

}
