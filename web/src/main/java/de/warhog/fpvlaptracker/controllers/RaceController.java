package de.warhog.fpvlaptracker.controllers;

import de.warhog.fpvlaptracker.controllers.dtos.ChartResult;
import de.warhog.fpvlaptracker.controllers.dtos.RaceStateResult;
import de.warhog.fpvlaptracker.controllers.dtos.StatusResult;
import de.warhog.fpvlaptracker.entities.Participant;
import de.warhog.fpvlaptracker.race.LapStorage;
import de.warhog.fpvlaptracker.race.RaceLogicHandler;
import de.warhog.fpvlaptracker.service.ParticipantsService;
import de.warhog.fpvlaptracker.race.ParticipantsRaceList;
import de.warhog.fpvlaptracker.race.RaceType;
import java.time.Duration;
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
    private RaceLogicHandler raceLogic;

    @Autowired
    private ParticipantsService participantsDbService;

    @Autowired
    private ParticipantsRaceList participantsList;

    @Autowired
    private LapStorage lapStorage;

    @RequestMapping(path = "/api/auth/race/type", method = RequestMethod.GET)
    public StatusResult type(@RequestParam(name = "type", defaultValue = "ROUND_BASED") RaceType raceType) {
        LOG.debug("requested /api/auth/race/type with raceType: " + raceType);
        raceLogic.init(raceType);
        return new StatusResult(StatusResult.Status.OK);
    }

    @RequestMapping(path = "/api/auth/race/start", method = RequestMethod.GET)
    public StatusResult start() {
        LOG.debug("requested /api/auth/race/start");
        raceLogic.startRace();
        return new StatusResult(StatusResult.Status.OK);
    }

    @RequestMapping(path = "/api/race/chartdata", method = RequestMethod.GET)
    public ChartResult chartdata() {
        ChartResult chartResult = new ChartResult();
        for (Participant participant : participantsList.getParticipants()) {
            chartResult.addParticipant(participant.getChipId(), participant.getName());
            if (participantsList.hasParticipant(participant.getChipId())) {
                for (Map.Entry<Integer, Duration> entry : lapStorage.getLapData(participant.getChipId()).getLapsFilterInvalid().entrySet()) {
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

    @RequestMapping(path = "/api/auth/race/invalidatelap", method = RequestMethod.GET)
    public StatusResult invalidateLap(@RequestParam(name = "chipid", required = true) String chipid, @RequestParam(name = "lap", required = true) String lapString) {
        Long chipId = Long.parseLong(chipid);
        Integer lap = Integer.parseInt(lapString);
        lapStorage.toggleLapValidity(chipId, lap);
        return new StatusResult(StatusResult.Status.OK);
    }

    @RequestMapping(path = "/api/auth/race/invalidatepilot", method = RequestMethod.GET)
    public StatusResult invalidatePilot(@RequestParam(name = "chipid", required = true) String chipid) {
        Long chipId = Long.parseLong(chipid);
        participantsList.invalidatePilot(chipId);
        return new StatusResult(StatusResult.Status.OK);
    }

    @RequestMapping(path = "/api/race/participants", method = RequestMethod.GET)
    public List<Participant> getParticipants() {
        return participantsList.getParticipants();
    }

    @RequestMapping(path = "/api/race/data", method = RequestMethod.GET)
    public RaceStateResult getData() {
        return raceLogic.getRaceData();
    }

}
