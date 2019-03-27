package de.warhog.fpvlaptracker.race;

import de.warhog.fpvlaptracker.controllers.WebSocketController;
import de.warhog.fpvlaptracker.entities.Participant;
import de.warhog.fpvlaptracker.entities.RaceState;
import de.warhog.fpvlaptracker.entities.FixedTimeRaceParticipantData;
import de.warhog.fpvlaptracker.entities.ParticipantExtraData;
import de.warhog.fpvlaptracker.entities.ToplistEntry;
import de.warhog.fpvlaptracker.service.AudioService;
import de.warhog.fpvlaptracker.service.ConfigService;
import de.warhog.fpvlaptracker.service.LedService;
import de.warhog.fpvlaptracker.service.ServiceLayerException;
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
    ParticipantsRaceList participantsRaceList;

    @Autowired
    WebSocketController webSocketController;

    @Autowired
    LapStorage lapStorage;

    @Autowired
    ConfigService configService;

    @Autowired
    LedService ledService;

    private final Map<Long, FixedTimeRaceParticipantData> participantData = new HashMap<>();
    private final Map<Long, Instant> raceTimes = new HashMap<>();
    private RaceState state = RaceState.WAITING;
    private Integer raceDuration = 120;
    private Integer overtimeDuration = 60;
    private Integer startPreparationDuration = 10;
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
        this.state = state;
        webSocketController.sendRaceStateChangedMessage(state);
    }

    @Override
    public List<ToplistEntry> getToplist() {
        List<ToplistEntry> toplist = new ArrayList<>();
        for (Participant participant : participantsRaceList.getParticipants()) {
            Integer laps = lapStorage.getLapData(participant.getChipId()).getTotalLaps();
            Duration duration = lapStorage.getLapData(participant.getChipId()).getTotalDuration();
            ToplistEntry toplistEntry = new ToplistEntry(participant.getName(), duration.toMillis(), laps);
            if (laps > 0 && duration != Duration.ZERO && participant.isValid()) {
                toplist.add(toplistEntry);
            }
        }

        Collections.sort(toplist, new ToplistFixedTimeComparator());
        return toplist;
    }

    @Override
    public Map<Long, ParticipantExtraData> getParticipantExtraData() {
        HashMap<Long, ParticipantExtraData> participantsExtraData = new HashMap<>();
        for (Participant participant : participantsRaceList.getParticipants()) {
            Long chipId = participant.getChipId();
            ParticipantExtraData participantExtraData = new ParticipantExtraData();
            if (participantData.containsKey(chipId)) {
                FixedTimeRaceParticipantData data = participantData.get(chipId);
                participantExtraData.setState(data.getState().getText());
                if (data.isStateLastLap() || data.isStateStarted()) {
                    if (raceTimes.containsKey(chipId)) {
                        Duration currentRaceDuration = Duration.between(raceTimes.get(chipId), Instant.now());
                        Duration raceDurationLeft = Duration.ofSeconds(raceDuration).minus(currentRaceDuration);
                        if (raceDurationLeft.plus(Duration.ofSeconds(overtimeDuration)).isNegative()) {
                            raceDurationLeft = Duration.ofSeconds(-overtimeDuration);
                        }
                        participantExtraData.setDuration(raceDurationLeft);
                    }
                } else if (data.isStateWaitingForFirstPass() || data.isStateWaitingForStart()) {
                    participantExtraData.setDuration(Duration.ofSeconds(raceDuration));
                }
            }
            participantsExtraData.put(chipId, participantExtraData);
        }
        return participantsExtraData;
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
            for (Map.Entry<Long, FixedTimeRaceParticipantData> entry : participantData.entrySet()) {
                if (!participantsRaceList.getParticipantByChipId(entry.getKey()).isValid()) {
                    continue;
                }
                if (!entry.getValue().isStateFinished() && !entry.getValue().isStateInvalid()) {
//                    LOG.debug("participant " + participantsRaceList.getParticipantByChipId(entry.getKey()).getName() + " not finished yet: " + entry.getValue().getState());
                    allFinished = false;
                }
            }
            if (allFinished) {
                LOG.info("all participants finished, ending race");
                setState(RaceState.FINISHED);
                audioService.speakFinished();
                stopRace();
            }
        }

        private void startNextParticipant() {
            for (Map.Entry<Long, FixedTimeRaceParticipantData> entry : participantData.entrySet()) {
                if (entry.getValue().isStateWaitingForStart()) {
                    String name = participantsRaceList.getParticipantByChipId(entry.getKey()).getName();
                    // found participant that is waiting for start -> start that participant
                    LOG.info("starting participant " + name);
                    entry.getValue().setState(FixedTimeRaceParticipantData.ParticipantState.WAITING_FOR_FIRST_PASS);
                    audioService.speakParticipantStart(name);
                    ledService.countdownColor(Color.GREEN, 1000);
                    break;
                }
            }
        }

        private void testTimeExceeded() {
            for (Map.Entry<Long, FixedTimeRaceParticipantData> entry : participantData.entrySet()) {
                // get race duration for participant
                String name = participantsRaceList.getParticipantByChipId(entry.getKey()).getName();
                Duration runDuration = Duration.between(raceTimes.get(entry.getKey()), Instant.now());
//                LOG.debug("run duration " + runDuration.toString());
                if (entry.getValue().isStateFinished() || entry.getValue().isStateInvalid()) {
                    // participant already ended run
//                    LOG.debug("participant " + name + " already ended this run: " + entry.getValue().getState());
                } else {
                    // start testing if regular run time for this participant is exceeded
                    if (runDuration.compareTo(Duration.ofSeconds(raceDuration)) >= 0) {
                        // test if run duration is exceeding run time + over time
                        if (runDuration.compareTo(Duration.ofSeconds(raceDuration + overtimeDuration)) >= 0) {
                            // maximum time (run duration + overtime duration) reached
                            // -> invalid run
                            LOG.info("participant exceeded run + overtime duration: " + name + ": " + runDuration.toString());
                            entry.getValue().setState(FixedTimeRaceParticipantData.ParticipantState.INVALID);
                            audioService.speakTimeOverParticipant(name);
                            ledService.countdownColor(Color.RED, 100);
                        } else {
                            // run time + over time not yet exceeded
                            // but run time is exceeded -> every participant should have been started
                            // or is marked as invalid
                            if (entry.getValue().isStateStarted()) {
                                // player started and exceeded run time -> last lap for participant
                                LOG.info("participant exceeded run duration, last possible lap now: " + name + ": " + runDuration.toString());
                                LOG.debug("participant state: " + entry.getValue().getState());
                                LOG.debug("participant is in started, setting state last_lap");
                                entry.getValue().setState(FixedTimeRaceParticipantData.ParticipantState.LAST_LAP);
                                audioService.speakLastLapParticipant(name);
                                ledService.countdownColor(Color.BLUE, 100);
                            } else if (entry.getValue().isStateWaitingForFirstPass() || entry.getValue().isStateWaitingForStart()) {
                                // participant not started yet
                                LOG.info("participant not started during run time: " + name);
                                audioService.speakTimeOverParticipant(name);
                                entry.getValue().setState(FixedTimeRaceParticipantData.ParticipantState.INVALID);
                                ledService.countdownColor(Color.RED, 100);
                            }
                        }
                    }
                }
            }
        }

        @Override
        public void run() {
            LOG.debug("entering runnable run()");
            while (run) {
                try {
                    if (!allStarted()) {
                        if (Instant.now().isAfter(nextStart)) {
                            LOG.info("start next participant");
                            // if race not running yet set it to running now
                            if (getState() != RaceState.RUNNING) {
                                setState(RaceState.RUNNING);
                            }
                            // not all participants have started yet -> start next non started participant
                            startNextParticipant();
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
            startPreparationDuration = configService.getPreparationDuration();
            startInterval = configService.getStartInterval();
        } catch (ServiceLayerException ex) {
            LOG.error("cannot get settings, init with default value: " + ex.getMessage(), ex);
        }
    }

    @Override
    public void startRace() {
        LOG.info("start race");
        if (!participantsRaceList.hasParticipants()) {
            throw new IllegalStateException("no participants");
        }
        participantData.clear();
        raceTimes.clear();
        for (Participant participant : participantsRaceList.getParticipants()) {
            participantData.put(participant.getChipId(), new FixedTimeRaceParticipantData());
            raceTimes.put(participant.getChipId(), Instant.now());
        }
        setState(RaceState.PREPARE);
        nextStart = Instant.now().plusSeconds(startPreparationDuration);
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
        thread = new Thread(raceRunnable);
        thread.start();
        ledService.expandColor(Color.BLUE, 150);
        audioService.speakPleasePrepareForRace();
    }

    private boolean allStarted() {
        boolean ret = true;
        for (Map.Entry<Long, FixedTimeRaceParticipantData> entry : participantData.entrySet()) {
            if (entry.getValue().isStateWaitingForStart()) {
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
        for (Map.Entry<Long, FixedTimeRaceParticipantData> entry : participantData.entrySet()) {
            entry.getValue().setState(FixedTimeRaceParticipantData.ParticipantState.FINISHED);
        }
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
        if (participantData.containsKey(chipId)) {
            FixedTimeRaceParticipantData data = participantData.get(chipId);
            Participant participant = participantsRaceList.getParticipantByChipId(chipId);
            String name = participant.getName();

            if (!participant.isValid()) {
                data.setState(FixedTimeRaceParticipantData.ParticipantState.INVALID);
                webSocketController.sendAlertMessage(WebSocketController.WarningMessageTypes.INFO, "invalid lap", "invalid lap for pilot " + name, false);
                audioService.speakInvalidLap(name);
                ledService.countdownColor(Color.RED, 100);
                return;
            }

            if (null == data.getState()) {
                LOG.error("invalid state: " + data.getState().toString());
            } else {
                switch (data.getState()) {
                    case WAITING_FOR_START:
                        // false start, participant is not allowed to start yet
                        LOG.error("false start participant " + name + ", " + participant.getChipId());
                        audioService.speakFalseStartParticipant(name);
                        data.setState(FixedTimeRaceParticipantData.ParticipantState.INVALID);
                        webSocketController.sendAlertMessage(WebSocketController.WarningMessageTypes.WARNING, "early start", name + " was starting too early!", true);
                        ledService.countdownColor(Color.RED, 1000);
                        break;
                    case WAITING_FOR_FIRST_PASS:
                        // is first pass, dont count the lap
                        audioService.playStart();
                        raceTimes.put(chipId, Instant.now());
                        data.setState(FixedTimeRaceParticipantData.ParticipantState.STARTED);
                        webSocketController.sendNewLapMessage(chipId);
                        ledService.countdownColor(Color.GREEN, 100);
                        break;
                    case STARTED:
                        // participant started -> real lap -> add to laptimelist
                        audioService.playLap();
                        lapStorage.addLap(chipId, duration, rssi);
                        webSocketController.sendNewLapMessage(chipId);
                        ledService.countdownColor(Color.GREEN, 100);
                        break;
                    case LAST_LAP:
                        // this was the last lap
                        data.setState(FixedTimeRaceParticipantData.ParticipantState.FINISHED);
                        lapStorage.addLap(chipId, duration, rssi);
                        audioService.speakParticipantEnded(name);
                        webSocketController.sendNewLapMessage(chipId);
                        ledService.countdownColor(Color.BLUE, 1000);
                        break;
                    case FINISHED:
                        // participant already finished the race
                        audioService.speakAlreadyDone(name);
                        ledService.countdownColor(Color.GREEN, 1000);
                        break;
                    case INVALID:
                        // invalid lap (e.g. after false start)
                        audioService.speakInvalidLap(name);
                        ledService.countdownColor(Color.RED, 100);
                        break;
                    default:
                        LOG.error("invalid state: " + data.getState().toString());
                        break;
                }
            }
        } else {
            LOG.error("cannot find chipid " + chipId + " in participants");
        }
    }

}
