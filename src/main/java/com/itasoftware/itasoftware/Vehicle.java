package com.itasoftware.itasoftware;

import javafx.geometry.Point2D;

public class Vehicle {

    double speed, speedMax, simSpeed;
    double x1, y1, x2, y2;  // Współrzędne dwóch punktów
    static int vehicleWidth = 15, vehicleHeight = 30;
    IntersectionLane.Localization vehicleOrigin, vehicleDestination;
    private MovementTrajectory trajectory;
    private double distanceTraveled;
    private double fovX, fovY, fovRadius, fovStartAngle, fovStartAngleHalf, fovLength, fovLengthLeft; // Parametry Field Of View
    double angle;   // Kąt kierunku jazdy
    Point2D[] squareFOVCorners, squareSmallFOVCorners;  // Lista czterech punktów prostokąta
    private StopLine assignedStopLine;
    private TrafficLight assignedTrafficLight;
    private TrafficLight.Phase cachedPhase;
    public boolean isAccelerating = true;
    public boolean shouldStop = false;
    public boolean shouldSlowDown = false;
    public boolean ignoreRules = false;
    private long ignoreRulesStartTime = 0;
    public double vehicleSpawnTime = 0.0, vehicleStopLineTime = 0.0;
    public double vehicleAverageSpeed = 0;
    public boolean hasPassedStopLine = false;
    public boolean isCurrentlyStopped = false;
    public double vehicleStopTimeTotal = 0.0, currentStopTimeStartTime = 0.0;
    public int vehicleStopsCount = 0;

    public Vehicle(MovementTrajectory trajectory) {
        this.trajectory = trajectory;
        this.speed = 1 * SettingsController.speedMultiplier;
        this.speedMax = speed;
        this.simSpeed = 1.0;
        this.distanceTraveled = 0;

        setVehicleOriginAndDestination();
    }

    // Przypisanie pojazdowi kierunku pochodzenia (origin) i destynacji
    private void setVehicleOriginAndDestination() {
        Point2D firstPoint = trajectory.getPoints().getFirst();
        Point2D lastPoint = trajectory.getPoints().getLast();

        for (BorderLine bl : GeneratorController.borderLines) {
            if (bl.getPositionCenterX() == firstPoint.getX() && bl.getPositionCenterY() == firstPoint.getY()) {
                this.vehicleOrigin = bl.getLocalization();
            }
            if (bl.getPositionCenterX() == lastPoint.getX() && bl.getPositionCenterY() == lastPoint.getY()) {
                this.vehicleDestination = bl.getLocalization();
            }
        }
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
        distanceTraveled += (speed * simSpeed);

        angle = Math.toDegrees(Math.atan2(uy, ux)); // Kąt kierunku jazdy
        setFOV(center, angle);
        setSquareFOV(center, angle, false);
        setSquareFOV(center, angle, true);
    }

    public void setFOV(Point2D center, double angleDegrees) {
        this.fovX = center.getX();
        this.fovY = center.getY();
        this.fovRadius = 210;  // Promień pola widzenia
        this.fovLength = 180;  // Kąt pola widzenia
        this.fovLengthLeft = 110;  // ąt pola widzenia lewego pola
        this.fovStartAngle = - angleDegrees - 90; // Ustawienie początku sektora FOV
        this.fovStartAngleHalf = angleDegrees - 90 - fovLengthLeft; // Ustawienie początku sektora FOV
    }

