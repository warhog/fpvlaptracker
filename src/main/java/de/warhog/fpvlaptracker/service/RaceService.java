package de.warhog.fpvlaptracker.service;

import de.warhog.fpvlaptracker.controllers.dtos.ToplistResult;
import de.warhog.fpvlaptracker.db.DbLayerException;
import de.warhog.fpvlaptracker.db.RaceLayer;
import de.warhog.fpvlaptracker.race.RaceState;
import de.warhog.fpvlaptracker.jooq.tables.records.LapsRecord;
import de.warhog.fpvlaptracker.jooq.tables.records.ParticipantsRecord;
import de.warhog.fpvlaptracker.jooq.tables.records.RacesRecord;
import de.warhog.fpvlaptracker.race.entities.Participant;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.temporal.TemporalField;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.jooq.Record4;
import org.jooq.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RaceService {

    private static final Logger LOG = LoggerFactory.getLogger(RaceService.class);

    @Autowired
    private RaceLayer dbLayer;

    @Autowired
    private ParticipantsService participantsService;

    public Integer createRace(Integer laps, RaceState state) throws ServiceLayerException {
        try {
            return dbLayer.createRace(laps, state);
        } catch (DbLayerException ex) {
            LOG.error(ex.getMessage(), ex);
            throw new ServiceLayerException(ex);
        }
    }

    public RacesRecord getRaceRecordForId(Integer id) throws ServiceLayerException {
        try {
            return dbLayer.getRaceRecordForId(id);
        } catch (DbLayerException ex) {
            LOG.error(ex.getMessage(), ex);
            throw new ServiceLayerException(ex.getMessage());
        }
    }

    public void setState(Integer id, RaceState state) throws ServiceLayerException {
        try {
            dbLayer.setState(id, state);
        } catch (DbLayerException ex) {
            LOG.error(ex.getMessage(), ex);
            throw new ServiceLayerException(ex.getMessage());
        }
    }

    public void setStartTime(Integer id, Integer startTime) throws ServiceLayerException {
        try {
            dbLayer.setStartTime(id, startTime);
        } catch (DbLayerException ex) {
            LOG.error(ex.getMessage(), ex);
            throw new ServiceLayerException(ex.getMessage());
        }
    }

    public Integer addLap(Integer raceId, Integer chipId, Integer lap, Integer duration) throws ServiceLayerException {
        try {
            return dbLayer.addLap(raceId, chipId, lap, duration);
        } catch (DbLayerException ex) {
            LOG.error(ex.getMessage(), ex);
            throw new ServiceLayerException(ex);
        }
    }

    public List<Participant> getRaceParticipants(Integer raceId) throws ServiceLayerException {
        try {
            List<Integer> part = dbLayer.getRaceParticipants(raceId);
            List<Participant> ret = new ArrayList<>();
            for (Integer chipId : part) {
                ParticipantsRecord participantsRecord = participantsService.getParticipantRecordForChipIdFromDb(chipId);
                Participant participant = new Participant(participantsRecord.getName(), participantsRecord.getChipid(), null);
                ret.add(participant);
            }
            return ret;
        } catch (DbLayerException ex) {
            LOG.error(ex.getMessage(), ex);
            throw new ServiceLayerException(ex);
        }
    }

    public List<LapsRecord> getLaps(Integer raceId, Integer chipId) throws ServiceLayerException {
        try {
            List<LapsRecord> ret = dbLayer.getLaps(raceId, chipId);
            return ret;
        } catch (DbLayerException ex) {
            LOG.error(ex.getMessage(), ex);
            throw new ServiceLayerException(ex);
        }
    }

    public List<RacesRecord> getRaces() throws ServiceLayerException {
        try {
            return dbLayer.getRaces();
        } catch (DbLayerException ex) {
            LOG.error(ex.getMessage(), ex);
            throw new ServiceLayerException(ex);
        }
    }

    public List<ToplistResult> getToplist(Result<Record4<Integer, Integer, BigDecimal, String>> data) throws ServiceLayerException {
        List<ToplistResult> result = new ArrayList<>();
        for (Record4<Integer, Integer, BigDecimal, String> rec : data) {
            ToplistResult toplistResult = new ToplistResult();
            toplistResult.setChipId((Integer) rec.get("CHIPID"));
            toplistResult.setNumberOfTotalLaps((Integer) rec.get("NROFLAPS"));
            BigDecimal totalDurationRaw = (BigDecimal) rec.get("TOTALDURATION");
            Duration totalDuration = Duration.ZERO;
            totalDuration = totalDuration.plusMillis(totalDurationRaw.longValueExact());
            toplistResult.setTotalDuration(totalDuration);
            toplistResult.setName((String) rec.get("NAME"));
            result.add(toplistResult);
        }
        return result;
    }

    public List<ToplistResult> getToplistToday() throws ServiceLayerException {
        try {
            Integer startTime = Math.toIntExact(LocalDate.now().toEpochDay()) * 24 * 60 * 60 - 3600;
            return getToplist(dbLayer.getToplist(startTime));
        } catch (DbLayerException ex) {
            LOG.error(ex.getMessage(), ex);
            throw new ServiceLayerException(ex);
        }
    }

    public List<ToplistResult> getToplistAllTime() throws ServiceLayerException {
        try {
            return getToplist(dbLayer.getToplist(0));
        } catch (DbLayerException ex) {
            LOG.error(ex.getMessage(), ex);
            throw new ServiceLayerException(ex);
        }
    }

    public List<ToplistResult> getToplistWeek() throws ServiceLayerException {
        try {
            TemporalField temporalField = WeekFields.of(Locale.getDefault()).dayOfWeek();
            Integer startTime = Math.toIntExact(LocalDate.now().with(temporalField, 1).toEpochDay()) * 24 * 60 * 60 - 3600;
            return getToplist(dbLayer.getToplist(startTime));
        } catch (DbLayerException ex) {
            LOG.error(ex.getMessage(), ex);
            throw new ServiceLayerException(ex);
        }
    }

}
