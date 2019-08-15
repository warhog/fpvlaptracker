package de.warhog.fpvlaptracker.controllers;

import de.warhog.fpvlaptracker.race.RaceLogicHandler;
import de.warhog.fpvlaptracker.service.NodesService;
import de.warhog.fpvlaptracker.service.PilotsService;
import de.warhog.fpvlaptracker.service.VersionService;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MiscController {

    private static final Logger LOG = LoggerFactory.getLogger(MiscController.class);

    @Autowired
    private RaceLogicHandler race;

    @Autowired
    private PilotsService pilotsService;
    
    @Autowired
    private NodesService nodeService;
    
    @Autowired
    private VersionService versionService;

    @RequestMapping(path = "/api/badgedata", method = RequestMethod.GET)
    public Map<String, String> getBadgeData() {
        Map<String, String> ret = new HashMap<>();
        ret.put("pilots", Integer.toString(pilotsService.getPilots().size()));
        ret.put("state", race.getState().toString());
        ret.put("nodes", Integer.toString(nodeService.getNodes().size()));
        return ret;
    }
    
    @RequestMapping(path = "/api/version", method = RequestMethod.GET)
    public Map<String, String> getVersion() {
        Map<String, String> ret = new HashMap<>();
        ret.put("version", versionService.getVersion());
        return ret;
    }

}
