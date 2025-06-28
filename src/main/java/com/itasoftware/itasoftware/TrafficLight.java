package com.itasoftware.itasoftware;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TrafficLight extends StopLine {

    public enum Phase {RED, YELLOW, RED_YELLOW, GREEN, GREEN_ARROW}
    public enum LaneType {MAIN, LEFT, RIGHT}    // Typ pasa np. lewoskręt
    public Phase phase;
    public LaneType laneType;
    public static final List<TrafficLight> trafficLights = new ArrayList<>();   // Lista sygnalizatorów świetlnych
    public List<Phase> phaseSequence = new ArrayList<>();  // Lista programów faz
    public static Map<TrafficLight, StopLine> trafficLightStopLineMap = new HashMap<>();

    public int currentPhaseIndex = 0;
    public long lastPhaseChangeTime = 0; // W milisekundach
    public static final long phaseDuration = 1000; // Długość jednej fazy (1000 ms)

    public TrafficLight(Localization localization, Type type, int index, double positionCenterX, double positionCenterY, LaneType laneType) {
        super(localization, type, index, positionCenterX, positionCenterY);
        this.laneType = laneType;
    }

    public static void addTrafficLight(IntersectionLane il, LaneType laneType) {
        for (StopLine sl : GeneratorController.stopLines) {
            if (sl.getLocalization() == il.getLocalization() && sl.getType() == Type.ENTRY && sl.getIndex() == il.getIndex()) {
                TrafficLight tl = new TrafficLight(sl.getLocalization(), sl.getType(), sl.getIndex(), sl.getPositionCenterX(), sl.getPositionCenterY(), laneType);

                // Ustawienie domyślnej sekwencji faz
                List<Phase> defaultSequence = List.of(Phase.RED);
                tl.setPhaseSequence(defaultSequence);

                // Ustawienie początkowej fazy i czasu
                tl.setCurrentPhase(defaultSequence.getFirst());
                //tl.lastPhaseChangeTime = System.currentTimeMillis();

                if (!trafficLights.contains(tl)) {
                    trafficLights.add(tl);
                }

                trafficLightStopLineMap.put(tl, sl);
            }
        }
    }

    public void updatePhase(long currentTime) {
        if (currentTime == 0) {
            setCurrentPhase(phaseSequence.getFirst());
            return;
        }
        if (currentTime - lastPhaseChangeTime >= phaseDuration) {
            currentPhaseIndex = (currentPhaseIndex + 1) % phaseSequence.size(); // Po osiągnięciu ostatniego elementu z listy, powrót na jej początek
            setCurrentPhase(phaseSequence.get(currentPhaseIndex));
            lastPhaseChangeTime = currentTime;
        }
    }

    // Sprawdzenie, czy współrzędne TL pokrywają się z SL
    public static boolean isTrafficLightAndStopLineStack(TrafficLight tl, StopLine sl) {
        return (tl.getPositionCenterX() == sl.getPositionCenterX() && tl.getPositionCenterY() == sl.getPositionCenterY());
    }

    public void resetPhaseSchedule() {
        currentPhaseIndex = 0;
        lastPhaseChangeTime = 0;
    }

    public void clearPhaseSchedule() {
        phaseSequence.clear();
    }

    public static void clear() {
        trafficLights.clear();
    }

    public void setCurrentPhase(Phase phase) {
        this.phase = phase;
    }

    public Phase getCurrentPhase() {
        return phase;
    }

    public void setLaneType(LaneType laneType) {
        this.laneType = laneType;
    }

    public LaneType getLaneType() {
        return laneType;
    }

    public void setPhaseSequence(List<Phase> sequence) {
        this.phaseSequence = sequence;
    }

    public List<Phase> getPhaseSequence() {
        return phaseSequence;
    }

}
