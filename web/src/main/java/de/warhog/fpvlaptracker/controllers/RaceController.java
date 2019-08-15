package de.warhog.fpvlaptracker.controllers;

import de.warhog.fpvlaptracker.dtos.ChartResult;
import de.warhog.fpvlaptracker.dtos.RaceDataResult;
import de.warhog.fpvlaptracker.dtos.StatusResult;
import de.warhog.fpvlaptracker.entities.LapTimeListLap;
import de.warhog.fpvlaptracker.entities.Pilot;
import de.warhog.fpvlaptracker.race.RaceLogicHandler;
import de.warhog.fpvlaptracker.service.PilotsService;
import de.warhog.fpvlaptracker.util.RaceType;
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
    private PilotsService pilotsService;

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
        for (Pilot pilot : pilotsService.getPilotsWithNodes()) {
            chartResult.addPilot(pilot.getName());
            for (LapTimeListLap lap : pilot.getLapTimeList().getLaps()) {
                chartResult.addLapTimes(pilot.getName(), lap.getLap(), lap.getDuration());
            }
        }
        return chartResult;
    }

    @RequestMapping(path = "/api/auth/race/stop", method = RequestMethod.GET)
    public StatusResult stop() {
        raceLogic.stopRace();
        return new StatusResult(StatusResult.Status.OK);
    }

    @RequestMapping(path = "/api/auth/race/lap/valid", method = RequestMethod.GET)
    public StatusResult setLapValid(@RequestParam(name = "name", required = true) String name, @RequestParam(name = "lap", required = true) String lapString, @RequestParam(name = "valid", defaultValue = "true") String validRaw) {
        boolean valid = true;
        if (validRaw.equals("false")) {
            valid = false;
        }
        Integer lap = Integer.parseInt(lapString);
        Pilot pilot = pilotsService.getPilot(name);
        pilot.setLapValid(lap, valid);
        return new StatusResult(StatusResult.Status.OK);
    }

    @RequestMapping(path = "/api/auth/race/pilot/valid", method = RequestMethod.GET)
    public StatusResult setPilotValid(@RequestParam(name = "name", required = true) String name, @RequestParam(name = "valid", defaultValue = "true") String validRaw) {
        boolean valid = true;
        if (validRaw.equals("false")) {
            valid = false;
        }
        pilotsService.setPilotValid(name, valid);
        return new StatusResult(StatusResult.Status.OK);
    }

    @RequestMapping(path = "/api/race/data", method = RequestMethod.GET)
    public RaceDataResult getData() {
        return raceLogic.getRaceData();
    }

}
