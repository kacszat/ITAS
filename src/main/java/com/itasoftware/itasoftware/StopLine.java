package com.itasoftware.itasoftware;

public class StopLine extends IntersectionLane {

    private double positionCenterX;
    private double positionCenterY;

    // Klasa StopLine dziedziczy po IntersectionLane
    public StopLine(Localization localization, Type type, int index, double positionCenterX, double positionCenterY) {
        super(localization, type, index);
        this.positionCenterX = positionCenterX;
        this.positionCenterY = positionCenterY;
    }

    public double getPositionCenterX() {
        return positionCenterX;
    }

    public double getPositionCenterY() {
        return positionCenterY;
    }

    public void setPositionCenterX(double positionCenterX) {
        this.positionCenterX = positionCenterX;
    }

    public void setPositionCenterY(double positionCenterY) {
        this.positionCenterY = positionCenterY;
    }
}
