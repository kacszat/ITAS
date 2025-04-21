package com.itasoftware.itasoftware;
import javafx.scene.paint.Color;

import java.util.Objects;

public class IntersectionLaneButton extends IntersectionLane {

    private Localization localization;
    private Type type;
    private int index;
    private double positionCenterX;
    private double positionCenterY;
    private double x;
    private double y;
    private double size;
    private boolean isActive = false;

    // Klasa StopLine dziedziczy po IntersectionLane
    public IntersectionLaneButton(Localization localization, Type type, int index, double positionCenterX, double positionCenterY, double x, double y, double size) {
        super(localization, type, index);
        this.localization = localization;
        this.type = type;
        this.index = index;
        this.positionCenterX = positionCenterX;
        this.positionCenterY = positionCenterY;
        this.x = x;
        this.y = y;
        this.size = size;
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

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        IntersectionLaneButton that = (IntersectionLaneButton) obj;
        return this.index == that.index &&
                this.type == that.type &&
                this.localization == that.localization;
    }

    @Override
    public int hashCode() {
        return Objects.hash(index, type, localization);


    }
}