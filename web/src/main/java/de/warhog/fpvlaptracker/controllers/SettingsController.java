package de.warhog.fpvlaptracker.controllers;

import de.warhog.fpvlaptracker.configuration.ApplicationConfig;
import de.warhog.fpvlaptracker.dtos.SettingsResult;
import de.warhog.fpvlaptracker.dtos.StatusResult;
import de.warhog.fpvlaptracker.service.ConfigService;
import de.warhog.fpvlaptracker.service.ServiceLayerException;
import de.warhog.fpvlaptracker.util.ShutdownUtil;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/settings")
public class SettingsController {

    private static final Logger LOG = LoggerFactory.getLogger(SettingsController.class);

    @Autowired
    private ConfigService configService;

    @Autowired
    private ApplicationConfig applicationConfig;

    @RequestMapping(path = "/data", method = RequestMethod.GET)
    public SettingsResult loadSettings() {
        final SettingsResult result = new SettingsResult();
        try {
            result.setNumberOfLaps(configService.getNumberOfLaps());
            result.setPreparationDuration(configService.getPreparationDuration());
            result.setRaceDuration(configService.getRaceDuration());
            result.setOvertimeDuration(configService.getOvertimeDuration());
            result.setStartInterval(configService.getStartInterval());
            result.setTimezone(configService.getTimezone());
        } catch (ServiceLayerException ex) {
            LOG.error("service layer exception: " + ex.getMessage(), ex);
        }
        return result;
    }

    @RequestMapping(path = "/data", method = RequestMethod.POST)
    public StatusResult storeSettings(@RequestBody SettingsResult settingsResult) {
        LOG.error("storeSettings " + settingsResult.toString());
        try {
            configService.setTimezone(settingsResult.getTimezone());
            configService.setNumberOfLaps(settingsResult.getNumberOfLaps());
            configService.setOvertimeDuration(settingsResult.getOvertimeDuration());
            configService.setPreparationTime(settingsResult.getPreparationDuration());
            configService.setRaceDuration(settingsResult.getRaceDuration());
            configService.setStartInterval(settingsResult.getStartInterval());
        } catch (ServiceLayerException ex) {
            LOG.error("cannot store settings: " + ex.getMessage(), ex);
            return new StatusResult(StatusResult.Status.NOK);
        }
        return new StatusResult(StatusResult.Status.OK);
    }

    @SuppressFBWarnings(value = "DM_EXIT", justification = "graceful shutdown from webui")
    @RequestMapping(path = "/shutdown", method = RequestMethod.GET)
    public void shutdown() {
        LOG.info("received shutdown");
        if (applicationConfig.isShutdownMachine()) {
            try {
                LOG.debug("shutting down machine");
                ShutdownUtil.shutdown();
            } catch (Exception ex) {
                LOG.error("could not shutdown machine: " + ex.getMessage(), ex);
            }
        }
        LOG.info("shutting down application");
        System.exit(0);
    }

}
