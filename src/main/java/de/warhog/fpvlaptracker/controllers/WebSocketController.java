package de.warhog.fpvlaptracker.controllers;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.warhog.fpvlaptracker.configuration.ApplicationConfig;
import de.warhog.fpvlaptracker.entities.RaceState;
import de.warhog.fpvlaptracker.util.AudioFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {

    private static final Logger LOG = LoggerFactory.getLogger(WebSocketController.class);

    @Autowired
    private SimpMessagingTemplate template;
    
    @Autowired
    private ApplicationConfig applicationConfig;

    @MessageMapping("/lap")
    @SendTo("/topic/lap")
    public String sendMessage(String message) {
        return message;
    }
    
    
    public void sendRaceStateChangedMessage(RaceState raceState) {
        LOG.debug("sending race state changed message: " + raceState.toString());
        this.template.convertAndSend("/topic/race/state", raceState);
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
    
    public void sendSpeechMessage(String text) {
        LOG.debug("sending speech message: " + text);
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.put("text", text);
        node.put("language", applicationConfig.getAudioLanguage());
        this.template.convertAndSend("/topic/speech", node.toString());
    }
    
    public void sendStatusMessage(String status) {
        LOG.debug("sending status message: " + status);
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.put("udp", status);
        this.template.convertAndSend("/topic/status", node.toString());
    }

    public void sendRssiMessage(Long chipid, Integer rssi) {
        LOG.debug("sending new rssi message");
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.put("rssi", rssi);
        node.put("chipid", chipid);
        this.template.convertAndSend("/topic/rssi", node.toString());
    }

    public void sendScanMessage(Long chipid, Integer frequency, Integer rssi) {
        LOG.debug("sending new scan message");
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.put("rssi", rssi);
        node.put("frequency", frequency);
        node.put("chipid", chipid);
        this.template.convertAndSend("/topic/scan", node.toString());
    }

    public enum WarningMessageTypes {
        DANGER("danger"),
        WARNING("warning"),
        SUCCESS("success"),
        INFO("info"),
        PRIMARY("primary"),
        SECONDARY("secondary");

        private String value = "";
        
        WarningMessageTypes(String value) {
            this.value = value;
        }
        
        public String getValue() {
            return this.value;
        }
    }
    
    public void sendAlertMessage(WarningMessageTypes type, String headline, String text, boolean permanent) {
        LOG.debug("sending alert message");
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.put("type", type.getValue());
        node.put("headline", headline);
        node.put("text", text);
        node.put("permanent", permanent);
        this.template.convertAndSend("/topic/alert", node.toString());
    }

    public void sendAlertMessage(WarningMessageTypes type, String headline, String text) {
        LOG.debug("sending alert message");
        sendAlertMessage(type, headline, text, false);
    }

}
