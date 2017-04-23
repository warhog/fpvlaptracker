package de.warhog.fpvlaptracker.service;

import de.warhog.fpvlaptracker.configuration.ApplicationConfig;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
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
    
    public void init() {
        if (!applicationConfig.isAudioPlayLocal()) {
            LOG.info("skipping audio file check because no local playback is configured");
            return;
        }
        
        if (!Files.exists(Paths.get("audio/lap.wav"))) {
            throw new RuntimeException("audio/lap.wav not found");
        }

        if (!Files.exists(Paths.get("audio/register.wav"))) {
            throw new RuntimeException("audio/register.wav not found");
        }

        if (!Files.exists(Paths.get("audio/finished.wav"))) {
            throw new RuntimeException("audio/finished.wav not found");
        }
        
        if (!Files.exists(Paths.get("audio/participant_ended.wav"))) {
            throw new RuntimeException("audio/participant_ended.wav not found");
        }
        
        if (!Files.exists(Paths.get("audio/invalidlap.wav"))) {
            throw new RuntimeException("audio/invalidlap.wav not found");
        }
    }

    public void play(String filename) {
        play(filename, 1);
    }

    public static String ordinal(int i) {
        return i % 100 == 11 || i % 100 == 12 || i % 100 == 13 ? i + "th" : i + new String[]{"th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th"}[i % 10];
    }

    public void play(String filename, Integer repeat) {
        LOG.debug("request for playing audio file " + filename);
        if (!applicationConfig.isAudioPlayLocal()) {
            LOG.info("not playing audio because local audio playback is not configured");
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                AudioInputStream inputStream = null;
                try {
                    inputStream = AudioSystem.getAudioInputStream(new File(filename));
                    DataLine.Info info = new DataLine.Info(Clip.class, inputStream.getFormat());
                    Clip clip = (Clip) AudioSystem.getLine(info);
                    clip.open(inputStream);
                    clip.start();
                    if (repeat > 1) {
                        LOG.debug("playing repeated: " + repeat);
                        for (Integer i = 1; i < repeat; i++) {
                            LOG.debug("playing " + ordinal(i + 1) + " time");
                            Thread.sleep(clip.getMicrosecondLength() / 1000);
                            play(filename);
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
        play("audio/lap.wav", 3);
    }

    public void playLap() {
        play("audio/lap.wav");
    }

    public void playRegistered() {
        play("audio/register.wav");
    }

    public void playFinished() {
        play("audio/finished.wav");
    }

    public void playInvalidLap() {
        play("audio/invalidlap.wav");
    }

    public void playParticipantEnded() {
        play("audio/participant_ended.wav");
    }

}
