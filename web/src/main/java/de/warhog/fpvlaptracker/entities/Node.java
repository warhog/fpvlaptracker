package de.warhog.fpvlaptracker.entities;

import de.warhog.fpvlaptracker.dtos.Rssi;
import de.warhog.fpvlaptracker.dtos.StringResult;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.net.InetAddress;
import java.time.Duration;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;

@JsonIgnoreProperties
public class Node {

    private static final Logger LOG = LoggerFactory.getLogger(Node.class);

    private RestTemplate restTemplate = null;
    
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
    private InetAddress inetAddress = null;
    private Long chipId = 0L;

    public Node() {
    }
    
    public Node(Long chipId, InetAddress inetAddress) {
        this.chipId = chipId;
        this.inetAddress = inetAddress;
    }

    public Node(Integer frequency, Integer minimumLapTime, Integer triggerThreshold, Integer triggerThresholdCalibration, Integer calibrationOffset, String state, Integer triggerValue, Double voltage, Integer uptime, Integer defaultVref, Integer rssi, Long loopTime, Double filterRatio, Double filterRatioCalibration, String version, InetAddress inetAddress, Long chipid) {
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
        this.inetAddress = inetAddress;
        this.chipId = chipid;
    }

    public void copyFromNode(Node node) {
        this.frequency = node.getFrequency();
        this.minimumLapTime = node.getMinimumLapTime();
        this.triggerThreshold = node.getTriggerThreshold();
        this.triggerThresholdCalibration = node.getTriggerThresholdCalibration();
        this.calibrationOffset = node.getCalibrationOffset();
        this.state = node.getState();
        this.triggerValue = node.getTriggerValue();
        this.voltage = node.getVoltage();
        this.uptime = node.getUptime();
        this.defaultVref = node.getDefaultVref();
        this.rssi = node.getRssi();
        this.loopTime = node.getLoopTime();
        this.filterRatio = node.getFilterRatio();
        this.filterRatioCalibration = node.getFilterRatioCalibration();
        this.version = node.getVersion();
        this.inetAddress = node.getInetAddress();
        this.chipId = node.getChipId();
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

    public StringResult postState(String state) {
        try {
            DeviceStates deviceStateFound = null;
            for (DeviceStates deviceState : DeviceStates.values()) {
                if (deviceState.name().toUpperCase().equals(state.toUpperCase())) {
                    deviceStateFound = deviceState;
                    break;
                }
            }
            if (deviceStateFound == null) {
                LOG.error("device state not found: " + state);
                throw new RuntimeException("invalid state given: " + state);
            }
            this.state = state;
            return getRestTemplate().getForObject(buildUrl(getInetAddress(), "api/setstate") + "?state={state}", StringResult.class, state.toUpperCase());
        } catch (Exception ex) {
            LOG.error("cannot set state: " + ex.getMessage(), ex);
            return new StringResult("NOK");
        }
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

    public InetAddress getInetAddress() {
        return inetAddress;
    }

    public void setInetAddress(InetAddress inetAddress) {
        this.inetAddress = inetAddress;
    }

    
    public Long getChipId() {
        return chipId;
    }

    public void setChipId(Long chipId) {
        this.chipId = chipId;
    }
    
   private String buildUrl(InetAddress inetAddress, String uri) {
       if (inetAddress == null) {
           LOG.warn("call to buildUrl with inetAddress being null");
           return "";
       }
        return "http://" + inetAddress.getHostAddress() + "/" + uri;
    }

    private RestTemplate getRestTemplate() {
        if (restTemplate == null) {
            RestTemplateBuilder restTemplateBuilder = new RestTemplateBuilder();
            restTemplate = restTemplateBuilder
                    .setConnectTimeout(Duration.ofSeconds(5L))
                    .setReadTimeout(Duration.ofSeconds(5L))
                    .build();
        }
        return restTemplate;
    }

    @SuppressFBWarnings(value = "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE", justification = "null pointer dereferencing is intended")
    public Rssi loadRssi() {
        try {
            Rssi rssi = getRestTemplate().getForObject(buildUrl(getInetAddress(), "api/rssi"), Rssi.class);
            this.rssi = rssi.getRssi();
            return rssi;
        } catch (Exception ex) {
            LOG.error("cannot load rssi: " + ex.getMessage(), ex);
            return new Rssi();
        }
    }

    @SuppressFBWarnings(value = "NP_NULL_ON_SOME_PATH", justification = "null pointer dereferencing is intended")
    public void loadNodeDeviceData() {
        try {
            Node node = getRestTemplate().getForObject(buildUrl(getInetAddress(), "api/devicedata"), Node.class);
            if (node != null) {
                LOG.debug("loaded from node: " + node.toString());
            }
            node.setInetAddress(this.getInetAddress());
            node.setChipId(this.getChipId());
            copyFromNode(node);
        } catch (Exception ex) {
            LOG.error("cannot load device data: " + ex.getMessage(), ex);
        }
    }

    public StringResult rebootNode() {
        LOG.debug("rebooting node");
        try {
            return new StringResult(getRestTemplate().getForObject(buildUrl(getInetAddress(), "api/reboot"), String.class));
        } catch (Exception ex) {
            LOG.error("cannot reboot device: " + ex.getMessage(), ex);
            return new StringResult(StringResult.NOK);
        }
    }

    public StringResult restoreNodeFactoryDefaults() {
        LOG.debug("restoring node factory defaults");
        try {
            return new StringResult(getRestTemplate().getForObject(buildUrl(getInetAddress(), "api/factorydefaults"), String.class));
        } catch (Exception ex) {
            LOG.error("cannot restore factory defaults on device: " + ex.getMessage(), ex);
            return new StringResult(StringResult.NOK);
        }
    }

    public StringResult postNodeDeviceData() {
        try {
            LOG.debug("posting node data: " + toString());
            String ret = getRestTemplate().postForObject(buildUrl(getInetAddress(), "api/devicedata"), this, String.class);
            if (ret != null) {
                if (ret.trim().contains(StringResult.NOK)) {
                    throw new RuntimeException("cannot set devicedata");
                }
            }
            return new StringResult(ret);
        } catch (Exception ex) {
            LOG.error("cannot post device data: " + ex.getMessage(), ex);
            return new StringResult(StringResult.NOK);
        }
    }

    @Override
    public String toString() {
        return "Node{" + "frequency=" + frequency + ", minimumLapTime=" + minimumLapTime + ", triggerThreshold=" + triggerThreshold + ", triggerThresholdCalibration=" + triggerThresholdCalibration + ", calibrationOffset=" + calibrationOffset + ", state=" + state + ", triggerValue=" + triggerValue + ", voltage=" + voltage + ", uptime=" + uptime + ", defaultVref=" + defaultVref + ", rssi=" + rssi + ", loopTime=" + loopTime + ", filterRatio=" + filterRatio + ", filterRatioCalibration=" + filterRatioCalibration + ", version=" + version + ", inetAddress=" + inetAddress + ", chipid=" + chipId + '}';
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 37 * hash + Objects.hashCode(this.chipId);
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
        final Node other = (Node) obj;
        if (!Objects.equals(this.chipId, other.chipId)) {
            return false;
        }
        return true;
    }

    
    
}
