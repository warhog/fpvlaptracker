package de.warhog.fpvlaptracker.controllers;

import de.warhog.fpvlaptracker.dtos.PilotResult;
import de.warhog.fpvlaptracker.dtos.StatusResult;
import de.warhog.fpvlaptracker.entities.Pilot;
import de.warhog.fpvlaptracker.dtos.Profile;
import de.warhog.fpvlaptracker.service.PilotsService;
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
public class PilotsController {

    private static final Logger LOG = LoggerFactory.getLogger(PilotsController.class);

    @Autowired
    private PilotsService pilotsService;

    @Autowired
    private ProfilesService profilesService;

    @RequestMapping(path = "/api/pilots", method = RequestMethod.GET)
    public List<PilotResult> getAll() {
        List<PilotResult> results = new ArrayList<>();
        for (Pilot pilot : pilotsService.getPilots()) {
            PilotResult pilotResult = new PilotResult();
            Long chipId = null;
            if (pilot.getNode() != null) {
                chipId = pilot.getNode().getChipId();
            }
            pilotResult.setChipId(chipId);
            pilotResult.setName(pilot.getName());
            results.add(pilotResult);
        }
        return results;
    }

    @RequestMapping(path = "/api/pilot", method = RequestMethod.GET)
    public PilotResult getPilot(@RequestParam(name = "name", required = true) String name) {
        PilotResult pilotResult = new PilotResult();
        Pilot pilot = pilotsService.getPilot(name);
        Long chipId = null;
        if (pilot.getNode() != null) {
            chipId = pilot.getNode().getChipId();
        }
        pilotResult.setChipId(chipId);
        pilotResult.setName(pilot.getName());
        return pilotResult;
    }

    @RequestMapping(path = "/api/auth/pilot", method = RequestMethod.POST)
    public StatusResult addPilot(@RequestBody PilotResult pilotResult) {
        LOG.debug("addPilot" + pilotResult.toString());
        try {
            // create or update pilot
            if (!pilotResult.getName().equals(pilotResult.getUnmodifiedName())) {
                // name was changed, delete
                pilotsService.removePilot(pilotResult.getUnmodifiedName(), true);
            }

            pilotsService.addPilot(pilotResult.getName(), pilotResult.getChipId());
            return new StatusResult(StatusResult.Status.OK);
        } catch (ServiceLayerException ex) {
            LOG.error("cannot get profile: " + ex.getMessage(), ex);
            return new StatusResult(StatusResult.Status.NOK, "cannot save profile: " + ex.getMessage());
        }
    }

    @RequestMapping(path = "/api/auth/pilot", method = RequestMethod.DELETE)
    public StatusResult deletePilot(@RequestParam(name = "name", required = true) String name) {
        LOG.debug("delete pilot " + name);
        try {
            pilotsService.removePilot(name);
            return new StatusResult(StatusResult.Status.OK);
        } catch (ServiceLayerException ex) {
            LOG.error("cannot get profile: " + ex.getMessage(), ex);
            return new StatusResult(StatusResult.Status.NOK, "cannot delete profile: " + ex.getMessage());
        }
    }

    @RequestMapping(path = "/api/auth/pilot/profiles", method = RequestMethod.GET)
    public List<Profile> getProfiles(@RequestParam(name = "chipid", required = true) Long chipId) {
        try {
            return profilesService.getProfiles(chipId);
        } catch (ServiceLayerException ex) {
            LOG.error("cannot get profiles: " + ex.getMessage(), ex);
            return new ArrayList<>();
        }
    }

    @RequestMapping(path = "/api/auth/pilot/profile", method = RequestMethod.GET)
    public Profile getProfile(@RequestParam(name = "chipid", required = true) Long chipId, @RequestParam(name = "name", required = true) String name) {
        try {
            return profilesService.getProfile(chipId, name);
        } catch (ServiceLayerException ex) {
            LOG.error("cannot get profile: " + ex.getMessage(), ex);
            return new Profile();
        }
    }

    @RequestMapping(path = "/api/auth/pilot/profile", method = RequestMethod.POST)
    public StatusResult setProfile(@RequestBody Profile profile) {
        LOG.debug("setProfile " + profile.toString());
        try {
            profilesService.createOrUpdateProfile(profile.getChipId(), profile.getName(), profile.getData());
            return new StatusResult(StatusResult.Status.OK);
        } catch (ServiceLayerException ex) {
            LOG.error("cannot get profile: " + ex.getMessage(), ex);
            return new StatusResult(StatusResult.Status.NOK, "cannot save profile: " + ex.getMessage());
        }
    }

    @RequestMapping(path = "/api/auth/pilot/profile", method = RequestMethod.DELETE)
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
