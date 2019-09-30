package de.warhog.fpvlaptracker.controllers;

import de.warhog.fpvlaptracker.dtos.StatusResult;
import de.warhog.fpvlaptracker.entities.Node;
import de.warhog.fpvlaptracker.dtos.Rssi;
import de.warhog.fpvlaptracker.dtos.StringResult;
import de.warhog.fpvlaptracker.service.NodesService;
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
public class NodesController {

    private static final Logger LOG = LoggerFactory.getLogger(NodesController.class);

    @Autowired
    private NodesService nodeService;

    @RequestMapping(path = "/api/nodes", method = RequestMethod.GET)
    public List<Node> getNodes(@RequestParam(name = "update", defaultValue = "false") boolean update) {
        LOG.debug("get all nodes");
        try {
            nodeService.checkNodesStillAvailable();
            List<Node> nodes = nodeService.getNodes();
            if (update) {
                LOG.debug("updating node device data");
                for (Node node : nodes) {
                    LOG.debug("updating device data for " + node.toString());
                    node.loadNodeDeviceData();
                }
            }
            return nodes;
        } catch (Exception ex) {
            LOG.error("cannot get nodes: " + ex.getMessage(), ex);
            throw ex;
        }
    }

    @RequestMapping(path = "/api/node", method = RequestMethod.GET)
    public Node getDeviceData(@RequestParam(name = "chipid", required = true) Long chipid) {
        if (nodeService.hasNode(chipid)) {
            Node node = nodeService.getNode(chipid);
            node.loadNodeDeviceData();
            LOG.info("sending device data " + node.toString());
            return node;
        } else {
            LOG.error("invalid node requested: " + chipid);
            return new Node();
        }
    }

    @RequestMapping(path = "/api/auth/node", method = RequestMethod.POST)
    public StatusResult setDeviceData(@RequestBody Node node) {
        LOG.debug("posting nodeDeviceData " + node.toString());
        try {
            StringResult result = node.postNodeDeviceData();
            return new StatusResult(result.getResult());
        } catch (Exception ex) {
            LOG.error("cannot save devicedata: " + ex.getMessage(), ex);
            return new StatusResult(StatusResult.Status.NOK);
        }
    }

    @RequestMapping(path = "/api/node/rssi", method = RequestMethod.GET)
    public Rssi getRssi(@RequestParam(name = "chipid", required = true) Long chipid) {
        try {
            Node node = nodeService.getNode(chipid);
            return node.loadRssi();
        } catch (Exception ex) {
            LOG.error("cannot load rssi: " + ex.getMessage(), ex);
            Rssi rssi = new Rssi();
            rssi.setRssi(0);
            return rssi;
        }
    }

    @RequestMapping(path = "/api/auth/node/reboot", method = RequestMethod.GET)
    public StatusResult rebootDevice(@RequestParam(name = "chipid", required = true) Long chipid) {
        StatusResult result = new StatusResult(StatusResult.Status.NOK);
        try {
            Node node = nodeService.getNode(chipid);
            if (node.rebootNode().isOK()) {
                LOG.debug("reboot ok, removing node");
                nodeService.removeNode(node);
                result = new StatusResult(StatusResult.Status.OK);
            }
        } catch (Exception ex) {
            LOG.error("cannot reboot device", ex);
        }
        return result;
    }

    @RequestMapping(path = "/api/auth/node/factorydefaults", method = RequestMethod.GET)
    public StatusResult factoryDefaultsDevice(@RequestParam(name = "chipid", required = true) Long chipid) {
        StatusResult result = new StatusResult(StatusResult.Status.NOK);
        try {
            Node node = nodeService.getNode(chipid);
            if (node.restoreNodeFactoryDefaults().isOK()) {
                LOG.debug("restore factory defaults ok, rebooting node");
                if (node.rebootNode().isOK()) {
                    nodeService.removeNode(node);
                    result = new StatusResult(StatusResult.Status.OK);
                } else {
                    LOG.error("cannot reboot device");
                }
            } else {
                LOG.error("cannot restore factory defaults for device");
            }
        } catch (Exception ex) {
            LOG.error("cannot reboot device", ex);
        }
        return result;
    }
    
    @RequestMapping(path = "/api/auth/node/setstate", method = RequestMethod.GET)
    public StatusResult setState(@RequestParam(name = "chipid", required = true) Long chipId, @RequestParam(name = "state", required = true) String state) {
        LOG.debug("setting state for " + chipId + " to " + state);
        StatusResult result = new StatusResult(StatusResult.Status.NOK);
        try {
            Node node = nodeService.getNode(chipId);
            StringResult resultState = node.postState(state);
            LOG.debug("return for setstate: " + resultState.toString());
            if (resultState.isOK()) {
                result = new StatusResult(StatusResult.Status.OK);
            }
        } catch (Exception ex) {
            LOG.error("cannot set state: " + ex.getMessage(), ex);
        }
        return result;
    }

}
