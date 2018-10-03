package de.warhog.fpvlaptracker.controllers;

import de.warhog.fpvlaptracker.controllers.dtos.RaceStateResult;
import de.warhog.fpvlaptracker.controllers.dtos.ToplistResult;
import de.warhog.fpvlaptracker.jooq.tables.records.LapsRecord;
import de.warhog.fpvlaptracker.jooq.tables.records.RacesRecord;
import de.warhog.fpvlaptracker.entities.Participant;
import de.warhog.fpvlaptracker.entities.ParticipantRaceData;
import de.warhog.fpvlaptracker.service.ConfigService;
import de.warhog.fpvlaptracker.service.RaceDbService;
import de.warhog.fpvlaptracker.service.ServiceLayerException;
import de.warhog.fpvlaptracker.util.TimeUtil;
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
public class RacesController {

    private static final Logger LOG = LoggerFactory.getLogger(RacesController.class);

    @Autowired
    private RaceDbService raceDbService;

    @Autowired
    private ConfigService configService;

    @Autowired
    private TimeUtil timeUtil;

    @RequestMapping(path = "/api/races/all", method = RequestMethod.GET)
    public Map<Integer, String> getRaces() {
        try {
            List<RacesRecord> races = raceDbService.getRaces();
            Map<Integer, String> ret = new HashMap<>();
            for (RacesRecord racesRecord : races) {
                if (racesRecord.getStarttime() != null) {
                    ret.put(racesRecord.getId(), timeUtil.getUnixTimestampInIsoFormat(racesRecord.getStarttime()));
                } else {
                    ret.put(racesRecord.getId(), "-");
                }
            }
            return ret;
        } catch (ServiceLayerException ex) {
            LOG.error(ex.getMessage(), ex);
            throw new RuntimeException("cannot get races");
        }
    }

    @RequestMapping(path = "/api/races/toplist/today", method = RequestMethod.GET)
    public List<ToplistResult> getToplistToday() {
        try {
            return raceDbService.getToplistToday();
        } catch (ServiceLayerException ex) {
            LOG.error(ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }

    @RequestMapping(path = "/api/races/toplist/alltime", method = RequestMethod.GET)
    public List<ToplistResult> getToplistAllTime() {
        try {
            return raceDbService.getToplistAllTime();
        } catch (ServiceLayerException ex) {
            LOG.error(ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }

    @RequestMapping(path = "/api/races/toplist/week", method = RequestMethod.GET)
    public List<ToplistResult> getToplistWeek() {
        try {
            return raceDbService.getToplistWeek();
        } catch (ServiceLayerException ex) {
            LOG.error(ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }

    @RequestMapping(path = "/api/races/load", method = RequestMethod.GET)
    public RaceStateResult getRaceData(@RequestParam(name = "id", required = true) Integer raceId) {
        try {
            RacesRecord racesRecord = raceDbService.getRaceRecordForId(raceId);
            List<Participant> participants = raceDbService.getRaceParticipants(raceId);

            RaceStateResult raceStateResult = new RaceStateResult();
            raceStateResult.setMaxLaps(racesRecord.getLaps());
            raceStateResult.setStartTime(timeUtil.unixToLocalDateTime(racesRecord.getStarttime()));
            //            raceStateResult.setState(racesRecord.getState());
            Map<Participant, ParticipantRaceData> raceData = new HashMap<>();
            for (Participant participant : participants) {
                ParticipantRaceData participantRaceData = new ParticipantRaceData();
                List<LapsRecord> laps = raceDbService.getLaps(raceId, participant.getChipId());
                participantRaceData.fillLapDataFromDatabase(laps);
                raceData.put(participant, participantRaceData);
            }
            raceStateResult.setRaceData(raceData);
            return raceStateResult;

        } catch (ServiceLayerException ex) {
            LOG.error(ex.getMessage(), ex);
            throw new RuntimeException("cannot get race " + raceId);
        }
    }

}
