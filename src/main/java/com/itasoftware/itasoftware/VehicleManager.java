package com.itasoftware.itasoftware;

import javafx.geometry.Point2D;
import javafx.scene.control.TextField;

import java.util.*;

public class VehicleManager {
    private final List<Vehicle> vehiclesList = new ArrayList<>();
    private static final List<Vehicle> vehiclesToSpawnList = new ArrayList<>();
    private static Map<Vehicle, IntersectionLane.Localization> vehiclesOriginMap = new HashMap<>();
    private IntersectionLane.Localization selectedLocalization, lastSpawnedLocalization;
    int sameLocationCounter = 0, iterations = 0;

    double distanceToStop = 20, distanceToSlowDown = 120, distanceToTL = 30, marginTL = 10;

    public void spawnVehicle() {
        if (!vehiclesToSpawnList.isEmpty()) {
            Collections.shuffle(vehiclesToSpawnList);
            int randomIndex = new Random().nextInt(vehiclesToSpawnList.size());
            Vehicle selectedVehicle = vehiclesToSpawnList.get(randomIndex);
            selectedLocalization = vehiclesOriginMap.get(selectedVehicle);

            if (!Objects.equals(selectedLocalization, lastSpawnedLocalization)) {
                sameLocationCounter = 0;    // Inna lokalizacja, reset licznika
            } else {
                sameLocationCounter++;      // Ta sama lokalizacja, zwiększenie licznika
            }

            if (sameLocationCounter < 5) {  // Jeśli wybrana lokalziacja nie powtórzyła się 5 razy, można spawnować
                lastSpawnedLocalization = selectedLocalization;
                vehiclesList.add(selectedVehicle);
                vehiclesToSpawnList.remove(selectedVehicle);
            } else {    // Po 5 powtózeniu, powone wywołanie funkcji
                if (iterations > 4) {   // Jeśli 5-krotne potwórzenie funkcji nie zmieniło lokalziacji, pomijamy warunek
                    lastSpawnedLocalization = selectedLocalization;
                    vehiclesList.add(selectedVehicle);
                    vehiclesToSpawnList.remove(selectedVehicle);
                } else {
                    iterations++;
                    spawnVehicle();
                }
            }

        }
    }

    public void resetCountInts() {
        sameLocationCounter = 0;
        iterations = 0;
    }

