package de.warhog.fpvlaptracker.controllers;

import de.warhog.fpvlaptracker.controllers.dtos.StatusResult;
import de.warhog.fpvlaptracker.controllers.dtos.requestbodies.Frequency;
import de.warhog.fpvlaptracker.service.RestService;
import de.warhog.fpvlaptracker.communication.entities.Data;
import de.warhog.fpvlaptracker.communication.entities.Rssi;
import de.warhog.fpvlaptracker.communication.entities.RssiMeasure;
import de.warhog.fpvlaptracker.controllers.dtos.requestbodies.MinLapTime;
import de.warhog.fpvlaptracker.controllers.dtos.requestbodies.Name;
import de.warhog.fpvlaptracker.controllers.dtos.requestbodies.Threshold;
import de.warhog.fpvlaptracker.race.entities.Participant;
import de.warhog.fpvlaptracker.service.ParticipantsDbService;
import de.warhog.fpvlaptracker.service.ServiceLayerException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ParticipantController {

    private static final Logger LOG = LoggerFactory.getLogger(ParticipantController.class);

    @Autowired
    private RestService comm;

    @Autowired
    private ParticipantsDbService participantsDbService;

    @RequestMapping(path = "/api/participant/rssi", method = RequestMethod.GET)
    public Rssi getRssi(@RequestParam(name = "chipid", required = true) Long chipid) {
        Participant participant = participantsDbService.getParticipant(chipid);
        Rssi rssi = comm.getRssi(participant);
        return rssi;
    }

    @RequestMapping(path = "/api/participant/setupData", method = RequestMethod.GET)
    public Map<String, String> getSetupData(@RequestParam(name = "chipid", required = true) Long chipid) {
        Participant participant = participantsDbService.getParticipant(chipid);
        Data data = comm.getData(participant);
        Map<String, String> ret = new HashMap<>();
        if (participant.isAllowConfiguration()) {
            ret.put("thresholdLow", data.getThresholdLow().toString());
            ret.put("thresholdHigh", data.getThresholdHigh().toString());
            ret.put("minLapTime", data.getMinLapTime().toString());
            ret.put("rssi", data.getRssi().toString());
            ret.put("frequency", data.getFrequency().toString());
        }
        if (participant.isAllowConfiguration() || participant.isAllowConfigureName()) {
            ret.put("name", participant.getName());
        }
        ret.put("ipAddress", participant.getIp().getHostAddress());
        ret.put("isAllowConfiguration", String.valueOf(participant.isAllowConfiguration()));
        ret.put("isAllowConfigureName", String.valueOf(participant.isAllowConfigureName()));
        ret.put("isCallable", String.valueOf(participant.isCallable()));
        return ret;
    }

    @RequestMapping(path = "/api/participant/minlaptime", method = RequestMethod.GET)
    public Map<String, Long> getMinLapTime(@RequestParam(name = "chipid", required = true) Long chipid) {
        Participant participant = participantsDbService.getParticipant(chipid);
        Long minLapTime = comm.getMinLapTime(participant);
        Map<String, Long> lapTime = new HashMap<>();
        lapTime.put("minLapTime", minLapTime);
        return lapTime;
    }

    @RequestMapping(path = "/api/participant/measure", method = RequestMethod.GET)
    public RssiMeasure getMeasure(@RequestParam(name = "chipid", required = true) Long chipid) {
        Participant participant = participantsDbService.getParticipant(chipid);
        RssiMeasure rssiMeasure = comm.getRssiMeasure(participant);
        return rssiMeasure;
    }

    @RequestMapping(path = "/api/auth/participant/minlaptime", method = RequestMethod.POST)
    public StatusResult setMinLapTime(@RequestBody MinLapTime minLapTime) {
        Participant participant = participantsDbService.getParticipant(minLapTime.getChipid());
        comm.setMinLapTime(participant, minLapTime.getMinlaptime().longValue());
        return new StatusResult(StatusResult.Status.OK);
    }

    @RequestMapping(path = "/api/auth/participant/frequency", method = RequestMethod.POST)
    public StatusResult setFrequency(@RequestBody Frequency frequency) {
        Participant participant = participantsDbService.getParticipant(frequency.getChipid());
        comm.setFrequency(participant, frequency.getFrequency());
        return new StatusResult(StatusResult.Status.OK);
    }

    @RequestMapping(path = "/api/participant/threshold", method = RequestMethod.GET)
    public Map<String, Integer> getThreshold(@RequestParam(name = "chipid", required = true) Long chipid) {
        Participant participant = participantsDbService.getParticipant(chipid);
        Integer thresholdLowValue = comm.getThresholdLow(participant);
        Integer thresholdHighValue = comm.getThresholdHigh(participant);
        Map<String, Integer> threshold = new HashMap<>();
        threshold.put("thresholdLow", thresholdLowValue);
        threshold.put("thresholdHigh", thresholdHighValue);
        return threshold;
    }

    @RequestMapping(path = "/api/auth/participant/threshold", method = RequestMethod.POST)
    public StatusResult setThreshold(@RequestBody Threshold threshold) {
        Participant participant = participantsDbService.getParticipant(threshold.getChipid());
        comm.setThresholds(participant, threshold.getThresholdLow(), threshold.getThresholdHigh());
        return new StatusResult(StatusResult.Status.OK);
    }

    @RequestMapping(path = "/api/auth/participant/name", method = RequestMethod.POST)
    public StatusResult setName(@RequestBody Name name) {
        Participant participant = participantsDbService.getParticipant(name.getChipid());
        participant.setName(name.getName());
        try {
            participantsDbService.createOrUpdateParticipantInDb(name.getChipid(), name.getName());
        } catch (ServiceLayerException ex) {
            LOG.error("cannot store name to database", ex);
        }
        return new StatusResult(StatusResult.Status.OK);
    }

    @RequestMapping(path = "/api/participants", method = RequestMethod.GET)
    public List<Participant> getAll() {
        return participantsDbService.getAllParticipants();
    }

}
