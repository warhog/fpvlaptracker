package de.warhog.fpvlaptracker.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@JsonIgnoreProperties
public class ParticipantDeviceData {

    private static final Logger LOG = LoggerFactory.getLogger(ParticipantDeviceData.class);

    private Integer frequency = 0;
    private Integer minimumLapTime = 0;
    private Integer triggerThreshold = 0;
    private Integer triggerThresholdCalibration = 0;
    private Integer calibrationOffset = 0;
    private String state = "unknown";
    private Integer triggerValue = 0;
    private Double voltage = 0.0;
    private Integer uptime = 0;
    private Integer defaultVref = 0;
    private Integer rssi = 0;
    private Long loopTime = 0L;
    private Double filterRatio = 0.0;
    private Double filterRatioCalibration = 0.0;
    private String version = "unknown";
    private String ipAddress = "0.0.0.0";
    private String participantName = "-";
    private Long chipid = 0L;

    public ParticipantDeviceData() {
    }

    public ParticipantDeviceData(Integer frequency, Integer minimumLapTime, Integer triggerThreshold, Integer triggerThresholdCalibration, Integer calibrationOffset, String state, Integer triggerValue, Double voltage, Integer uptime, Integer defaultVref, Integer rssi, Long loopTime, Double filterRatio, Double filterRatioCalibration, String version, String ipAddress, String name, Long chipid) {
        this.frequency = frequency;
        this.minimumLapTime = minimumLapTime;
        this.triggerThreshold = triggerThreshold;
        this.triggerThresholdCalibration = triggerThresholdCalibration;
        this.calibrationOffset = calibrationOffset;
        this.state = state;
        this.triggerValue = triggerValue;
        this.voltage = voltage;
        this.uptime = uptime;
        this.defaultVref = defaultVref;
        this.rssi = rssi;
        this.loopTime = loopTime;
        this.filterRatio = filterRatio;
        this.filterRatioCalibration = filterRatioCalibration;
        this.version = version;
        this.ipAddress = ipAddress;
        this.participantName = name;
        this.chipid = chipid;
    }
    
    public Integer getFrequency() {
        return frequency;
    }

    public void setFrequency(Integer frequency) {
        this.frequency = frequency;
    }

    public Integer getMinimumLapTime() {
        return minimumLapTime;
    }

    public void setMinimumLapTime(Integer minimumLapTime) {
        this.minimumLapTime = minimumLapTime;
    }

    public Integer getTriggerThreshold() {
        return triggerThreshold;
    }

    public void setTriggerThreshold(Integer triggerThreshold) {
        this.triggerThreshold = triggerThreshold;
    }

    public Integer getTriggerThresholdCalibration() {
        return triggerThresholdCalibration;
    }

    public void setTriggerThresholdCalibration(Integer triggerThresholdCalibration) {
        this.triggerThresholdCalibration = triggerThresholdCalibration;
    }

    public Integer getCalibrationOffset() {
        return calibrationOffset;
    }

    public void setCalibrationOffset(Integer calibrationOffset) {
        this.calibrationOffset = calibrationOffset;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Integer getTriggerValue() {
        return triggerValue;
    }

    public void setTriggerValue(Integer triggerValue) {
        this.triggerValue = triggerValue;
    }

    public Double getVoltage() {
        return voltage;
    }

    public void setVoltage(Double voltage) {
        this.voltage = voltage;
    }

    public Integer getUptime() {
        return uptime;
    }

    public void setUptime(Integer uptime) {
        this.uptime = uptime;
    }

    public Integer getDefaultVref() {
        return defaultVref;
    }

    public void setDefaultVref(Integer defaultVref) {
        this.defaultVref = defaultVref;
    }

    public Integer getRssi() {
        return rssi;
    }

    public void setRssi(Integer rssi) {
        this.rssi = rssi;
    }

    public Long getLoopTime() {
        return loopTime;
    }

    public void setLoopTime(Long loopTime) {
        this.loopTime = loopTime;
    }

    public Double getFilterRatio() {
        return filterRatio;
    }

    public void setFilterRatio(Double filterRatio) {
        this.filterRatio = filterRatio;
    }

    public Double getFilterRatioCalibration() {
        return filterRatioCalibration;
    }

    public void setFilterRatioCalibration(Double filterRatioCalibration) {
        this.filterRatioCalibration = filterRatioCalibration;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getParticipantName() {
        return participantName;
    }

    public void setParticipantName(String participantName) {
        this.participantName = participantName;
    }

    public Long getChipid() {
        return chipid;
    }

    public void setChipid(Long chipid) {
        this.chipid = chipid;
    }

    @Override
    public String toString() {
        return "ParticipantDeviceData{" + "frequency=" + frequency + ", minimumLapTime=" + minimumLapTime + ", triggerThreshold=" + triggerThreshold + ", triggerThresholdCalibration=" + triggerThresholdCalibration + ", calibrationOffset=" + calibrationOffset + ", state=" + state + ", triggerValue=" + triggerValue + ", voltage=" + voltage + ", uptime=" + uptime + ", defaultVref=" + defaultVref + ", rssi=" + rssi + ", loopTime=" + loopTime + ", filterRatio=" + filterRatio + ", filterRatioCalibration=" + filterRatioCalibration + ", version=" + version + ", ipAddress=" + ipAddress + ", participantName=" + participantName + ", chipid=" + chipid + '}';
    }
    
}
