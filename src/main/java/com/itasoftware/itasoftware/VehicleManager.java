package com.itasoftware.itasoftware;

import javafx.geometry.Point2D;

import java.util.ArrayList;
import java.util.List;

public class VehicleManager {
    private final List<Vehicle> vehiclesList = new ArrayList<>();

    public void spawnVehicle(MovementTrajectory traj) {
        vehiclesList.add(new Vehicle(traj));
    }

    public void updateVehicles(double simSpeed) {
        for (Vehicle v : vehiclesList) {
            v.setSimSpeed(simSpeed);
            v.updateVehiclePosition();
            checkVehicleContact(v);
        }
        vehiclesList.removeIf(Vehicle::isFinished);     // Pojazd zostaje usunięty, jeśli dotarł do końca trasy
    }

    public List<Vehicle> getVehicles() {
        return vehiclesList;
    }

    public void clearVehicles() {
        vehiclesList.clear();
    }

    // Funkcja sprawdzająca, czy dany pojazd nie jest w kontakcie z innymi i jak powinien się zachować
    public void checkVehicleContact(Vehicle vehicle) {
        boolean shouldStop = false, shouldSlowDown = false;

        for (Vehicle other : vehiclesList) {
            if (vehicle == other) continue; // Jeśli zadany pojazd jest taki sam, jak wskazany z pętli, pomijamy

            // Pomocniczne booleany, zwiększające czytelność warunków
            boolean inBigFOV = vehicle.isPointInFOV(other.getFovX(), other.getFovY(), false);
            boolean inSmallFOV = vehicle.isPointInFOV(other.getFovX(), other.getFovY(), true);
            boolean areTrajectoriesIntersect = MovementTrajectory.doTrajectoriesIntersect(vehicle.getTrajectory(), other.getTrajectory());
            // Pojazd skręcajacy w lewo udostępnia pierwszeństwa pojazdom jadącym prosto i skręcającym w prawo
            boolean isVehicleGoingLeftAndGivingWay = vehicle.isOnIntersectionSegment() && isVehicleTurningLeft(vehicle) && isOtherGoingFromOppositeOrigin(vehicle, other) &&
                    (isVehicleGoingStraight(other) || isVehicleTurningRight(other));
            boolean inSquareFOV = vehicle.isPointInSquareFOV(other.getFovX(), other.getFovY(), false);
            boolean inSmallSquareFOV = vehicle.isPointInSquareFOV(other.getFovX(), other.getFovY(), true);
            boolean isVehicleGivingWayToRight = isOtherGoingFromRight(vehicle, other) && !vehicle.isOnIntersectionSegment();

            // Zasady ruchu drogowego
            if (inSmallSquareFOV) {
                shouldStop = true;
                break;
            } else if (inSquareFOV && areTrajectoriesIntersect && isVehicleGoingLeftAndGivingWay) {
                shouldStop = true;
                break;
            } else if (inSquareFOV) {
                shouldSlowDown = true;
            } else if (inBigFOV && areTrajectoriesIntersect) {
                if (isVehicleGivingWayToRight || isVehicleGoingLeftAndGivingWay) {
                    shouldSlowDown = true;
                }
            }
        }

        // Modyfikacja prędkości pojazdu
        if (shouldStop) {
            vehicle.setSpeed(0);
        } else if (shouldSlowDown) {
            decreaseSpeed(vehicle);
        } else {
            increaseSpeed(vehicle);
        }
    }

    public boolean isOtherGoingFromRight(Vehicle vehicle1, Vehicle vehicle2) {  // Sprawdzenie, czy drugi pojazd nie nadjeżdża z prawej
        return ((vehicle1.getVehicleOrigin() == IntersectionLane.Localization.NORTH && vehicle2.getVehicleOrigin() == IntersectionLane.Localization.WEST) ||
            (vehicle1.getVehicleOrigin() == IntersectionLane.Localization.WEST && vehicle2.getVehicleOrigin() == IntersectionLane.Localization.SOUTH) ||
            (vehicle1.getVehicleOrigin() == IntersectionLane.Localization.SOUTH && vehicle2.getVehicleOrigin() == IntersectionLane.Localization.EAST) ||
            (vehicle1.getVehicleOrigin() == IntersectionLane.Localization.EAST && vehicle2.getVehicleOrigin() == IntersectionLane.Localization.NORTH));
    }

