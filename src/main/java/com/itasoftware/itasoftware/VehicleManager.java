package com.itasoftware.itasoftware;

import javafx.geometry.Point2D;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class VehicleManager {
    private final List<Vehicle> vehiclesList = new ArrayList<>();

    public void spawnVehicle(MovementTrajectory traj) {
        vehiclesList.add(new Vehicle(traj));
    }

    public void updateVehicles(double simSpeed) {
        for (Vehicle v : vehiclesList) {
            v.setSimSpeed(simSpeed);
            v.updateCachedTrafficLightPhase();
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
        double distanceToStop = 20, distanceToSlowDown = 120, distanceToTL = 30, marginTL = 10;

        findStopLine(vehicle); // Sprawdzenie, czy znaleziono linie stopu w FOV na pasie ruchu pojazdu

        for (Vehicle other : vehiclesList) {
            if (vehicle == other) continue; // Jeśli zadany pojazd jest taki sam, jak wskazany z pętli, pomijamy

            if (!isVehicleWithinDistance(vehicle, other, 150)) continue;    // Jeśli zadany pojazd jest dalej niz 150, pomijamy

            boolean inBigFOV = vehicle.isPointInFOV(other.getFovX(), other.getFovY(), false);   // Jeśli zadany pojazd jest poza FOV, pomijamy
            if (!inBigFOV) continue;

            boolean inSquareFOV = vehicle.isPointInSquareFOV(other.getFovX(), other.getFovY(), false);
            boolean inSmallSquareFOV = vehicle.isPointInSquareFOV(other.getFovX(), other.getFovY(), true);

            // Sprawdzenie, czy trajektorie się przecinają
            boolean areTrajectoriesIntersect = MovementTrajectory.doTrajectoriesIntersect(vehicle.getTrajectory(), other.getTrajectory());
            // Pojazd skręcajacy w lewo udostępnia pierwszeństwa pojazdom jadącym prosto i skręcającym w prawo
            boolean isVehicleGoingLeftAndGivingWay = other.isOnIntersectionSegment() && isVehicleTurningLeft(vehicle) &&
                    isOtherGoingFromOppositeOrigin(vehicle, other) && (isVehicleGoingStraight(other) || isVehicleTurningRight(other));
            // Przeciwdziałanie zablokowaniu pojazdów w konkretnych sytuacjach
            boolean preventBlockingVehicle = (vehicle.vehicleOrigin == IntersectionLane.Localization.NORTH ||
                    vehicle.vehicleOrigin == IntersectionLane.Localization.EAST) &&
                    isOtherGoingFromOppositeOrigin(vehicle, other) && isVehicleTurningLeft(vehicle) && isVehicleTurningLeft(other);
            // Sprawdzenie, czy pojazd musi ustąpić innemu pojazdowi z prawej
            boolean isVehicleGivingWayToRight = isOtherGoingFromRight(vehicle, other) && !vehicle.isOnIntersectionSegment();

            if (SimulationController.areTrafficLightsActive && vehicle.hasAssignedTrafficLight()) {
                // Zasady ruchu z sygnalizacją
                TrafficLight.Phase phase = vehicle.getCachedPhase();
                boolean redPhase = phase == TrafficLight.Phase.RED || phase == TrafficLight.Phase.RED_YELLOW;
                boolean yellowPhase = phase == TrafficLight.Phase.YELLOW;
                boolean greenPhase = phase == TrafficLight.Phase.GREEN;
                boolean greenArrowPhase = phase == TrafficLight.Phase.GREEN_ARROW;

//                // Pojazd skręcajacy w lewo udostępnia pierwszeństwa pojazdom jadącym prosto i skręcającym w prawo
//                boolean isVehicleGoingLeftAndGivingWayWithTrafficLight = hasGreenLight(other) && isVehicleTurningLeft(vehicle)
//                        && isOtherGoingFromOppositeOrigin(vehicle, other) && (isVehicleGoingStraight(other) || isVehicleTurningRight(other));

                if (isVehicleApproachingStopLine(vehicle, vehicle.getAssignedStopLine(), distanceToSlowDown) && (redPhase || yellowPhase)) {
                    if (!(isVehicleApproachingStopLine(vehicle, vehicle.getAssignedStopLine(), marginTL) && yellowPhase)) {
                        shouldSlowDown = true;
                        if (isVehicleApproachingStopLine(vehicle, vehicle.getAssignedStopLine(), distanceToTL)) {
                            shouldStop = true;
                        }
                    }
                } else if (inSmallSquareFOV) {
                    shouldStop = true;
                    break;
                } else if (inSquareFOV) {
                    shouldSlowDown = true;
                } else if (preventBlockingVehicle) {
                    shouldSlowDown = true;
                    if (isVehicleApproachingStopLine(vehicle, vehicle.getAssignedStopLine(), distanceToStop)) {
                        shouldStop = true;
                        break;
                    }
                } else if (areTrajectoriesIntersect) {
                    if (isVehicleGoingLeftAndGivingWay) {
                        shouldSlowDown = true;
                        if (isVehicleApproachingStopLine(vehicle, vehicle.getAssignedStopLine(), distanceToStop)) {
                            shouldStop = true;
                            break;
                        }
                    }
                }
            } else {
                // Zasady ruchu bez sygnalziacji
                if (inSmallSquareFOV || (inSquareFOV && areTrajectoriesIntersect && isVehicleGoingLeftAndGivingWay)) {
                    shouldStop = true;
                    break;
                } else if (inSquareFOV) {
                    shouldSlowDown = true;
                } else if (preventBlockingVehicle) {
                    shouldSlowDown = true;
                    if (isVehicleApproachingStopLine(vehicle, vehicle.getAssignedStopLine(), distanceToStop)) {
                        shouldStop = true;
                        break;
                    }
                } else if (areTrajectoriesIntersect) {
                    if (isVehicleGivingWayToRight || isVehicleGoingLeftAndGivingWay) {
                        shouldSlowDown = true;
                        if (isVehicleApproachingStopLine(vehicle, vehicle.getAssignedStopLine(), distanceToStop)) {
                            shouldStop = true;
                            break;
                        }
                    }
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

    // Sprawdzenie i przypisanie lini stopu (w tym i sygnalizatora)
    private void findStopLine(Vehicle v) {
        if (!v.hasAssignedStopLine()) {
            for (StopLine sl : GeneratorController.stopLines) {
                if (sl.getLocalization() !=v.getVehicleOrigin()) continue;

                if (v.isPointInSquareFOV(sl.getPositionCenterX(), sl.getPositionCenterY(), false)) {
                    v.assignStopLine(sl);
                    findTrafficLight(v, sl);
                    break;
                }
            }
        }
    }

    private void findTrafficLight(Vehicle v, StopLine sl) {
        for (Map.Entry<TrafficLight, StopLine> entry : TrafficLight.trafficLightStopLineMap.entrySet()) {
            if (entry.getValue().equals(sl)) {
                v.assignTrafficLight(entry.getKey()); // znaleziony TrafficLight
            }
        }
    }

    public boolean isVehicleWithinDistance(Vehicle vehicle, Vehicle other, double maxDistance) {
        double dx = vehicle.getFovX() - other.getFovX();
        double dy = vehicle.getFovY() - other.getFovY();
        double distanceSquared = dx * dx + dy * dy;
        return distanceSquared <= maxDistance * maxDistance;
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

    // Funkcja sprawdzająca, czy dany pojazd jest w odległości mniejszej od zadanej do linii stopu
    public boolean isVehicleApproachingStopLine(Vehicle vehicle, StopLine stopLine, double distance) {
        if (stopLine == null) return false;
        double stopLineDistance = Double.MAX_VALUE;
        if (stopLine.getType() == IntersectionLane.Type.ENTRY) {
            stopLineDistance = vehicle.getTrajectory().getDistanceToApproximatePoint(new Point2D(stopLine.getPositionCenterX(), stopLine.getPositionCenterY()));
        }
        double distanceToStopLine = stopLineDistance - vehicle.getDistanceTraveled();
        return distanceToStopLine > 0 && distanceToStopLine <= distance;
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
