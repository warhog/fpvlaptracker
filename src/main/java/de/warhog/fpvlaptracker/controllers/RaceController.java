package de.warhog.fpvlaptracker.controllers;

import de.warhog.fpvlaptracker.controllers.dtos.ChartResult;
import de.warhog.fpvlaptracker.controllers.dtos.RaceStateResult;
import de.warhog.fpvlaptracker.controllers.dtos.StatusResult;
import de.warhog.fpvlaptracker.race.entities.Participant;
import de.warhog.fpvlaptracker.race.RaceLogic;
import de.warhog.fpvlaptracker.service.ConfigService;
import de.warhog.fpvlaptracker.service.ParticipantsService;
import de.warhog.fpvlaptracker.service.ServiceLayerException;
import java.time.Duration;
import java.util.ArrayList;
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
    private RaceLogic race;

    @Autowired
    private ConfigService configService;

    @Autowired
    private ParticipantsService participantsService;

    @RequestMapping(path = "/api/auth/race/maxlaps", method = RequestMethod.POST)
    public StatusResult setMaxLaps(@RequestParam(name = "laps", defaultValue = "10") Integer laps) {
        race.setNumberOfLaps(laps);
        try {
            configService.setNumberOfLaps(laps);
        } catch (ServiceLayerException ex) {
            LOG.debug("cannot store number of laps", ex);
        }
        return new StatusResult(StatusResult.Status.OK);
    }

    @RequestMapping(path = "/api/auth/race/start", method = RequestMethod.GET)
    public StatusResult start(@RequestParam(name = "laps", defaultValue = "10") Integer laps) {
        race.setNumberOfLaps(laps);
        race.initializeNewRace();
        return new StatusResult(StatusResult.Status.OK);
    }

    @RequestMapping(path = "/api/race/chartdata", method = RequestMethod.GET)
    public ChartResult chartdata() {
        ChartResult chartResult = new ChartResult();
        List<Participant> participants = participantsService.getAllParticipants();
        for (Participant participant : participants) {
            chartResult.addParticipant(participant.getChipId(), participant.getName());
            if (race.hasParticipant(participant.getChipId())) {
                Map<Integer, Duration> laps = race.getParticipantLapTimes(participant);
                for (Map.Entry<Integer, Duration> entry : laps.entrySet()) {
                    chartResult.addLapTimes(entry.getKey(), participant.getChipId(), entry.getValue());
                }
            }
        }
        return chartResult;
    }

    @RequestMapping(path = "/api/auth/race/stop", method = RequestMethod.GET)
    public StatusResult stop() {
        race.stopRace();
        return new StatusResult(StatusResult.Status.OK);
    }

    @RequestMapping(path = "/api/auth/race/participants/add", method = RequestMethod.GET)
    public StatusResult addParticipant(@RequestParam(name = "chipid", required = true) String chipid) {
        Integer chipId = Integer.parseInt(chipid);
        Participant participant = participantsService.getParticipant(chipId);
        race.addParticipant(participant);
        return new StatusResult(StatusResult.Status.OK);
    }

    @RequestMapping(path = "/api/auth/race/participants/remove", method = RequestMethod.GET)
    public StatusResult removeParticipant(@RequestParam(name = "chipid", required = true) String chipid) {
        Integer chipId = Integer.parseInt(chipid);
        Participant participant = participantsService.getParticipant(chipId);
        race.removeParticipant(participant);
        return new StatusResult(StatusResult.Status.OK);
    }

    private List<Participant> getRaceParticipants() {
        List<Participant> ret = new ArrayList<>();
        race.getParticipants().entrySet().forEach((entry) -> {
            ret.add(entry.getKey());
        });
        return ret;
    }

    @RequestMapping(path = "/api/race/participants", method = RequestMethod.GET)
    public List<Participant> getParticipants() {
        return getRaceParticipants();
    }

    @RequestMapping(path = "/api/race/state", method = RequestMethod.GET)
    public RaceStateResult getState() {
        RaceStateResult rsr = new RaceStateResult();
        rsr.setState(race.getState());
        rsr.setRaceData(race.getLaps());
        rsr.setStartTime(race.getStartTime());
        rsr.setMaxLaps(race.getNumberOfLaps());

        return rsr;
    }

}
