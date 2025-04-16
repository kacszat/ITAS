package com.itasoftware.itasoftware;

public class SliderLane {
    public enum Localization { NORTH, SOUTH, EAST, WEST }
    public enum Type { ENTRY, EXIT }

    private Localization localization;
    private Type type;

    public SliderLane(Localization localization, Type type) {
        this.localization = localization;
        this.type = type;
    }

    public Localization getLocalization() {
        return localization;
    }

    public Type getType() {
        return type;
    }
}

