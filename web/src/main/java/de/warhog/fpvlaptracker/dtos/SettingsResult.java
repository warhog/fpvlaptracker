package de.warhog.fpvlaptracker.dtos;

public class SettingsResult {

    private Integer numberOfLaps;
    private String timezone;
    private Integer preparationDuration;
    private Integer overtimeDuration;
    private Integer raceDuration;
    private Integer startInterval;

    public Integer getNumberOfLaps() {
        return numberOfLaps;
    }

    public void setNumberOfLaps(Integer numberOfLaps) {
        this.numberOfLaps = numberOfLaps;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public Integer getPreparationDuration() {
        return preparationDuration;
    }

    public void setPreparationDuration(Integer preparationDuration) {
        this.preparationDuration = preparationDuration;
    }

    public Integer getOvertimeDuration() {
        return overtimeDuration;
    }

    public void setOvertimeDuration(Integer overtimeDuration) {
        this.overtimeDuration = overtimeDuration;
    }

    public Integer getRaceDuration() {
        return raceDuration;
    }

    public void setRaceDuration(Integer raceDuration) {
        this.raceDuration = raceDuration;
    }

    public Integer getStartInterval() {
        return startInterval;
    }

    public void setStartInterval(Integer startInterval) {
        this.startInterval = startInterval;
    }

    @Override
    public String toString() {
        return "SettingsResult{" + "numberOfLaps=" + numberOfLaps + ", timezone=" + timezone + ", preparationDuration=" + preparationDuration + ", overtimeDuration=" + overtimeDuration + ", raceDuration=" + raceDuration + ", startInterval=" + startInterval + '}';
    }

}
