package com.itasoftware.itasoftware;

import javafx.geometry.Point2D;

public class Vehicle {

    double speed;
    double x1, y1, x2, y2;  // Współrzędne dwóch punktów
    static int vehicleWidth = 15, vehicleHeight = 30;
    private MovementTrajectory trajectory;
    private double distanceTraveled;

    public Vehicle(MovementTrajectory trajectory) {
        this.trajectory = trajectory;
        this.speed = 2.0;
        this.distanceTraveled = 0;
    }

    public void updateVehiclePosition() {
        double centerDistance = distanceTraveled;
        double epsilon = 0.01; // Mała wartość do wyznaczenia tangensa

        // Pozycja środka pojazdu na trajektorii
        Point2D center = trajectory.getPosition(centerDistance);
        Point2D ahead = trajectory.getPosition(centerDistance + epsilon);

        // Kierunek jazdy (wektor tangensa trajektorii)
        double dx = ahead.getX() - center.getX();
        double dy = ahead.getY() - center.getY();
        double length = Math.hypot(dx, dy);
        if (length == 0) return; // zapobiegamy dzieleniu przez 0

        // Współrzędne wektora jednostkowego
        double ux = dx / length;
        double uy = dy / length;

        // Wyznaczenie punktów tyłu i przodu względem środka
        double halfLength = vehicleHeight / 2.0;

        // Tył (środek tylnej krawędzi)
        x1 = center.getX() - ux * halfLength;
        y1 = center.getY() - uy * halfLength;

        // Przód (środek przedniej krawędzi)
        x2 = center.getX() + ux * halfLength;
        y2 = center.getY() + uy * halfLength;

        // Przemieszczenie
        distanceTraveled += speed;

        System.out.printf("centerDistance = %.2f, center = (%.2f, %.2f)%n", centerDistance, center.getX(), center.getY());
        System.out.println("Distance traveled: " + distanceTraveled);
    }

    public boolean isFinished() {
        return distanceTraveled >= trajectory.getTotalLength();
    }

    public static int getVehicleWidth() {
        return vehicleWidth;
    }

    public static int getVehicleHeight() {
        return vehicleHeight;
    }
}
