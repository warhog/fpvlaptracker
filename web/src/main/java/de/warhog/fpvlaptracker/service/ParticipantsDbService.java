package de.warhog.fpvlaptracker.service;

import de.warhog.fpvlaptracker.db.DbLayerException;
import de.warhog.fpvlaptracker.db.ParticipantsLayer;
import de.warhog.fpvlaptracker.jooq.tables.records.ParticipantsRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ParticipantsDbService {

    private static final Logger LOG = LoggerFactory.getLogger(ParticipantsDbService.class);

    @Autowired
    private ParticipantsLayer dbLayer;

    public ParticipantsRecord getParticipantRecordForChipId(Long chipId) throws ServiceLayerException {
        try {
            ParticipantsRecord pr = dbLayer.getParticipantForChipId(chipId);
            return pr;
        } catch (DbLayerException ex) {
            throw new ServiceLayerException(ex.getMessage());
        }
    }

    public String getNameForChipId(Long chipId) throws ServiceLayerException {
        ParticipantsRecord pr = getParticipantRecordForChipId(chipId);
        return pr.getName();
    }

    public void createOrUpdateParticipant(Long chipId, String name) throws ServiceLayerException {
        if (name.length() > 255) {
            name = name.substring(0, 255);
        }
        try {
            dbLayer.createOrUpdateParticipant(chipId, name);
        } catch (DbLayerException ex) {
            throw new ServiceLayerException(ex);
        }
    }

}