    // Dodanie pojazdów do listy oczekujących na spawn, na bazie dopasowań wpisów w Textfiel-dach a trajektoriach
    public static void addVehiclesToSpawn() {
        vehiclesToSpawnList.clear();
        vehiclesOriginMap.clear();

        for (Map.Entry<TextField, TextFieldVehicleNumber> tfm : SimulationController.textfieldMap.entrySet()) {
            TextField tf = tfm.getKey();
            TextFieldVehicleNumber tfVehNum = tfm.getValue();

            for (MovementRelations mr : MovementRelations.movementRelations) {
                IntersectionLaneButton objA = mr.getObjectA();
                IntersectionLaneButton objB = mr.getObjectB();

                if (tfVehNum.getLocalization() == objA.getLocalization() && tfVehNum.getDestination() == objB.getLocalization()) {

                    MovementTrajectory traj = MovementTrajectory.movementMap.get(mr);
                    if (traj != null) {
                        for (int i = 0; i < tfVehNum.getVehiclesNumber().intValue(); i++) {
                            Vehicle vehicle = new Vehicle(traj);
                            vehiclesToSpawnList.add(vehicle);
                            vehiclesOriginMap.put(vehicle, tfVehNum.getLocalization());
                        }
                    }

                }
            }
        }
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
        vehicle.shouldStop = false;
        vehicle.shouldSlowDown = false;

        if (vehicle.getDistanceTraveled() < 30) return; // Jeśli koniec jest bliżej niż 10px, nie analizujemy reszty warunków

        findStopLine(vehicle); // Sprawdzenie, czy znaleziono linie stopu w FOV na pasie ruchu pojazdu
        chechkTrafficLightState(vehicle);   // Sprawdzenie aktualnej fazy sygnalizacji

        for (Vehicle other : vehiclesList) {
            if (vehicle == other) continue; // Jeśli zadany pojazd jest taki sam, jak wskazany z pętli, pomijamy

            if (!isVehicleWithinDistance(vehicle, other, 170)) continue;    // Jeśli zadany pojazd jest dalej niz 150, pomijamy

            boolean inBigFOV = vehicle.isPointInFOV(other.getFovX(), other.getFovY(), false);   // Jeśli zadany pojazd jest poza FOV, pomijamy
            if (!inBigFOV) continue;

            boolean inSquareFOV = vehicle.isPointInSquareFOV(other.getFovX(), other.getFovY(), false);
            boolean inSmallSquareFOV = vehicle.isPointInSquareFOV(other.getFovX(), other.getFovY(), true);
            boolean inLeftFOV = vehicle.isPointInFOV(other.getFovX(), other.getFovY(), true);

            // Sprawdzenie, czy trajektorie się przecinają
            boolean areTrajectoriesIntersect = MovementTrajectory.doTrajectoriesIntersect(vehicle.getTrajectory(), other.getTrajectory());
            // Pojazd skręcajacy w lewo udostępnia pierwszeństwa pojazdom jadącym prosto i skręcającym w prawo
            boolean isVehicleGoingLeftAndGivingWay = isVehicleTurningLeft(vehicle) &&
                    isOtherGoingFromOppositeOrigin(vehicle, other);
            // Przeciwdziałanie zablokowaniu pojazdów w konkretnych sytuacjach
            boolean preventBlockingVehicle = (vehicle.vehicleOrigin == IntersectionLane.Localization.NORTH ||
                    vehicle.vehicleOrigin == IntersectionLane.Localization.EAST) &&
                    isOtherGoingFromOppositeOrigin(vehicle, other) && isVehicleTurningLeft(vehicle) && isVehicleTurningLeft(other);
            // Sprawdzenie, czy pojazd musi ustąpić innemu pojazdowi z prawej
            boolean isVehicleGivingWayToRight = isOtherGoingFromRight(vehicle, other) && !vehicle.isOnIntersectionSegment();

            if (SimulationController.areTrafficLightsActive && vehicle.hasAssignedTrafficLight()) {
                // Zasady ruchu z sygnalizacją
                TrafficLight.Phase phase = vehicle.getCachedPhase();
                TrafficLight.Phase phaseOther = other.getCachedPhase();
                boolean redPhase = phase == TrafficLight.Phase.RED || phase == TrafficLight.Phase.RED_YELLOW;
                boolean yellowPhase = phase == TrafficLight.Phase.YELLOW;
                boolean greenPhase = phase == TrafficLight.Phase.GREEN;
                boolean greenArrowPhase = phase == TrafficLight.Phase.GREEN_ARROW;
                boolean redPhaseOther = phaseOther == TrafficLight.Phase.RED || phaseOther == TrafficLight.Phase.RED_YELLOW;
                boolean yellowPhaseOther = phaseOther == TrafficLight.Phase.YELLOW;
                boolean greenPhaseOther = phaseOther == TrafficLight.Phase.GREEN;
                boolean greenArrowPhaseOther = phaseOther == TrafficLight.Phase.GREEN_ARROW;

                if (inSmallSquareFOV) {
                    vehicle.shouldStop = true;
                    break;
                } else if (isVehicleApproachingStopLine(vehicle, vehicle.getAssignedStopLine(), distanceToSlowDown) && (redPhase || yellowPhase)) {
                    if (!(isVehicleApproachingStopLine(vehicle, vehicle.getAssignedStopLine(), marginTL) && yellowPhase)) {
                        vehicle.shouldSlowDown = true;
                        if (isVehicleApproachingStopLine(vehicle, vehicle.getAssignedStopLine(), distanceToTL)) {
                            vehicle.shouldStop = true;
                        }
                    }
                } else if (inSquareFOV && !other.isAccelerating()) {
                    vehicle.shouldSlowDown = true;
                } else if (inLeftFOV && preventBlockingVehicle && (greenPhaseOther || greenArrowPhaseOther)) {
//                    //vehicle.shouldSlowDown = true;
                    vehicle.shouldStop = true;
//                    if (isVehicleApproachingStopLine(vehicle, vehicle.getAssignedStopLine(), distanceToStop) || vehicle.isOnIntersectionSegment()) {
//                        vehicle.shouldStop = true;
//                        break;
//                    }
                } else if (areTrajectoriesIntersect) {
                    if (isVehicleGivingWayToRight && greenArrowPhase) {
                        vehicle.shouldSlowDown = true;
                        if (isVehicleApproachingStopLine(vehicle, vehicle.getAssignedStopLine(), distanceToStop)) {
                            vehicle.shouldStop = true;
                            break;
                        }
                    }
                    if (inLeftFOV && isVehicleGoingLeftAndGivingWay && (greenPhaseOther || greenArrowPhaseOther) && vehicle.isOnIntersectionSegment()) {
                        vehicle.shouldSlowDown = true;
                        if (other.isAccelerating() || (isVehicleTurningLeft(other) && other.isOnIntersectionSegment())) {
                            vehicle.shouldStop = true;
                            break;
                        }
                    }
                }
            } else {
                // Zasady ruchu bez sygnalziacji
                if (inSmallSquareFOV || (inSquareFOV && areTrajectoriesIntersect && isVehicleGoingLeftAndGivingWay)) {
                    vehicle.shouldStop = true;
                    break;
                } else if (inSquareFOV && !other.isAccelerating()) {
                    vehicle.shouldSlowDown = true;
                } else if (preventBlockingVehicle) {
                    vehicle.shouldSlowDown = true;
                    if (isVehicleApproachingStopLine(vehicle, vehicle.getAssignedStopLine(), distanceToStop) || vehicle.isOnIntersectionSegment()) {
                        vehicle.shouldStop = true;
                        break;
                    }
                } else if (areTrajectoriesIntersect) {
                    if (isVehicleGivingWayToRight || isVehicleGoingLeftAndGivingWay) {
                        vehicle.shouldSlowDown = true;
                        if (isVehicleApproachingStopLine(vehicle, vehicle.getAssignedStopLine(), distanceToStop)) {
                            vehicle.shouldStop = true;
                            break;
                        }
                    }
                }

            }

        }

        // Modyfikacja prędkości pojazdu
        changeVehicleSpeed(vehicle);
    }

