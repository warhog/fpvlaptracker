package de.warhog.fpvlaptracker.service;

import de.warhog.fpvlaptracker.db.DbLayerException;
import de.warhog.fpvlaptracker.db.ProfilesDbLayer;
import de.warhog.fpvlaptracker.entities.Pilot;
import de.warhog.fpvlaptracker.dtos.Profile;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ProfilesService {

    private static final Logger LOG = LoggerFactory.getLogger(ProfilesService.class);

    @Autowired
    private ProfilesDbLayer dbLayer;

    public void createOrUpdateProfile(Long chipId, String name, String data) throws ServiceLayerException {
        try {
                dbLayer.createOrUpdateProfile(chipId, name, data);
        } catch (DbLayerException ex) {
            throw new ServiceLayerException(ex);
        }
    }

    public void deleteProfile(Long chipId, String name) throws ServiceLayerException {
        try {
            dbLayer.deleteProfile(chipId, name);
        } catch (DbLayerException ex) {
            throw new ServiceLayerException(ex);
        }
    }

    public Profile getProfile(Pilot pilot, String name) throws ServiceLayerException {
        return getProfile(pilot.getNode().getChipId(), name);
    }

    public Profile getProfile(Long chipId, String name) throws ServiceLayerException {
        try {
            return dbLayer.getProfileForChipIdWithName(chipId, name);
        } catch (DbLayerException ex) {
            throw new ServiceLayerException(ex);
        }
    }

    public List<Profile> getProfiles(Pilot pilot) throws ServiceLayerException {
        return getProfiles(pilot.getNode().getChipId());
    }

    public List<Profile> getProfiles(Long chipId) throws ServiceLayerException {
        try {
            return dbLayer.getProfilesForChipId(chipId);
        } catch (DbLayerException ex) {
            throw new ServiceLayerException(ex);
        }
    }

}
