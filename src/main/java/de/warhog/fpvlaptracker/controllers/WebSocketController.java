package de.warhog.fpvlaptracker.controllers;

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

    @MessageMapping("/lap")
    @SendTo("/topic/lap")
    public String sendMessage(String message) {
        return message;
    }

    public void sendNewLapMessage(Integer chipId) {
        LOG.debug("sending lap message for chipid " + chipId);
        this.template.convertAndSend("/topic/lap", chipId);
    }

    public void sendNewParticipantMessage(Integer chipId) {
        LOG.debug("sending new participant message for chipid " + chipId);
        this.template.convertAndSend("/topic/participant", chipId);
    }

    public void sendAudioRaceEndedMessage() {
        LOG.debug("sending audio race ended message");
        this.template.convertAndSend("/topic/audio", "raceEnded");
    }

    public void sendAudioRaceStartedMessage() {
        LOG.debug("sending audio race started message");
        this.template.convertAndSend("/topic/audio", "raceStarted");
    }

    public void sendAudioLapMessage() {
        LOG.debug("sending audio lap message");
        this.template.convertAndSend("/topic/audio", "lap");
    }

    public void sendAudioInvalidLapMessage() {
        LOG.debug("sending audio invalid lap message");
        this.template.convertAndSend("/topic/audio", "invalidLap");
    }

    public void sendAudioRegisteredMessage() {
        LOG.debug("sending audio registered message");
        this.template.convertAndSend("/topic/audio", "registered");
    }

    public void sendAudioParticipantEndedMessage() {
        LOG.debug("sending audio participant ended message");
        this.template.convertAndSend("/topic/audio", "participantEnded");
    }

}
