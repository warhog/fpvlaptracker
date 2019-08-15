package de.warhog.fpvlaptracker.util;

public enum PilotState {
    WAITING_FOR_START("waiting for start"),
    WAITING_FOR_FIRST_PASS("waiting for first pass"),
    STARTED("started"),
    LAST_LAP("last lap"),
    FINISHED("finished"),
    INVALID("invalid");

    private final String text;

    private PilotState(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
