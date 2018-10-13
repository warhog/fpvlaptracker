package de.warhog.fpvlaptracker.controllers;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.warhog.fpvlaptracker.util.AudioFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {

    private static final Logger LOG = LoggerFactory.getLogger(WebSocketController.class);

    @Autowired
    private SimpMessagingTemplate template;

    @MessageMapping("/lap")
    @SendTo("/topic/lap")
    public String sendMessage(String message) {
        return message;
    }

    public void sendNewLapMessage(Long chipId) {
        LOG.debug("sending lap message for chipid " + chipId);
        this.template.convertAndSend("/topic/lap", chipId);
    }

    public void sendNewParticipantMessage(Long chipId) {
        LOG.debug("sending new participant message for chipid " + chipId);
        this.template.convertAndSend("/topic/participant", chipId);
    }

    public void sendAudioMessage(AudioFile file) {
        sendAudioMessage(file, 1);
    }

    public void sendAudioMessage(AudioFile file, Integer repeat) {
        LOG.debug("sending audio message for " + file + ", repeated " + repeat);
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.put("file", file.getFilename());
        node.put("repeat", repeat);
        this.template.convertAndSend("/topic/audio", node.toString());
    }
    
    public void sendStatusMessage(String status) {
        LOG.debug("sending status message");
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.put("udp", status);
        this.template.convertAndSend("/topic/status", node.toString());
    }

}