    public void setSquareFOV(Point2D center, double angleDegrees, Boolean smallFOV) {
        double rectLength;                      // Długość FOV
        double rectWidth = vehicleWidth * 1.4;  // Szerokość FOV
        rectLength = !smallFOV ? 160 : 40;

        // Wektor kierunku jazdy
        double angleRad = Math.toRadians(angleDegrees);
        double ux = Math.cos(angleRad);
        double uy = Math.sin(angleRad);

        // Wektor normalny (prostopadły)
        double nx = -uy;
        double ny = ux;

        // Środek prostokąta przesunięty do przodu
        double cx = center.getX() + ux * (rectLength / 2);
        double cy = center.getY() + uy * (rectLength / 2);

        // Rogi prostokąta
        double dx = rectLength / 2.0;
        double dy = rectWidth / 2.0;

        Point2D frontLeft  = new Point2D(cx + ux * dx + nx * dy, cy + uy * dx + ny * dy);
        Point2D frontRight = new Point2D(cx + ux * dx - nx * dy, cy + uy * dx - ny * dy);
        Point2D backLeft   = new Point2D(cx - ux * dx + nx * dy, cy - uy * dx + ny * dy);
        Point2D backRight  = new Point2D(cx - ux * dx - nx * dy, cy - uy * dx - ny * dy);

        if (!smallFOV) {
            squareFOVCorners = new Point2D[]{backLeft, frontLeft, frontRight, backRight};
        } else {
            squareSmallFOVCorners = new Point2D[]{backLeft, frontLeft, frontRight, backRight};
        }

    }

    // Funkcja sprawdzająca, czy dane punkty znajdują się w FOV
    public boolean isPointInFOV(double px, double py, boolean halfFOV) {
        double dx = px - fovX;
        double dy = py - fovY;
        double distance = Math.hypot(dx, dy);
        double radius = fovRadius;

        if (distance > radius) return false;

        // Obliczenie kąta FOV w zależności od parametru
        double fovAngle = halfFOV ? fovLength / 2.0 : fovLength;

        // Obliczenie kąta do punktu
        double angleToPoint = Math.toDegrees(Math.atan2(dy, dx));   // Kąt w radianach
        angleToPoint = (angleToPoint + 360) % 360;  // Kąt jest unormowany do zakresu od 0 do 360 stopni

        double normalizedAngle = (angle + 360) % 360;   // Normalizacja kąta kierunku jazdy do zakresu 0–360 stopni
        double fovStartAngle_Real = (normalizedAngle - fovAngle / 2.0 + 360) % 360; // Obliczenie rzeczywistego kąta początkowego FOV
                                                                                    // (dla części relacji położenie rzeczywistego FOV nie pokrywa się z rysowanym)

        // Wyznaczenie zakresu FOV
        double start = (fovStartAngle_Real + 360) % 360;
        double end = (start + fovAngle) % 360;

        // Sprawdzenie, czy punkt mieści się w FOV
        if (start < end) {
            return angleToPoint >= start && angleToPoint <= end;
        } else {
            // zakres przechodzi przez 0°
            return angleToPoint >= start || angleToPoint <= end;
        }
    }

    public boolean isPointInSquareFOV(double px, double py, Boolean smallFOV) {
        Point2D point = new Point2D(px, py);
        Point2D[] polygon = !smallFOV ? squareFOVCorners : squareSmallFOVCorners;
        return isPointInPolygon(point, polygon);
    }

    private boolean isPointInPolygon(Point2D point, Point2D[] polygon) {
        boolean inside = false;
        int j = polygon.length - 1;

        for (int i = 0; i < polygon.length; i++) {
            double xi = polygon[i].getX();
            double yi = polygon[i].getY();
            double xj = polygon[j].getX();
            double yj = polygon[j].getY();

            boolean intersect = ((yi > point.getY()) != (yj > point.getY())) &&
                    (point.getX() < (xj - xi) * (point.getY() - yi) / (yj - yi) + xi);
            if (intersect) inside = !inside;

            j = i;
        }

        return inside;
    }

