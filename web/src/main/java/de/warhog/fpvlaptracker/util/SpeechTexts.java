package de.warhog.fpvlaptracker.util;

import de.warhog.fpvlaptracker.configuration.ApplicationConfig;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SpeechTexts {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(SpeechTexts.class);

    @Autowired
    private ApplicationConfig applicationConfig;
    
    private final Map<String, String> texts = new HashMap<>();
    
    public enum TextsEnum {
        REGISTERED,
        UNREGISTERED,
        FINISHED,
        INVALID_LAP,
        PILOT_ENDED,
        CALIBRATION_DONE,
        BATTERY_LOW,
        BATTERY_SHUTDOWN,
        ALREADY_DONE,
        PILOT_START,
        FALSE_START_PILOT,
        LAST_LAP_PILOT,
        TIME_OVER_PILOT,
        PREPARE_FOR_RACE,
        GO,
        RACE_ABORTED
    };
    
    @PostConstruct
    public void speechTextsConstructor() {
        String languageFile = "lang/" + applicationConfig.getAudioLanguage();
        LOG.info("parse language file " + languageFile);
        try (BufferedReader br = Files.newBufferedReader(Paths.get(languageFile), StandardCharsets.UTF_8)) {
            String line;
            while ((line = br.readLine()) != null) {
                LOG.debug("read text line from language file: " + line);
                if (!line.startsWith("#") && !line.isEmpty()) {
                    String[] parts = line.split("=");
                    LOG.debug("add language text: " + Arrays.toString(parts));
                    texts.put(parts[0], parts[1]);
                }
            }
            boolean error = false;
            for (TextsEnum text : TextsEnum.values()) {
                if (!texts.containsKey(text.toString())) {
                    LOG.error("language key " + text + " not found in language file");
                    error = true;
                }
            }
            if (error) {
                throw new RuntimeException("invalid language file");
            }
        } catch (FileNotFoundException ex) {
            LOG.error("language file not found: " + languageFile);
            throw new RuntimeException("cannot find language file");
        } catch (IOException ex) {
            LOG.error("language file open error: " + languageFile);
            throw new RuntimeException("cannot open language file");
        }
    }

    public String getText(TextsEnum key) throws NoSuchFieldException {
        if (texts.containsKey(key.toString())) {
            return texts.get(key.toString());
        }
        throw new NoSuchFieldException("no language text for key " + key.toString() + " found.");
    }
    
}
