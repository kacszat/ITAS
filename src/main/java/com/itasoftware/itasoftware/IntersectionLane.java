package com.itasoftware.itasoftware;

public class IntersectionLane {
    public enum Localization { NORTH, SOUTH, EAST, WEST }
    public enum Type { ENTRY, EXIT }

    private Localization localization;
    private Type type;
    private int index; // pozycja względem środka (np. 0 - najbliżej środka, 1, 2, ...)

    public IntersectionLane(Localization localization, Type type, int index) {
        this.localization = localization;
        this.type = type;
        this.index = index;
    }

    public Localization getLocalization() {
        return localization;
    }

    public Type getType() {
        return type;
    }

    public int getIndex() {
        return index;
    }
}
