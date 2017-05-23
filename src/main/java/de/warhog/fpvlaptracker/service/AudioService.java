package de.warhog.fpvlaptracker.service;

import de.warhog.fpvlaptracker.configuration.ApplicationConfig;
import de.warhog.fpvlaptracker.controllers.WebSocketController;
import de.warhog.fpvlaptracker.util.AudioFile;
import java.io.IOException;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
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

    public void init() {
        if (!applicationConfig.isAudioPlayLocal()) {
            LOG.info("skipping audio file check because no local playback is configured");
            return;
        }

        for (AudioFile audioFile : AudioFile.values()) {
            audioFile.testIfExisting();
        }

    }

    public void play(AudioFile file) {
        play(file, 1);
    }

    public static String ordinal(int i) {
        return i % 100 == 11 || i % 100 == 12 || i % 100 == 13 ? i + "th" : i + new String[]{"th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th"}[i % 10];
    }

    public void play(AudioFile file, Integer repeat) {
        LOG.debug("sending websocket audio message " + file.getFilename() + ", repeat " + repeat);
        webSocketController.sendAudioMessage(file, repeat);
        LOG.debug("request for playing audio file " + file.getFilename() + ", repeat " + repeat);
        if (!applicationConfig.isAudioPlayLocal()) {
            LOG.info("not playing audio because local audio playback is not configured");
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                AudioInputStream inputStream = null;
                try {
                    inputStream = AudioSystem.getAudioInputStream(file.getFile());
                    DataLine.Info info = new DataLine.Info(Clip.class, inputStream.getFormat());
                    Clip clip = (Clip) AudioSystem.getLine(info);
                    clip.open(inputStream);
                    clip.start();
                    if (repeat > 1) {
                        LOG.debug("playing repeated: " + repeat);
                        for (Integer i = 1; i < repeat; i++) {
                            LOG.debug("playing " + ordinal(i + 1) + " time");
                            Thread.sleep(clip.getMicrosecondLength() / 1000);
                            play(file);
                        }
                    }
                } catch (UnsupportedAudioFileException | IOException | LineUnavailableException | InterruptedException ex) {
                    LOG.info("cannot play audio file", ex);
                } finally {
                    try {
                        if (inputStream != null) {
                            inputStream.close();
                        }
                    } catch (IOException ex) {
                        LOG.debug("cannot close input stream");
                    }
                }
            }
        }).start();
    }

    public void playStart() {
        play(AudioFile.LAP, 3);
    }

    public void playLap() {
        play(AudioFile.LAP);
    }

    public void playRegistered() {
        play(AudioFile.REGISTER);
    }

    public void playUnregistered() {
        play(AudioFile.UNREGISTER);
    }

    public void playFinished() {
        play(AudioFile.FINISHED);
    }

    public void playInvalidLap() {
        play(AudioFile.INVALID_LAP);
    }

    public void playParticipantEnded() {
        play(AudioFile.PARTICIPANT_ENDED);
    }

}
