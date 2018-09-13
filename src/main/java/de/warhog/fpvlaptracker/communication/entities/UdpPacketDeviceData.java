package de.warhog.fpvlaptracker.communication.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@JsonIgnoreProperties
public class UdpPacketDeviceData extends UdpPacket {

    private static final Logger LOG = LoggerFactory.getLogger(UdpPacketDeviceData.class);

    private Long chipid;
    private Integer frequency;
    private Integer minimumLapTime;
    private Integer triggerThreshold;
    private Integer triggerThresholdCalibration;
    private Integer calibrationOffset;
    private String state;
    private Integer triggerValue;
    private Double voltage;
    private Integer uptime;
    private Integer defaultVref;
    private Integer rssi;
    private Long loopTime;
    private Double filterRatio;
    private Double filterRatioCalibration;
    private String version;

    public Long getChipid() {
        return chipid;
    }

    public void setChipid(Long chipid) {
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

    @Override
    public String toString() {
        return "UdpPacketDeviceData{" + "chipid=" + chipid + ", frequency=" + frequency + ", minimumLapTime=" + minimumLapTime + ", triggerThreshold=" + triggerThreshold + ", triggerThresholdCalibration=" + triggerThresholdCalibration + ", calibrationOffset=" + calibrationOffset + ", state=" + state + ", triggerValue=" + triggerValue + ", voltage=" + voltage + ", uptime=" + uptime + ", defaultVref=" + defaultVref + ", rssi=" + rssi + ", loopTime=" + loopTime + ", filterRatio=" + filterRatio + ", filterRatioCalibration=" + filterRatioCalibration + ", version=" + version + '}';
    }
    
}
