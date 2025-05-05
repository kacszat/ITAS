package com.itasoftware.itasoftware;

public class Vehicle {

    double speed;
    double x1, y1, x2, y2;  // Współrzędne dwóch punktów
    double dx, dy;  // Wektor ruchu
    int vehicleWidth = 15, vehicleHeight = 30;

    public Vehicle(double x1, double y1, double x2, double y2, double speed, double dx, double dy) {
        this.x1 = x1; this.y1 = y1;
        this.x2 = x2; this.y2 = y2;
        this.speed = speed;
        this.dx = dx; this.dy = dy;
    }

    public void updateVehiclePosition() {
        x1 += dx * speed;
        y1 += dy * speed;
        x2 += dx * speed;
        y2 += dy * speed;
    }

    // Sprawdzenie, czy współrzędne drugiego punktu są poza obszarem rysowania
    public boolean isVehicleOutOfBounds() {
        return (x2 < 0 || x2 > 800 || y2 < 0 || y2 > 800);
    }

    private int getVehicleWidth() {
        return vehicleWidth;
    }

    private int getVehicleHeight() {
        return vehicleHeight;
    }

}