    public boolean isOtherGoingFromOppositeOrigin(Vehicle vehicle1, Vehicle vehicle2) {  // Sprawdzenie, czy drugi pojazd nie nadjeżdża z przeciwnego origin
        return ((vehicle1.getVehicleOrigin() == IntersectionLane.Localization.NORTH && vehicle2.getVehicleOrigin() == IntersectionLane.Localization.SOUTH) ||
                (vehicle1.getVehicleOrigin() == IntersectionLane.Localization.WEST && vehicle2.getVehicleOrigin() == IntersectionLane.Localization.EAST) ||
                (vehicle1.getVehicleOrigin() == IntersectionLane.Localization.SOUTH && vehicle2.getVehicleOrigin() == IntersectionLane.Localization.NORTH) ||
                (vehicle1.getVehicleOrigin() == IntersectionLane.Localization.EAST && vehicle2.getVehicleOrigin() == IntersectionLane.Localization.WEST));
    }

    public boolean isVehicleTurningLeft(Vehicle vehicle) {  // Sprawdzenie, czy pojazd skręca w lewo
        return ((vehicle.getVehicleOrigin() == IntersectionLane.Localization.NORTH && vehicle.getVehicleDestination() == IntersectionLane.Localization.EAST) ||
                (vehicle.getVehicleOrigin() == IntersectionLane.Localization.WEST && vehicle.getVehicleDestination() == IntersectionLane.Localization.NORTH) ||
                (vehicle.getVehicleOrigin() == IntersectionLane.Localization.SOUTH && vehicle.getVehicleDestination() == IntersectionLane.Localization.WEST) ||
                (vehicle.getVehicleOrigin() == IntersectionLane.Localization.EAST && vehicle.getVehicleDestination() == IntersectionLane.Localization.SOUTH));
    }

    public boolean isVehicleGoingStraight(Vehicle vehicle) {  // Sprawdzenie, czy pojazd jedzie prosto
        return ((vehicle.getVehicleOrigin() == IntersectionLane.Localization.NORTH && vehicle.getVehicleDestination() == IntersectionLane.Localization.SOUTH) ||
                (vehicle.getVehicleOrigin() == IntersectionLane.Localization.WEST && vehicle.getVehicleDestination() == IntersectionLane.Localization.EAST) ||
                (vehicle.getVehicleOrigin() == IntersectionLane.Localization.SOUTH && vehicle.getVehicleDestination() == IntersectionLane.Localization.NORTH) ||
                (vehicle.getVehicleOrigin() == IntersectionLane.Localization.EAST && vehicle.getVehicleDestination() == IntersectionLane.Localization.WEST));
    }

    public boolean isVehicleTurningRight(Vehicle vehicle) {  // Sprawdzenie, czy pojazd skręca w prawo
        return ((vehicle.getVehicleOrigin() == IntersectionLane.Localization.NORTH && vehicle.getVehicleDestination() == IntersectionLane.Localization.WEST) ||
                (vehicle.getVehicleOrigin() == IntersectionLane.Localization.WEST && vehicle.getVehicleDestination() == IntersectionLane.Localization.SOUTH) ||
                (vehicle.getVehicleOrigin() == IntersectionLane.Localization.SOUTH && vehicle.getVehicleDestination() == IntersectionLane.Localization.EAST) ||
                (vehicle.getVehicleOrigin() == IntersectionLane.Localization.EAST && vehicle.getVehicleDestination() == IntersectionLane.Localization.NORTH));
    }

    public void decreaseSpeed(Vehicle vehicle) {
        double tempSpeed = vehicle.getSpeed();
        vehicle.setSpeed(Math.max(tempSpeed - 0.02, 0)); // Minimalna prędkość: 0
    }

    public void increaseSpeed(Vehicle vehicle) {
        double tempSpeed = vehicle.getSpeed();
        vehicle.setSpeed(Math.min(tempSpeed + 0.02, 2.0)); // Maksymalna prędkość: 2.0
    }

}
