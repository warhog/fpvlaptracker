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

    public void speakRegistered(Integer nodes) {
        try {
            speak(String.format(speechTexts.getText(SpeechTexts.TextsEnum.REGISTERED), nodes));
        } catch (NoSuchFieldException ex) {
            LOG.error("cannot speak register: " + ex.getMessage(), ex);
        }
    }

    public void speakUnregistered(Integer nodes) {
        try {
            speak(String.format(speechTexts.getText(SpeechTexts.TextsEnum.UNREGISTERED), nodes));
        } catch (NoSuchFieldException ex) {
            LOG.error("cannot speak unregister: " + ex.getMessage(), ex);
        }
    }

    public void speakFinished() {
        try {
            speak(speechTexts.getText(SpeechTexts.TextsEnum.FINISHED));
        } catch (NoSuchFieldException ex) {
            LOG.error("cannot speak finished: " + ex.getMessage(), ex);
        }
    }

    public void speakInvalidLap(String name) {
        try {
            speak(String.format(speechTexts.getText(SpeechTexts.TextsEnum.INVALID_LAP), name));
        } catch (NoSuchFieldException ex) {
            LOG.error("cannot speak invalid lap for " + name + ": " + ex.getMessage(), ex);
        }
    }

    public void speakPilotEnded(String name) {
        try {
            speak(String.format(speechTexts.getText(SpeechTexts.TextsEnum.PILOT_ENDED), name));
        } catch (NoSuchFieldException ex) {
            LOG.error("cannot speak pilot ended for " + name + ": " + ex.getMessage(), ex);
        }
    }

    public void speakCalibrationDone(String name) {
        try {
            speak(String.format(speechTexts.getText(SpeechTexts.TextsEnum.CALIBRATION_DONE)));
        } catch (NoSuchFieldException ex) {
            LOG.error("cannot speak calibration done for " + name + ": " + ex.getMessage(), ex);
        }
    }

    public void speakBatteryLow() {
        try {
            speak(String.format(speechTexts.getText(SpeechTexts.TextsEnum.BATTERY_LOW)));
        } catch (NoSuchFieldException ex) {
            LOG.error("cannot speak battery low: " + ex.getMessage(), ex);
        }
    }

    public void speakBatteryShutdown() {
        try {
            speak(String.format(speechTexts.getText(SpeechTexts.TextsEnum.BATTERY_SHUTDOWN)));
        } catch (NoSuchFieldException ex) {
            LOG.error("cannot speak battery shutdown: " + ex.getMessage(), ex);
        }
    }

    public void speakAlreadyDone(String name) {
        try {
            speak(String.format(speechTexts.getText(SpeechTexts.TextsEnum.ALREADY_DONE), name));
        } catch (NoSuchFieldException ex) {
            LOG.error("cannot speak already done for " + name + ": " + ex.getMessage(), ex);
        }
    }

    public void speakPilotStart(String name) {
        try {
            speak(String.format(speechTexts.getText(SpeechTexts.TextsEnum.PILOT_START), name));
        } catch (NoSuchFieldException ex) {
            LOG.error("cannot speak pilot start for " + name + ": " + ex.getMessage(), ex);
        }
    }

    public void speakFalseStartPilot(String name) {
        try {
            speak(String.format(speechTexts.getText(SpeechTexts.TextsEnum.FALSE_START_PILOT), name));
        } catch (NoSuchFieldException ex) {
            LOG.error("cannot speak false start for " + name + ": " + ex.getMessage(), ex);
        }
    }

    public void speakLastLapPilot(String name) {
        try {
            speak(String.format(speechTexts.getText(SpeechTexts.TextsEnum.LAST_LAP_PILOT), name));
        } catch (NoSuchFieldException ex) {
            LOG.error("cannot speak last lap for " + name + ": " + ex.getMessage(), ex);
        }
    }

    public void speakTimeOverPilot(String name) {
        try {
            speak(String.format(speechTexts.getText(SpeechTexts.TextsEnum.TIME_OVER_PILOT), name));
        } catch (NoSuchFieldException ex) {
            LOG.error("cannot speak time over for " + name + ": " + ex.getMessage(), ex);
        }
    }

    public void speakPleasePrepareForRace() {
        try {
            speak(String.format(speechTexts.getText(SpeechTexts.TextsEnum.PREPARE_FOR_RACE)));
        } catch (NoSuchFieldException ex) {
            LOG.error("cannot speak prepare for race: " + ex.getMessage(), ex);
        }
    }

    public void speakRaceAborted() {
        try {
            speak(String.format(speechTexts.getText(SpeechTexts.TextsEnum.RACE_ABORTED)));
        } catch (NoSuchFieldException ex) {
            LOG.error("cannot speak race aborted: " + ex.getMessage(), ex);
        }
    }

    public void speakNumberThree() {
        speak("3");
    }

    public void speakNumberTwo() {
        speak("2");
    }

    public void speakNumberOne() {
        speak("1");
    }

    public void speakGo() {
        try {
            speak(String.format(speechTexts.getText(SpeechTexts.TextsEnum.GO)));
        } catch (NoSuchFieldException ex) {
            LOG.error("cannot speak go", ex);
        }
    }

}