    private void chechkTrafficLightState(Vehicle vehicle) {
        if (SimulationController.areTrafficLightsActive && vehicle.hasAssignedTrafficLight()) {
            // Zasady ruchu z sygnalizacją
            TrafficLight.Phase phase = vehicle.getCachedPhase();
            boolean redPhase = phase == TrafficLight.Phase.RED || phase == TrafficLight.Phase.RED_YELLOW;
            boolean yellowPhase = phase == TrafficLight.Phase.YELLOW;
            boolean greenPhase = phase == TrafficLight.Phase.GREEN;
            boolean greenArrowPhase = phase == TrafficLight.Phase.GREEN_ARROW;
            if (isVehicleApproachingStopLine(vehicle, vehicle.getAssignedStopLine(), distanceToSlowDown) && (redPhase || yellowPhase)) {
                if (!(isVehicleApproachingStopLine(vehicle, vehicle.getAssignedStopLine(), marginTL) && yellowPhase)) {
                    vehicle.shouldSlowDown = true;
                    if (isVehicleApproachingStopLine(vehicle, vehicle.getAssignedStopLine(), distanceToTL)) {
                        vehicle.shouldStop = true;
                    }
                }
            }
            // Modyfikacja prędkości pojazdu
            changeVehicleSpeed(vehicle);
        }
    }

    // Modyfikacja prędkości pojazdu
    private void changeVehicleSpeed(Vehicle vehicle) {
        if (vehicle.getShouldStop()) {
            vehicle.setSpeed(0);
            vehicle.isAccelerating = false;
        } else if (vehicle.getShouldSlowDown()) {
            decreaseSpeed(vehicle);
            vehicle.isAccelerating = false;
        } else {
            increaseSpeed(vehicle);
            vehicle.isAccelerating = true;
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
        vehicle.setSpeed(Math.max(tempSpeed - (0.02 * SimulationController.simSpeed), 0.4)); // Minimalna prędkość: 0.2
    }

    public void increaseSpeed(Vehicle vehicle) {
        double tempSpeed = vehicle.getSpeed();
        vehicle.setSpeed(Math.min(tempSpeed + (0.02 * SimulationController.simSpeed), 2.0)); // Maksymalna prędkość: 2.0
    }

}