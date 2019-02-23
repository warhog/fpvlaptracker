package de.warhog.fpvlaptracker.race;

import de.warhog.fpvlaptracker.entities.Participant;
import de.warhog.fpvlaptracker.entities.RaceState;
import de.warhog.fpvlaptracker.entities.racedata.FixedTimeRaceParticipantData;
import de.warhog.fpvlaptracker.service.AudioService;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
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
    ParticipantsList participantsList;

    Map<Long, FixedTimeRaceParticipantData> participantData = new HashMap<>();
    Map<Long, Instant> raceTimes = new HashMap<>();
    RaceState state = RaceState.WAITING;
    Integer maxRunDuration = 120;
    Integer maxOverDuration = 60;
    Integer startTime = 10;
    Integer startInterval = 2;
    Instant nextStart = Instant.MIN;
    RaceRunnable raceRunnable = null;
    Thread thread = null;

    public class RaceRunnable implements Runnable {

        Boolean run = true;

        public RaceRunnable() {
            LOG.debug("constructing runnable");
        }

        public void stop() {
            LOG.debug("stopping runnable");
            run = false;
        }

        private void startNextParticipant() {
            for (Map.Entry<Long, FixedTimeRaceParticipantData> entry : participantData.entrySet()) {
                if (entry.getValue().isStateWaitingForStart()) {
                    String name = participantsList.getParticipantByChipId(entry.getKey()).getName();
                    // found participant that is waiting for start -> start that participant
                    LOG.info("starting participant " + name);
                    entry.getValue().setState(FixedTimeRaceParticipantData.ParticipantState.WAITING_FOR_FIRST_PASS);
                    audioService.speakParticipantStart(name);
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
                            // not all participants have started yet -> start next non started participant
                            startNextParticipant();
                            nextStart = Instant.now().plus(startInterval, ChronoUnit.SECONDS);
                        }
                    }

                    for (Map.Entry<Long, FixedTimeRaceParticipantData> entry : participantData.entrySet()) {
                        // get race duration for participant
                        String name = participantsList.getParticipantByChipId(entry.getKey()).getName();
                        Duration runDuration = Duration.between(raceTimes.get(entry.getKey()), Instant.now());
                        LOG.debug("run duration " + runDuration.toString());
                        if (!entry.getValue().isStateFinished() && !entry.getValue().isStateInvalid()) {
                            if (runDuration.compareTo(Duration.ofSeconds(maxRunDuration + maxOverDuration)) >= 0) {
                                // maximum time (run duration + overtime duration) reached
                                LOG.info("participant exceeded run + overtime duration: " + name + ": " + runDuration.toString());
                                entry.getValue().setState(FixedTimeRaceParticipantData.ParticipantState.INVALID);
                                audioService.speakTimeOverParticipant(name);
                            }
                        } else if (entry.getValue().isStateStarted()) {
                            if (runDuration.compareTo(Duration.ofSeconds(maxRunDuration)) >= 0) {
                                // player exceeded run duration -> last lap for participant
                                LOG.info("participant exceeded run duration: " + name + ": " + runDuration.toString());
                                LOG.debug("participant state: " + entry.getValue().getState());
                                LOG.debug("participant is in started, setting state last_lap");
                                entry.getValue().setState(FixedTimeRaceParticipantData.ParticipantState.LAST_LAP);
                                audioService.speakLastLapParticipant(name);
                            }
                        }
                    }
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    run = false;
                    break;
                }
            }
        }
    }

    @Override
    public void init() {
        LOG.debug("init");
    }

    public void setParameters(Integer maxRunDuration, Integer maxOverDuration) {
        LOG.debug("setParameters with runtime " + maxRunDuration + " and overtime " + maxOverDuration);
        this.maxRunDuration = maxRunDuration;
        this.maxOverDuration = maxOverDuration;
    }

    @Override
    public void startRace() {
        LOG.info("start race");
        participantData.clear();
        raceTimes.clear();
        for (Participant participant : participantsList.getParticipants()) {
            participantData.put(participant.getChipId(), new FixedTimeRaceParticipantData());
            raceTimes.put(participant.getChipId(), Instant.now());
        }
        state = RaceState.RUNNING;
        if (raceRunnable != null) {
            raceRunnable.stop();
            try {
                thread.join();
            } catch (InterruptedException ex) {
                LOG.debug("interrupted during join");
            }
            raceRunnable = null;
            thread = null;
        }
        raceRunnable = new RaceRunnable();
        thread = new Thread(raceRunnable);
        thread.start();
        nextStart = Instant.now().plus(startTime, ChronoUnit.SECONDS);
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
        if (raceRunnable != null) {
            raceRunnable.stop();
        }
    }

    @Override
    public void addLap(Long chipId, Long duration, Integer rssi) {
        if (participantData.containsKey(chipId)) {
            FixedTimeRaceParticipantData data = participantData.get(chipId);
            Participant participant = participantsList.getParticipantByChipId(chipId);
            String name = participant.getName();
            if (null == data.getState()) {
                LOG.error("invalid state: " + data.getState().toString());
            } else {
                switch (data.getState()) {
                    case WAITING_FOR_START:
                        // false start, participant is not allowed to start yet
                        LOG.error("false start participant " + name + ", " + participant.getChipId());
                        audioService.speakFalseStartParticipant(name);
                        data.setState(FixedTimeRaceParticipantData.ParticipantState.INVALID);
                        break;
                    case WAITING_FOR_FIRST_PASS:
                        // is first pass, dont count the lap
                        audioService.playLap();
                        raceTimes.put(chipId, Instant.now());
                        data.setState(FixedTimeRaceParticipantData.ParticipantState.STARTED);
                        break;
                    case STARTED:
                        // participant started -> real lap -> add to laptimelist
                        audioService.playLap();
                        participantData.get(chipId).getLapTimeList().addLap(duration, rssi);
                        break;
                    case LAST_LAP:
                        // this was the last lap
                        data.setState(FixedTimeRaceParticipantData.ParticipantState.FINISHED);
                        participantData.get(chipId).getLapTimeList().addLap(duration, rssi);
                        audioService.speakParticipantEnded(name);
                    case FINISHED:
                        // participant already finished the race
                        audioService.speakAlreadyDone(name);
                        break;
                    case INVALID:
                        // invalid lap (e.g. after false start)
                        audioService.speakInvalidLap(name);
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
