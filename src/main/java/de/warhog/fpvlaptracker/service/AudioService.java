package de.warhog.fpvlaptracker.service;

import de.warhog.fpvlaptracker.configuration.ApplicationConfig;
import de.warhog.fpvlaptracker.controllers.WebSocketController;
import de.warhog.fpvlaptracker.util.AudioFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AudioService {

    private static final Logger LOG = LoggerFactory.getLogger(AudioService.class);

    @Autowired
    private ApplicationConfig applicationConfig;

    @Autowired
    private WebSocketController webSocketController;

    public void play(AudioFile file) {
        play(file, 1);
    }

    public static String ordinal(int i) {
        return i % 100 == 11 || i % 100 == 12 || i % 100 == 13 ? i + "th" : i + new String[]{"th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th"}[i % 10];
    }

    public void speak(String text) {
        LOG.debug("sending websocket speech message: " + text);
        webSocketController.sendSpeechMessage(text);
    }
    
    public void play(AudioFile file, Integer repeat) {
        LOG.debug("sending websocket audio message " + file.getFilename() + ", repeat " + repeat);
        webSocketController.sendAudioMessage(file, repeat);
    }

    public void playStart() {
        play(AudioFile.LAP, 3);
    }

    public void playLap() {
        play(AudioFile.LAP);
    }

    public void speakRegistered(String name) {
        String text = "Registered " + name + ".";
        if (applicationConfig.getAudioLanguage().equals("de-DE")) {
            text = name + " registriert.";
        }
        speak(text);
    }
    
    public void speakUnregistered(String name) {
        String text = "Removed " + name + ".";
        if (applicationConfig.getAudioLanguage().equals("de-DE")) {
            text = name + " entfernt.";
        }
        speak(text);
    }

    public void speakFinished() {
        String text = "Race finished.";
        if (applicationConfig.getAudioLanguage().equals("de-DE")) {
            text = "Rennen beendet.";
        }
        speak(text);
    }

    public void speakInvalidLap(String name) {
        String text = "Invalid lap: " + name;
        if (applicationConfig.getAudioLanguage().equals("de-DE")) {
            text = "Ungültige Runde: " + name;
        }
        speak(text);
    }
    
    public void speakParticipantEnded(String name) {
        String text = name + " reached the goal";
        if (applicationConfig.getAudioLanguage().equals("de-DE")) {
            text = name + " hat das Ziel erreicht.";
        }
        speak(text);
    }
    
    public void speakCalibrationDone(String name) {
        String text = name + " calibrated.";
        if (applicationConfig.getAudioLanguage().equals("de-DE")) {
            text = name + " kalibriert.";
        }
        speak(text);
    }

    public void speakBatteryLow(String name) {
        String text = "Low battery warning for " + name + ".";
        if (applicationConfig.getAudioLanguage().equals("de-DE")) {
            text = "Niedriger Akkustand " + name + ".";
        }
        speak(text);
    }

    public void speakAlreadyDone(String name) {
        String text = name + " already finished.";
        if (applicationConfig.getAudioLanguage().equals("de-DE")) {
            text = name + " bereits beendet.";
        }
        speak(text);
    }

}
