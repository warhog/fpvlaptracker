package de.warhog.fpvlaptracker.communication;

public enum PacketType {
    REGISTER32,
    REGISTERLED,
    REGISTERRESPONSE,
    REGISTERREQUEST,
    LAP,
    CALIBRATIONDONE,
    MESSAGE,
    BATTERY_LOW,
    BATTERY_SHUTDOWN,
    RSSI,
    SCAN
}
