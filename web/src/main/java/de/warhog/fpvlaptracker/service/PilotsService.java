package de.warhog.fpvlaptracker.service;

import de.warhog.fpvlaptracker.controllers.WebSocketController;
import de.warhog.fpvlaptracker.db.DbLayerException;
import de.warhog.fpvlaptracker.db.PilotDbLayer;
import de.warhog.fpvlaptracker.entities.Node;
import de.warhog.fpvlaptracker.entities.Pilot;
import de.warhog.fpvlaptracker.util.PilotState;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PilotsService {

    private static final Logger LOG = LoggerFactory.getLogger(PilotsService.class);

    @Autowired
    private WebSocketController websocketController;

    @Autowired
    private PilotDbLayer pilotDbLayer;

    @Autowired
    private NodesService nodeService;

    private List<Pilot> pilots = new ArrayList<>();

    @PostConstruct
    public void loadPilots() {
        LOG.debug("loading pilots from db");
        try {
            pilots = pilotDbLayer.getPilots();
        } catch (DbLayerException ex) {
            LOG.error("cannot get pilots from db: " + ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }

    public List<Pilot> getPilots() {
        return pilots;
    }

    public List<Pilot> getPilotsWithNodes() {
        List<Pilot> validPilots = new ArrayList<>();
        for (Pilot pilot : pilots) {
            if (pilot.hasNode()) {
                validPilots.add(pilot);
            }
        }
        return validPilots;
    }

    public void addPilot(String name, Long chipId) throws ServiceLayerException {
        Pilot pilot = new Pilot();
        pilot.setName(name);
        if (chipId != null) {
            if (nodeService.hasNode(chipId)) {
                Node node = nodeService.getNode(chipId);
                pilot.setNode(node);
            } else {
                LOG.error("cannot add node to pilot, non existing chipId given: " + chipId);
            }
        }
        addPilot(pilot);
    }

    public void addPilot(String name) throws ServiceLayerException {
        addPilot(name, null);
    }

    public void addPilot(Pilot pilot) throws ServiceLayerException {
        boolean silent = false;
        if (hasPilot(pilot)) {
            LOG.info("pilot already existing, replace");
            removePilot(pilot, silent);
        }
        if (pilot.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("pilot name is empty");
        }
        if (pilot.getName().length() > 255) {
            throw new IllegalArgumentException("pilot name too long");
        }

        Long chipId = null;
        if (pilot.getNode() != null) {
            chipId = pilot.getNode().getChipId();
        }
        try {
            pilotDbLayer.createOrUpdatePilot(pilot.getName(), chipId);
        } catch (DbLayerException ex) {
            LOG.error("cannot store pilot in db: " + ex.getMessage(), ex);
            throw new ServiceLayerException(ex);
        }
        pilots.add(pilot);
        if (!silent) {
            websocketController.sendPilotCountMessage(pilots.size());
        }
    }

    public Pilot getPilot(Long chipId) {
        for (Pilot pilot : pilots) {
            if (pilot.hasNode()) {
                if (Objects.equals(pilot.getNode().getChipId(), chipId)) {
                    return pilot;
                }
            }
        }
        throw new IllegalArgumentException("pilot with chipid not found: " + chipId.toString());
    }

    public Pilot getPilot(String name) {
        for (Pilot pilot : pilots) {
            if (pilot.getName().equals(name)) {
                return pilot;
            }
        }
        throw new IllegalArgumentException("pilot with name not found: " + name);
    }

    public Boolean hasPilot(String name) {
        for (Pilot pilot : pilots) {
            if (pilot.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    public Boolean hasValidPilots() {
        return !getPilotsWithNodes().isEmpty();
    }

    public Boolean hasPilots() {
        return !pilots.isEmpty();
    }

    public Boolean hasPilot(Pilot pilot) {
        return pilots.contains(pilot);
    }

    public Boolean hasPilot(Long chipId) {
        for (Pilot pilot : pilots) {
            if (pilot.hasNode()) {
                if (Objects.equals(pilot.getNode().getChipId(), chipId)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void clearPilots() {
        pilots.clear();
        websocketController.sendPilotCountMessage(pilots.size());
    }

    public void removePilot(Pilot pilot, boolean silent) throws ServiceLayerException {
        if (pilots.contains(pilot)) {
            pilots.remove(pilot);

            try {
                pilotDbLayer.deletePilot(pilot.getName());
            } catch (DbLayerException ex) {
                LOG.error("cannot remove pilot from database");
                throw new ServiceLayerException(ex);
            }

            if (!silent) {
                websocketController.sendPilotCountMessage(pilots.size());
            }
        }
    }

    public void removePilot(String name) throws ServiceLayerException {
        removePilot(name, false);
    }

    public void removePilot(String name, boolean silent) throws ServiceLayerException {
        if (hasPilot(name)) {
            Pilot pilot = getPilot(name);
            removePilot(pilot, silent);
        }
    }

    public void removePilot(Pilot pilot) throws ServiceLayerException {
        removePilot(pilot, false);
    }

    @Scheduled(initialDelay = 5000L, fixedDelay = 10000L)
    public void checkPilotNodesAvailable() {
        LOG.debug("checking for non-existing nodes on pilots");
        try {
            for (Pilot pilot : pilots) {
                LOG.debug("checking pilot " + pilot.getName());
                if (pilot.hasNode()) {
                    if (nodeService.hasNode(pilot.getNode())) {
                        LOG.debug("node found: " + pilot.getNode().getChipId().toString());
                    } else {
                        LOG.debug("node " + pilot.getChipId() + " not found, removing");
                        addPilot(pilot.getName(), null);
                    }
                } else if (pilot.getChipId() != null && !pilot.hasNode()) {
                    // pilot has chipid but no node -> check if node is available and register with pilot
                    LOG.debug("pilot has chipid " + pilot.getChipId() + " but no node");
                    if (nodeService.hasNode(pilot.getChipId())) {
                        LOG.debug("node available, register node on pilot");
                        Node node = nodeService.getNode(pilot.getChipId());
                        pilot.setNode(node);
                    }
                }
            }
        } catch (ServiceLayerException ex) {
            LOG.error("cannot check for non-existing nodes on pilots");
        }
    }

    public void resetValidity() {
        for (Pilot pilot : pilots) {
            pilot.setValid(true);
            pilot.setState(PilotState.WAITING_FOR_START);
        }
    }

    public void setPilotValid(String name, boolean valid) {
        if (!hasPilot(name)) {
            throw new RuntimeException("pilot with name not found: " + name);
        }
        Pilot pilot = getPilot(name);
        pilot.setValid(valid);
    }

    public void resetAllLapData() {
        for (Pilot pilot : getPilots()) {
            pilot.resetLapData();
        }
    }
}
