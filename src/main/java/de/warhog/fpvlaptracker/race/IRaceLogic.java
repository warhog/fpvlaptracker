package de.warhog.fpvlaptracker.race;

public interface IRaceLogic {

    void init();
    void startRace();
    void stopRace();
    void addLap(Long chipId, Long duration, Integer rssi);
    
}
