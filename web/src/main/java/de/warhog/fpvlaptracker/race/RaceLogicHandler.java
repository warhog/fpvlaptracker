package de.warhog.fpvlaptracker.race;

import de.warhog.fpvlaptracker.controllers.WebSocketController;
import de.warhog.fpvlaptracker.util.RaceType;
import de.warhog.fpvlaptracker.util.RaceState;
import de.warhog.fpvlaptracker.dtos.RaceDataResult;
import de.warhog.fpvlaptracker.dtos.ToplistEntry;
import de.warhog.fpvlaptracker.service.AudioService;
import de.warhog.fpvlaptracker.service.PilotsService;
import java.time.LocalDateTime;
import java.util.List;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RaceLogicHandler {

    private static final Logger LOG = LoggerFactory.getLogger(RaceLogicHandler.class);

    private IRaceLogic raceLogic = null;
    private RaceType raceType = RaceType.FIXED_TIME;

    @Autowired
    private PilotsService pilotsService;

    @Autowired
    private AudioService audioService;

    @Autowired
    private RaceLogicFixedTime raceLogicFixedTime;

    @Autowired
    private RaceLogicRoundBased raceLogicRoundBased;

    @Autowired
    private WebSocketController webSocketController;

    @PostConstruct
    public void postConstruct() {
        init(RaceType.FIXED_TIME);
        raceLogic = raceLogicFixedTime;
    }

    public void init(RaceType raceType) {
        this.raceType = raceType;
        if (this.raceType == RaceType.ROUND_BASED) {
            raceLogic = raceLogicRoundBased;
        } else if (this.raceType == RaceType.FIXED_TIME) {
            raceLogic = raceLogicFixedTime;
        }
        raceLogic.init();
    }

    public void startRace() {
        raceLogic.init();
        pilotsService.resetValidity();
        LOG.info("starting race");
        pilotsService.resetAllLapData();
        raceLogic.startRace();
        setStartTime(LocalDateTime.now());
    }

    public List<ToplistEntry> getToplist() {
        return raceLogic.getToplist();
    }

    public void stopRace() {
        LOG.info("stopping race");
        raceLogic.stopRace();
        audioService.speakRaceAborted();
    }

    public void addLap(Long chipId, Long duration, Integer rssi) {
        LOG.debug("add lap", chipId, duration, rssi);
        raceLogic.addLap(chipId, duration, rssi);
        webSocketController.sendReloadRaceData();
    }

    public RaceState getState() {
        return raceLogic.getState();
    }

    public RaceType getRaceType() {
        return raceType;
    }

    public void setState(RaceState state) {
        raceLogic.setState(state);
    }

    public LocalDateTime getStartTime() {
        return raceLogic.getStartTime();
    }

    private void setStartTime(LocalDateTime startTime) {
        raceLogic.setStartTime(startTime);
    }

    public RaceDataResult getRaceData() {
        RaceDataResult rdr = new RaceDataResult();
        rdr.setState(raceLogic.getState());
        rdr.setStartTime(raceLogic.getStartTime());
        rdr.setRaceType(getRaceType());
        rdr.setToplist(raceLogic.getToplist());
        rdr.setTypeSpecific(raceLogic.getRaceTypeSpecificData());
        rdr.setPilots(pilotsService.getPilotsWithNodes());
        rdr.setPilotDurations(raceLogic.getDurations());
        return rdr;
    }

}