    public boolean isOnIntersectionSegment() {
        double sl1 = trajectory.getDistanceToApproximatePoint(MovementTrajectory.stopLine1);
        double sl2 = trajectory.getDistanceToApproximatePoint(MovementTrajectory.stopLine2);

        // Obsługa przypadku, gdy punkty są w odwrotnej kolejności
        double fromSL = Math.min(sl1, sl2);
        double toSL = Math.max(sl1, sl2);

        return distanceTraveled >= fromSL && distanceTraveled <= toSL;
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

    public MovementTrajectory getTrajectory() {
        return trajectory;
    }

    public double getDistanceTraveled() {
        return distanceTraveled;
    }

    // Prędkość działania symulacji
    public void setSimSpeed(double simSpeed) {
        this.simSpeed = simSpeed;
    }

    public double getSimSpeed() {
        return simSpeed;
    }

    // Prędkość pojazdu
    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public double getSpeed() {
        return speed;
    }

    public double getFovX() {
        return fovX;
    }

    public double getFovY() {
        return fovY;
    }

    public double getFovRadius() {
        return fovRadius;
    }

    public double getFovLengthLeft() {
        return fovLengthLeft;
    }

    public double getFovStartAngle() {
        return fovStartAngle;
    }

    public double getFovStartAngleHalf() {
        return fovStartAngleHalf;
    }

    public double getFovLength() {
        return fovLength;
    }

    public IntersectionLane.Localization getVehicleOrigin() {
        return vehicleOrigin;
    }

    public IntersectionLane.Localization getVehicleDestination() {
        return vehicleDestination;
    }

    public void assignStopLine(StopLine sl) {
        this.assignedStopLine = sl;
    }

    public StopLine getAssignedStopLine() {
        return assignedStopLine;
    }

    public boolean hasAssignedStopLine() {
        return assignedStopLine != null;
    }

    public void assignTrafficLight(TrafficLight tl) {
        this.assignedTrafficLight = tl;
    }

    public TrafficLight getAssignedTrafficLight() {
        return assignedTrafficLight;
    }

    public boolean hasAssignedTrafficLight() {
        return assignedTrafficLight != null;
    }

    public void updateCachedTrafficLightPhase() {
        if (assignedTrafficLight != null) {
            this.cachedPhase = assignedTrafficLight.getCurrentPhase();
        }
    }

    public TrafficLight.Phase getCachedPhase() {
        return this.cachedPhase;
    }

    public boolean isAccelerating() {
        return isAccelerating;
    }

    public boolean getShouldStop() {
        return shouldStop;
    }

    public boolean getShouldSlowDown() {
        return shouldSlowDown;
    }

    public void setIgnoreRules(long currentTime) {
        this.ignoreRules = true;
        this.ignoreRulesStartTime = currentTime;
    }

    public void updateIgnoreRules(long currentTime, long durationMs) {
        if (ignoreRules && (currentTime - ignoreRulesStartTime > durationMs)) {
            this.ignoreRules = false;
        }
    }

    public void setVehicleSpawnTime(long currentTime) {
        this.vehicleSpawnTime = currentTime;
    }

    public void setVehicleStopLineTime(long currentTime) {
        this.vehicleStopLineTime = currentTime;
    }

    public void calculateVehicleAverageSpeed() {    // Obliczenie średniej prędkości
        double distanceInMeters = distanceTraveled / 25 * 3.5;
        double distanceTimeInSeconds = (vehicleStopLineTime - vehicleSpawnTime) / 1000;
        vehicleAverageSpeed = distanceInMeters / distanceTimeInSeconds * 3.6;   // Wynik w km/h
    }

    public void updateStopTimeTotalValue(long currentTime) { // Obliczenie łącznego czasu zatrzymania pojazdu
        // Jeśli pojazd zatrzymał się
        if (shouldStop && !isCurrentlyStopped) {
            currentStopTimeStartTime = currentTime;
            isCurrentlyStopped = true;
            vehicleStopsCount++;    // Liczba zatrzymań pojazdu
        }

        // Jeśli pojazd zaczął się poruszać po zatrzymaniu
        if (!shouldStop && isCurrentlyStopped) {
            if (currentStopTimeStartTime != 0) {
                vehicleStopTimeTotal += (currentTime - currentStopTimeStartTime);
            }
            currentStopTimeStartTime = 0;
            isCurrentlyStopped = false;
        }
    }

    public double getVehicleStopTimeTotal() {
        return (vehicleStopTimeTotal / 1000);  // Czas w sekundach
    }

    public int getVehicleStopsCount() {
        return vehicleStopsCount;   // Liczba zatrzymań pojazdu
    }

    public double getVehicleAverageSpeed() {
        return vehicleAverageSpeed;
    }

    public void setHasPassedStopLine() {
        this.hasPassedStopLine = true;
    }

}
