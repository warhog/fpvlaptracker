package de.warhog.fpvlaptracker.entities;

public class ToplistEntry {

    private String name;
    private Long totalLapTime;
    private Integer laps;

    public ToplistEntry() {
    }

    public ToplistEntry(String name, Long totalLapTime, Integer laps) {
        this.name = name;
        this.totalLapTime = totalLapTime;
        this.laps = laps;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getTotalLapTime() {
        return totalLapTime;
    }

    public void setTotalLapTime(Long totalLapTime) {
        this.totalLapTime = totalLapTime;
    }

    public Integer getLaps() {
        return laps;
    }

    public void setLaps(Integer laps) {
        this.laps = laps;
    }

    @Override
    public String toString() {
        return "ToplistEntry{" + "name=" + name + ", totalLapTime=" + totalLapTime + ", laps=" + laps + '}';
    }

}
