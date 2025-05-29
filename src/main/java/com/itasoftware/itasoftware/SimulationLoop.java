package com.itasoftware.itasoftware;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.canvas.Canvas;
import javafx.util.Duration;
import javafx.util.Pair;

import java.util.*;

public class SimulationLoop {

    Timeline timelineSim;
    double runTimerInterval = 10; // Interwał dla timeline w milisekundach (10 ms = 1 cs)
    private final VehicleManager vehicleManager;
    private final CanvasDrawer canvasDrawer;
    private final Canvas simCanvas;
    private double simSpeed;    // Prędkość działania symulacji
    long simTimeLength, elapsedTime = 0;
    private boolean isSimStopped = true;
    private SimulationController simController;
    private final List<VehicleSpawnSchedule> spawnSchedule = new ArrayList<>();     // Harmonogram spawnu pojazdów
    private final Map<Pair<IntersectionLane.Localization, IntersectionLane.Localization>, List<MovementTrajectory>> groupedTrajectories = new HashMap<>();

    public SimulationLoop(Canvas simCanvas, CanvasDrawer canvasDrawer, VehicleManager vehicleManager, double simSpeed, SimulationController simController) {
        if (simCanvas == null) {
            throw new IllegalArgumentException("Canvas nie może być null!");
        }

        this.vehicleManager = vehicleManager;
        this.canvasDrawer = canvasDrawer;
        this.simCanvas = simCanvas;
        this.simSpeed = simSpeed;
        this.simController = simController;

        timelineSim = new Timeline(new KeyFrame(Duration.millis(runTimerInterval), e -> update()));
        timelineSim.setCycleCount(Timeline.INDEFINITE);
    }

    public void run() {
        timelineSim.play();
        isSimStopped = false;
    }

    public void stop() {
        timelineSim.stop();
        isSimStopped = true;
    }

    public void spawn(List<TextFieldVehicleNumber> tfVehNumInputs, Map<MovementRelations, MovementTrajectory> movementMap) {
        spawnSchedule.clear();  // Czyszczenie poprzedniego harmonogramu spawn-ów

        // Grupowanie trajektorii według klucza lokalizacja A - lokalizacja B
        for (MovementRelations mr : MovementRelations.movementRelations) {
            var key = new Pair<>(mr.getObjectA().getLocalization(), mr.getObjectB().getLocalization()); // Utworzenie klucza
            MovementTrajectory traj = movementMap.get(mr);
            if (traj != null) {
                groupedTrajectories.computeIfAbsent(key, k -> new ArrayList<>()).add(traj); // Sprawdzenie istnienia klucza i dodanie trajektorii do listy
            }   // W efekcie, wszystkie trajektorie związane z tą samą parą lokalizacja A - lokalizacja B są przechowywane razem w groupedTrajectories
        }

        for (TextFieldVehicleNumber tfVehNum : tfVehNumInputs) {
            var key = new Pair<>(tfVehNum.getLocalization(), tfVehNum.getDestination());    // Utworzenie klucza lokalizacja-destynacja
            List<MovementTrajectory> trajGroup = groupedTrajectories.get(key);  // Pobranie grupy trajektorii dla danego klucza
            if (trajGroup != null && !trajGroup.isEmpty()) {
                int numVehicles = tfVehNum.getVehiclesNumber().intValue();  // Liczba pojazdów z danego textfield
                spawnSchedule.add(new VehicleSpawnSchedule(trajGroup, numVehicles, simTimeLength));
            }
        }
    }


    public void update() {
        updateTrafficLights();
        vehicleManager.updateVehicles(simSpeed);
        canvasDrawer.drawCanvasWithVehicles(simCanvas, vehicleManager.getVehicles());
        updateTime();

        // Pętla sprawdzająca, czy w danym momencie powinien zostać zespawnowany spojazd
        for (VehicleSpawnSchedule vss : spawnSchedule) {
            if (vss.shouldSpawn(elapsedTime)) {
                vehicleManager.spawnVehicle(vss.getRandomTrajectory());
                vss.markSpawned();
            }
        }
    }

    public void reset() {
        stop();
        elapsedTime = 0;
        vehicleManager.clearVehicles();
        canvasDrawer.drawCanvas(simCanvas);
        resetTrafficLights();
    }


    // Ustawienie prędkości symulacji
    public void setSimSpeed(double simSpeed) {
        this.simSpeed = simSpeed;
        if (!isSimStopped) {
            timelineSim.play();
        }
    }

    // Usatwienie czasu trwania symulacji
    public void setSimTimeLength(long simTimeLength) {
        this.simTimeLength = simTimeLength * 60 * 1000;  // Czas w milisekundach
    }

    // Funkcja aktualizująca czas
    private void updateTime() {
        elapsedTime += (long) (runTimerInterval * simSpeed);
        updateTimer();
        if (elapsedTime >= simTimeLength) {
            stop();
        }
    }

    // Wyświetlanie czasu jako timer
    private void updateTimer() {
        long timeToEnd = (simTimeLength - elapsedTime) / 1000;  // Czas w sekundach

        long hours = timeToEnd / 3600;
        long minutes = (timeToEnd % 3600) / 60;
        long seconds = timeToEnd % 60;

        // Formatowanie czasu jako hh:mm:ss
        String timeString = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        simController.setLabelTime(timeString);
    }

    private void updateTrafficLights() {
        for (TrafficLight tl : TrafficLight.trafficLights) {
            tl.updatePhase(elapsedTime);
        }
    }

    public static void resetTrafficLights() {
        for (TrafficLight tl : TrafficLight.trafficLights) {
            tl.resetPhaseSchedule();
        }
    }

}
