package de.warhog.fpvlaptracker.controllers;

import de.warhog.fpvlaptracker.controllers.dtos.ProfileResult;
import de.warhog.fpvlaptracker.controllers.dtos.StatusResult;
import de.warhog.fpvlaptracker.service.RestService;
import de.warhog.fpvlaptracker.entities.Rssi;
import de.warhog.fpvlaptracker.entities.Participant;
import de.warhog.fpvlaptracker.entities.ParticipantDeviceData;
import de.warhog.fpvlaptracker.entities.Profile;
import de.warhog.fpvlaptracker.entities.Result;
import de.warhog.fpvlaptracker.service.AudioService;
import de.warhog.fpvlaptracker.service.ParticipantsDbService;
import de.warhog.fpvlaptracker.service.ParticipantsService;
import de.warhog.fpvlaptracker.service.ProfilesService;
import de.warhog.fpvlaptracker.service.ServiceLayerException;
import java.util.ArrayList;
import java.util.List;
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
    private RestService restService;

    @Autowired
    private ParticipantsService participantsService;

    @Autowired
    private ParticipantsDbService participantsDbService;

    @Autowired
    private AudioService audioService;

    @Autowired
    private WebSocketController webSocketController;
    
    @Autowired
    private ProfilesService profilesService;

    @RequestMapping(path = "/api/participant/rssi", method = RequestMethod.GET)
    public Rssi getRssi(@RequestParam(name = "chipid", required = true) Long chipid) {
        try {
            Participant participant = participantsService.getParticipant(chipid);
            Rssi rssi = restService.getRssi(participant.getIp());
            return rssi;
        } catch (Exception ex) {
            LOG.error("cannot load rssi", ex);
            Rssi rssi = new Rssi();
            rssi.setRssi(0);
            return rssi;
        }
    }

    @RequestMapping(path = "/api/participant/deviceData", method = RequestMethod.GET)
    public ParticipantDeviceData getDeviceData(@RequestParam(name = "chipid", required = true) Long chipid) {
        Participant participant = participantsService.getParticipant(chipid);
        participant.loadParticipantDeviceDataFromUnit(restService);
        ParticipantDeviceData participantDeviceData = participant.getParticipantDeviceData();
        participantDeviceData.setChipid(chipid);
        LOG.info("sending participant data " + participantDeviceData.toString());
        return participantDeviceData;
    }
    
    @RequestMapping(path = "/api/auth/participant/deviceData", method = RequestMethod.POST)
    public StatusResult setDeviceData(@RequestBody ParticipantDeviceData participantDeviceData) {
        LOG.error("posting deviceData " + participantDeviceData.toString());
        try {
            Participant participant = participantsService.getParticipant(participantDeviceData.getChipid());
            String result = restService.postDeviceData(participant.getIp(), participantDeviceData);
            if (!participant.getName().equals(participantDeviceData.getParticipantName())) {
                participant.setName(participantDeviceData.getParticipantName());
                try {
                    participantsDbService.createOrUpdateParticipant(participant.getChipId(), participantDeviceData.getParticipantName());
                } catch (ServiceLayerException ex) {
                    LOG.error("cannot store name to database", ex);
                }
            }
            return new StatusResult(result);
        } catch (Exception ex) {
            LOG.error("cannot save devicedata", ex);
            return new StatusResult(StatusResult.Status.NOK);
        }
    }

    @RequestMapping(path = "/api/auth/participant/reboot", method = RequestMethod.GET)
    public StatusResult rebootDevice(@RequestParam(name = "chipid", required = true) Long chipid) {
        StatusResult result = new StatusResult(StatusResult.Status.NOK);
        try {
            Participant participant = participantsService.getParticipant(chipid);
            String data = restService.rebootDevice(participant.getIp());
            result = new StatusResult(data);
            if (!data.contains("NOK")) {
                // remove participant
                webSocketController.sendNewParticipantMessage(participant.getChipId());
                audioService.speakUnregistered(participant.getName());
                participantsService.removeParticipant(participant);
            }
        } catch (Exception ex) {
            LOG.error("cannot reboot device", ex);
        }
        return result;
    }

    @RequestMapping(path = "/api/auth/participant/skipcalibration", method = RequestMethod.GET)
    public StatusResult skipCalibration(@RequestParam(name = "chipid", required = true) Long chipId) {
        StatusResult result = new StatusResult(StatusResult.Status.NOK);
        try {
            Participant participant = participantsService.getParticipant(chipId);
            Result data = restService.skipCalibration(participant.getIp());
            LOG.debug("return for skipCalibration: " + data.toString());
            if (data.isOK()) {
                result = new StatusResult(StatusResult.Status.OK);
            }
        } catch (Exception ex) {
            LOG.error("cannot reboot device", ex);
        }
        return result;
    }

    @RequestMapping(path = "/api/participants", method = RequestMethod.GET)
    public List<Participant> getAll() {
        return participantsService.getAllParticipants();
    }
    
    @RequestMapping(path = "/api/auth/participants/profiles", method = RequestMethod.GET)
    public List<Profile> getProfiles(@RequestParam(name = "chipid", required = true) Long chipId) {
        try {
            return profilesService.getProfiles(chipId);
        } catch (ServiceLayerException ex) {
            LOG.error("cannot get profiles: " + ex.getMessage(), ex);
            return new ArrayList<>();
        }
    }

    @RequestMapping(path = "/api/auth/participants/profile", method = RequestMethod.GET)
    public Profile getProfiles(@RequestParam(name = "chipid", required = true) Long chipId, @RequestParam(name = "name", required = true) String name) {
        try {
            return profilesService.getProfile(chipId, name);
        } catch (ServiceLayerException ex) {
            LOG.error("cannot get profile: " + ex.getMessage(), ex);
            return new Profile();
        }
    }

    @RequestMapping(path = "/api/auth/participants/profile", method = RequestMethod.POST)
    public StatusResult setProfile(@RequestBody ProfileResult profileResult) {
        LOG.debug("setProfile " + profileResult.toString());
        try {
            profilesService.createOrUpdateProfile(profileResult.getChipId(), profileResult.getName(), profileResult.getData());
            return new StatusResult(StatusResult.Status.OK);
        } catch (ServiceLayerException ex) {
            LOG.error("cannot get profile: " + ex.getMessage(), ex);
            return new StatusResult(StatusResult.Status.NOK, "cannot save profile: " + ex.getMessage());
        }
    }

    @RequestMapping(path = "/api/auth/participants/profile", method = RequestMethod.DELETE)
    public StatusResult deleteProfile(@RequestParam(name = "chipid", required = true) Long chipId, @RequestParam(name = "name", required = true) String name) {
        LOG.debug("deleteProfile " + chipId + " - " + name);
        try {
            profilesService.deleteProfile(chipId, name);
            return new StatusResult(StatusResult.Status.OK);
        } catch (ServiceLayerException ex) {
            LOG.error("cannot get profile: " + ex.getMessage(), ex);
            return new StatusResult(StatusResult.Status.NOK, "cannot delete profile: " + ex.getMessage());
        }
    }

}
