package com.itasoftware.itasoftware;

public class IntersectionLaneButton extends IntersectionLane {

    private double x;
    private double y;
    private double size;
    private boolean isActive = false;

    // Klasa StopLine dziedziczy po IntersectionLane
    public IntersectionLaneButton(Localization localization, Type type, int index, double x, double y, double size) {
        super(localization, type, index);
        this.x = x;
        this.y = y;
        this.size = size;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getSize() {
        return size;
    }

    public boolean contains(double clickX, double clickY) {
        return clickX >= x && clickX <= (x + size) &&
                clickY >= y && clickY <= (y + size);
    }

    public void toggle() {
        isActive = !isActive;
    }

    public boolean isActive() {
        return isActive;
    }
}