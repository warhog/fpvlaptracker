package de.warhog.fpvlaptracker.service;

import de.warhog.fpvlaptracker.controllers.WebSocketController;
import de.warhog.fpvlaptracker.util.AudioFile;
import de.warhog.fpvlaptracker.util.SpeechTexts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AudioService {

    private static final Logger LOG = LoggerFactory.getLogger(AudioService.class);

    @Autowired
    private WebSocketController webSocketController;

    @Autowired
    private SpeechTexts speechTexts;

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
        try {
            speak(String.format(speechTexts.getText(SpeechTexts.TextsEnum.REGISTERED), name));
        } catch (NoSuchFieldException ex) {
            LOG.error("cannot speak register for " + name, ex);
        }
    }

    public void speakUnregistered(String name) {
        try {
            speak(String.format(speechTexts.getText(SpeechTexts.TextsEnum.UNREGISTERED), name));
        } catch (NoSuchFieldException ex) {
            LOG.error("cannot speak unregister for " + name, ex);
        }
    }

    public void speakFinished() {
        try {
            speak(speechTexts.getText(SpeechTexts.TextsEnum.FINISHED));
        } catch (NoSuchFieldException ex) {
            LOG.error("cannot speak finished", ex);
        }
    }

    public void speakInvalidLap(String name) {
        try {
            speak(String.format(speechTexts.getText(SpeechTexts.TextsEnum.INVALID_LAP), name));
        } catch (NoSuchFieldException ex) {
            LOG.error("cannot speak invalid lap for " + name, ex);
        }
    }

    public void speakParticipantEnded(String name) {
        try {
            speak(String.format(speechTexts.getText(SpeechTexts.TextsEnum.PARTICIPANT_ENDED), name));
        } catch (NoSuchFieldException ex) {
            LOG.error("cannot speak participant ended for " + name, ex);
        }
    }

    public void speakCalibrationDone(String name) {
        try {
            speak(String.format(speechTexts.getText(SpeechTexts.TextsEnum.CALIBRATION_DONE), name));
        } catch (NoSuchFieldException ex) {
            LOG.error("cannot speak calibration done for " + name, ex);
        }
    }

    public void speakBatteryLow(String name) {
        try {
            speak(String.format(speechTexts.getText(SpeechTexts.TextsEnum.BATTERY_LOW), name));
        } catch (NoSuchFieldException ex) {
            LOG.error("cannot speak battery low for " + name, ex);
        }
    }

    public void speakBatteryShutdown(String name) {
        try {
            speak(String.format(speechTexts.getText(SpeechTexts.TextsEnum.BATTERY_SHUTDOWN), name));
        } catch (NoSuchFieldException ex) {
            LOG.error("cannot speak battery shutdown for " + name, ex);
        }
    }

    public void speakAlreadyDone(String name) {
        try {
            speak(String.format(speechTexts.getText(SpeechTexts.TextsEnum.ALREADY_DONE), name));
        } catch (NoSuchFieldException ex) {
            LOG.error("cannot speak already done for " + name, ex);
        }
    }
    
}
