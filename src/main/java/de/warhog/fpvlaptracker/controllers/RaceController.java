package de.warhog.fpvlaptracker.controllers;

import de.warhog.fpvlaptracker.controllers.dtos.ChartResult;
import de.warhog.fpvlaptracker.controllers.dtos.RaceStateResult;
import de.warhog.fpvlaptracker.controllers.dtos.StatusResult;
import de.warhog.fpvlaptracker.race.entities.Participant;
import de.warhog.fpvlaptracker.race.RaceLogic;
import de.warhog.fpvlaptracker.race.entities.ParticipantRaceData;
import de.warhog.fpvlaptracker.service.ConfigService;
import de.warhog.fpvlaptracker.service.ParticipantsDbService;
import de.warhog.fpvlaptracker.service.ParticipantRaceService;
import de.warhog.fpvlaptracker.service.ServiceLayerException;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RaceController {

    private static final Logger LOG = LoggerFactory.getLogger(RaceController.class);

    @Autowired
    private RaceLogic raceLogic;

    @Autowired
    private ConfigService configService;

    @Autowired
    private ParticipantsDbService participantsDbService;
    
    @Autowired
    private ParticipantRaceService participantRaceService;

    @RequestMapping(path = "/api/auth/race/maxlaps", method = RequestMethod.POST)
    public StatusResult setMaxLaps(@RequestParam(name = "laps", defaultValue = "10") Integer laps) {
        raceLogic.setNumberOfLaps(laps);
        try {
            configService.setNumberOfLaps(laps);
        } catch (ServiceLayerException ex) {
            LOG.debug("cannot store number of laps", ex);
        }
        return new StatusResult(StatusResult.Status.OK);
    }

    @RequestMapping(path = "/api/auth/race/start", method = RequestMethod.GET)
    public StatusResult start(@RequestParam(name = "laps", defaultValue = "10") Integer laps) {
        raceLogic.setNumberOfLaps(laps);
        raceLogic.initializeNewRace();
        return new StatusResult(StatusResult.Status.OK);
    }

    @RequestMapping(path = "/api/race/chartdata", method = RequestMethod.GET)
    public ChartResult chartdata() {
        ChartResult chartResult = new ChartResult();
        List<Participant> participants = participantsDbService.getAllParticipants();
        for (Participant participant : participants) {
            chartResult.addParticipant(participant.getChipId(), participant.getName());
            if (participantRaceService.hasParticipant(participant.getChipId())) {
                Map<Integer, Duration> laps = participantRaceService.getParticipantLapTimes(participant);
                for (Map.Entry<Integer, Duration> entry : laps.entrySet()) {
                    chartResult.addLapTimes(entry.getKey(), participant.getChipId(), entry.getValue());
                }
            }
        }
        return chartResult;
    }

    @RequestMapping(path = "/api/auth/race/stop", method = RequestMethod.GET)
    public StatusResult stop() {
        raceLogic.stopRace();
        return new StatusResult(StatusResult.Status.OK);
    }

    @RequestMapping(path = "/api/auth/race/participants/add", method = RequestMethod.GET)
    public StatusResult addParticipant(@RequestParam(name = "chipid", required = true) String chipid) {
        Long chipId = Long.parseLong(chipid);
        Participant participant = participantsDbService.getParticipant(chipId);
        raceLogic.addParticipant(participant);
        return new StatusResult(StatusResult.Status.OK);
    }

    @RequestMapping(path = "/api/auth/race/participants/remove", method = RequestMethod.GET)
    public StatusResult removeParticipant(@RequestParam(name = "chipid", required = true) String chipid) {
        Long chipId = Long.parseLong(chipid);
        Participant participant = participantsDbService.getParticipant(chipId);
        raceLogic.removeParticipant(participant);
        return new StatusResult(StatusResult.Status.OK);
    }

    @RequestMapping(path = "/api/race/participants", method = RequestMethod.GET)
    public List<Participant> getParticipants() {
        return participantRaceService.getParticipants();
    }

    @RequestMapping(path = "/api/race/state", method = RequestMethod.GET)
    public RaceStateResult getState() {
        
        HashMap<Participant, ParticipantRaceData> data = new HashMap<>();
        for (Participant participant : participantRaceService.getParticipants()) {
            data.put(participant, participantRaceService.getParticipantRaceData(participant));
        }
        RaceStateResult rsr = new RaceStateResult();
        rsr.setState(raceLogic.getState());
        rsr.setRaceData(data);
        rsr.setStartTime(raceLogic.getStartTime());
        rsr.setMaxLaps(raceLogic.getNumberOfLaps());

        return rsr;
    }

}
