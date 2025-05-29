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
        double distanceToStop = 20, distanceToSlowDown = 120;

        for (Vehicle other : vehiclesList) {
            if (vehicle == other) continue; // Jeśli zadany pojazd jest taki sam, jak wskazany z pętli, pomijamy
            for (StopLine stopLine : GeneratorController.stopLines) {
                // Sprawdź czy StopLine jest z tego samego originu co pojazd
                if (stopLine.getLocalization() != vehicle.getVehicleOrigin()) continue;
                for (TrafficLight trafficLight : TrafficLight.trafficLights) {
                    // Sprawdź czy TL jest powiązany z tą samą lokalizacją (opcjonalne, jeśli TL ma localization)
                    if (trafficLight.getLocalization() != vehicle.getVehicleOrigin()) continue;

                    // Pomocniczne booleany, zwiększające czytelność warunków
                    boolean inBigFOV = vehicle.isPointInFOV(other.getFovX(), other.getFovY(), false);
                    boolean inSmallFOV = vehicle.isPointInFOV(other.getFovX(), other.getFovY(), true);
                    boolean inSquareFOV = vehicle.isPointInSquareFOV(other.getFovX(), other.getFovY(), false);
                    boolean inSmallSquareFOV = vehicle.isPointInSquareFOV(other.getFovX(), other.getFovY(), true);
//                    boolean isStopLineInBigFOV = vehicle.isPointInFOV(stopLine.getPositionCenterX(), stopLine.getPositionCenterY(), false);
//                    boolean isStopLineInSmallFOV = vehicle.isPointInFOV(stopLine.getPositionCenterX(), stopLine.getPositionCenterY(), true);
//                    boolean isStopLineInSquareFOV = vehicle.isPointInSquareFOV(stopLine.getPositionCenterX(), stopLine.getPositionCenterY(), false);
//                    boolean isStopLineInSmallSquareFOV = vehicle.isPointInSquareFOV(stopLine.getPositionCenterX(), stopLine.getPositionCenterY(), true);
//                    boolean isTrafficLightInBigFOV = vehicle.isPointInFOV(trafficLight.getPositionCenterX(), trafficLight.getPositionCenterY(), false);
//                    boolean isTrafficLightInSmallFOV = vehicle.isPointInFOV(trafficLight.getPositionCenterX(), trafficLight.getPositionCenterY(), true);
//                    boolean isTrafficLightInSquareFOV = vehicle.isPointInSquareFOV(trafficLight.getPositionCenterX(), trafficLight.getPositionCenterY(), false);
//                    boolean isTrafficLightInSmallSquareFOV = vehicle.isPointInSquareFOV(trafficLight.getPositionCenterX(), trafficLight.getPositionCenterY(), true);
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
                    // Sprawdzenie, czy TL pokrywa się z SL
                    boolean isTrafficLightAndStopLineStack = TrafficLight.isTrafficLightAndStopLineStack(trafficLight, stopLine);
                    // Sprawdzenie, czy jest zbliża się do sygnalizacji
                    boolean isVehicleApproachingTL = SimulationController.areTrafficLightsActive && isTrafficLightAndStopLineStack &&
                            isVehicleApproachingStopLine(vehicle, stopLine, distanceToSlowDown);
                    boolean isVehicleNearTL = SimulationController.areTrafficLightsActive && isTrafficLightAndStopLineStack &&
                            isVehicleApproachingStopLine(vehicle, stopLine, distanceToStop);
                    // Fazy sygnalizacji
                    boolean redPhase = trafficLight.getCurrentPhase() == TrafficLight.Phase.RED || trafficLight.getCurrentPhase() == TrafficLight.Phase.RED_YELLOW;
                    boolean yellowPhase = trafficLight.getCurrentPhase() == TrafficLight.Phase.YELLOW;
                    boolean greenPhase = trafficLight.getCurrentPhase() == TrafficLight.Phase.GREEN;
                    boolean greenArrowPhase = trafficLight.getCurrentPhase() == TrafficLight.Phase.GREEN_ARROW;

                    // Zasady ruchu drogowego
                    if (inSmallSquareFOV || (isVehicleNearTL && redPhase)) {
                        shouldStop = true;
                        break;
                    } else if (inSquareFOV && areTrajectoriesIntersect && isVehicleGoingLeftAndGivingWay) {
                        shouldStop = true;
                        break;
                    }  else if (inSquareFOV || (isVehicleApproachingTL && redPhase)) {
                        shouldSlowDown = true;
                    } else if (inBigFOV && preventBlockingVehicle) {
                        shouldSlowDown = true;
                        if (isVehicleApproachingStopLine(vehicle, stopLine, distanceToStop)) {
                            shouldStop = true;
                        }
                    } else if (inBigFOV && areTrajectoriesIntersect) {
                        if (isVehicleGivingWayToRight || isVehicleGoingLeftAndGivingWay) {
                            shouldSlowDown = true;
                            if (isVehicleApproachingStopLine(vehicle, stopLine, distanceToStop)) {
                                shouldStop = true;
                            }
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
