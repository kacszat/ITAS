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

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IntersectionLaneButton)) return false;

        IntersectionLaneButton that = (IntersectionLaneButton) o;

        return this.getIndex() == that.getIndex()
                && this.getLocalization() == that.getLocalization()
                && this.getType() == that.getType();
    }

    @Override
    public int hashCode() {
        int result = getIndex();
        result = 31 * result + (getLocalization() != null ? getLocalization().hashCode() : 0);
        result = 31 * result + (getType() != null ? getType().hashCode() : 0);
        return result;
    }

    public String getInfo() {
        return this.getLocalization() + "," + this.getType() + "," + this.getIndex() + "," + x + "," + y + "," + size;
    }

    public String toInfoString() {
        return getInfo(); // <- tak możesz nadpisać
    }

}