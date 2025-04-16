package com.itasoftware.itasoftware;

public class StopLine extends IntersectionLane {

    private Localization localization;
    private Type type;
    private int index;
    private double positionX;
    private double positionY;

    // Klasa StopLine dziedziczy po IntersectionLane
    public StopLine(Localization localization, Type type, int index, double positionX, double positionY) {
        super(localization, type, index);
        this.positionX = positionX;
        this.positionY = positionY;
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

    public double getPositionX() {
        return positionX;
    }

    public double getPositionY() {
        return positionY;
    }
}
