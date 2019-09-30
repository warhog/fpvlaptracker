package de.warhog.fpvlaptracker.service;

import de.warhog.fpvlaptracker.controllers.WebSocketController;
import de.warhog.fpvlaptracker.entities.Node;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class NodesService {

    private static final Logger LOG = LoggerFactory.getLogger(NodesService.class);

    private final List<Node> nodes = new ArrayList<>();

    @Autowired
    private AudioService audioService;
    
    @Autowired
    private WebSocketController websocketController;
    
    public void addNode(Node node) {
        if (!nodes.contains(node)) {
            LOG.debug("adding node: " + node.toString());
            node.loadNodeDeviceData();
            nodes.add(node);
            audioService.speakRegistered(nodes.size());
            websocketController.sendNodeCountMessage(nodes.size());
        } else {
            LOG.debug("node already existing: " + node.toString());
        }
    }

    public void removeNode(Node node) {
        if (nodes.contains(node)) {
            LOG.debug("removing node: " + node.toString());
            nodes.remove(node);
            audioService.speakUnregistered(nodes.size());
            websocketController.sendNodeCountMessage(nodes.size());
        } else {
            LOG.debug("cannot remove node, not existing: " + node.toString());
        }
    }
    
    public void loadNodeDeviceDataForAll() {
        for (Node node : nodes) {
            node.loadNodeDeviceData();
        }
    }

    public boolean hasNode(Node node) {
        return nodes.contains(node);
    }

    public boolean hasNode(Long chipId) {
        Node node = getNode(chipId);
        return node != null;
    }

    public boolean hasNodes() {
        return !nodes.isEmpty();
    }

    /**
     * get node by chipId
     *
     * @param chipId
     * @return Node or null if no node with chipId found
     */
    public Node getNode(Long chipId) {
        return nodes.stream().filter(x -> x.getChipId().equals(chipId)).findAny().orElse(null);
    }
    
    public List<Node> getNodes() {
        return nodes;
    }
    
    @Scheduled(fixedDelay = 60000L)
    public void checkNodesStillAvailable() {
        LOG.debug("checking for non-existing nodes");
        List<Node> nodesToRemove = new ArrayList<>();
        for (Node node : nodes) {
            try {
                if (node.getInetAddress().isReachable(100)) {
                    LOG.debug("node with chipId " + node.getChipId() + " found");
                } else {
                    LOG.info("node with chipid " + node.getChipId() + " not found, removing");
                    nodesToRemove.add(node);
                }
            } catch (Exception ex) {
                LOG.error("cannot check availability for " + node.toString());
            }
        }
        
        for (Node node : nodesToRemove) {
            LOG.debug("finally removing node with chipid " + node.getChipId());
            removeNode(node);
        }
    }
 
}
