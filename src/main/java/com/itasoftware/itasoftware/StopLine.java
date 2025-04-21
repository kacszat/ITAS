package com.itasoftware.itasoftware;

public class StopLine extends IntersectionLane {

    private Localization localization;
    private Type type;
    private int index;
    private double positionCenterX;
    private double positionCenterY;

    // Klasa StopLine dziedziczy po IntersectionLane
    public StopLine(Localization localization, Type type, int index, double positionCenterX, double positionCenterY) {
        super(localization, type, index);
        this.positionCenterX = positionCenterX;
        this.positionCenterY = positionCenterY;
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

    public double getPositionCenterX() {
        return positionCenterX;
    }

    public double getPositionCenterY() {
        return positionCenterY;
    }
}
