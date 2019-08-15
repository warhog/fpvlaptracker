package de.warhog.fpvlaptracker.entities;

import de.warhog.fpvlaptracker.util.PilotState;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Pilot {

    private static final Logger LOG = LoggerFactory.getLogger(Pilot.class);

    private String name;
    private boolean valid = true;
    private Node node = null;
    /**
     * @hidden chipId is for internal usage only (detection of non connected nodes together with db startup)
    */
    private Long chipId = null;
    private final LapTimeList lapTimeList = new LapTimeList();
    private PilotState state = PilotState.WAITING_FOR_START;

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
        if (!valid) {
            this.state = PilotState.INVALID;
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Node getNode() {
        return node;
    }

    public void setNode(Node node) {
        this.node = node;
    }

    public PilotState getState() {
        return state;
    }

    public void setState(PilotState state) {
        this.state = state;
    }

    /**
     * @deprecated chipId is for internal usage only (detection of non connected nodes together with db startup)
     * @return chipId
    */
    public Long getChipId() {
        return chipId;
    }

    /**
     * @param chipId
     * @deprecated chipId is for internal usage only (detection of non connected nodes together with db startup)
    */
    public void setChipId(Long chipId) {
        this.chipId = chipId;
    }

    @Override
    public String toString() {
        return "Pilot{" + "name=" + name + ", valid=" + valid + ", node=" + node + ", chipId=" + chipId + ", lapTimeList=" + lapTimeList + ", state=" + state + '}';
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + Objects.hashCode(this.name);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Pilot other = (Pilot) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        return true;
    }

    public void addLap(final Long duration, final Integer rssi) {
        addLap(duration, rssi, false);
    }

    public void addLap(final Long duration, final Integer rssi, boolean ignoreFirstLap) {
        LOG.debug("adding lap with duration " + duration + " and rssi " + rssi);
        lapTimeList.addLap(duration, rssi, ignoreFirstLap);
    }

    public void invalidateLap(final Integer lap, final boolean state) {
        lapTimeList.invalidateLap(lap, state);
    }

    public void setLapValid(final Integer lap, final boolean valid) {
        lapTimeList.invalidateLap(lap, valid);
    }

    public void toggleLapValidity(final Integer lap) {
        if (lapTimeList.isLapValid(lap)) {
            LOG.debug("set lap invalid");
            lapTimeList.invalidateLap(lap, true);
        } else {
            LOG.debug("set lap valid");
            lapTimeList.invalidateLap(lap, false);
        }
    }

    public LapTimeList getLapTimeList() {
        return lapTimeList;
    }

    public void resetLapData() {
        this.lapTimeList.reset();
    }

    public boolean isStateWaitingForStart() {
        return state == PilotState.WAITING_FOR_START;
    }

    public boolean isStateWaitingForFirstPass() {
        return state == PilotState.WAITING_FOR_FIRST_PASS;
    }

    public boolean isStateStarted() {
        return state == PilotState.STARTED;
    }

    public boolean isStateLastLap() {
        return state == PilotState.LAST_LAP;
    }

    public boolean isStateFinished() {
        return state == PilotState.FINISHED;
    }

    public boolean isStateInvalid() {
        return state == PilotState.INVALID;
    }

    public boolean hasNode() {
        return getNode() != null;
    }

    public boolean isReady() {
        return hasNode() && isValid();
    }
    
}
