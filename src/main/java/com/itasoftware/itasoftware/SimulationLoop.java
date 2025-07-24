package com.itasoftware.itasoftware;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.util.Duration;
import javafx.util.Pair;

import java.util.*;

public class SimulationLoop {

    private Thread simulationThread;
    private volatile boolean running = false;

    double runTimerInterval = 10; // Interwał dla timeline w milisekundach (10 ms = 1 cs)
    private final VehicleManager vehicleManager;
    private final CanvasDrawer canvasDrawer;
    private final Canvas simCanvas;
    private double simSpeed;    // Prędkość działania symulacji
    long simTimeLength, elapsedTime = 0;
    private boolean isSimStopped = true;
    public static boolean isSimFinished = false;
    private SimulationController simController;
    private final List<VehicleSpawnSchedule> spawnSchedule = new ArrayList<>();     // Harmonogram spawnu pojazdów
    private final Map<Pair<IntersectionLane.Localization, IntersectionLane.Localization>, List<MovementTrajectory>> groupedTrajectories = new HashMap<>();
    private final Map<Pair<IntersectionLane.Localization, IntersectionLane.Localization>, Queue<MovementTrajectory>> spawnQueues = new HashMap<>();

    public SimulationLoop(Canvas simCanvas, CanvasDrawer canvasDrawer, VehicleManager vehicleManager, double simSpeed, SimulationController simController) {
        if (simCanvas == null) {
            throw new IllegalArgumentException("Canvas nie może być null!");
        }

        this.vehicleManager = vehicleManager;
        this.canvasDrawer = canvasDrawer;
        this.simCanvas = simCanvas;
        this.simSpeed = simSpeed;
        this.simController = simController;
    }

    public void run() {
        if (running) return;
        running = true;
        isSimStopped = false;

        simulationThread = new Thread(() -> {
            long lastUpdate = System.nanoTime();

            while (running) {
                long now = System.nanoTime();
                double deltaMillis = (now - lastUpdate) / 1_000_000.0;
                lastUpdate = now;

                update();

                try {
                    Thread.sleep((long) runTimerInterval);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });

        simulationThread.setDaemon(true);
        simulationThread.start();
    }

    public void stop() {
        running = false;
        isSimStopped = true;
    }

    public void createSpawnSchedule() {
        spawnSchedule.clear();  // Czyszczenie poprzedniego harmonogramu spawn-ów
        spawnSchedule.add(new VehicleSpawnSchedule(simTimeLength));
    }

    public void update() {
        updateTrafficLights();
        vehicleManager.updateVehicles(simSpeed);
        vehicleManager.setCurrentTime(elapsedTime);

        Platform.runLater(() -> {
            canvasDrawer.drawCanvasWithVehicles(simCanvas, vehicleManager.getVehicles());
            updateTimer();
        });

        updateTime();

        // Pętla sprawdzająca, czy w danym momencie powinien zostać zespawnowany spojazd
        for (VehicleSpawnSchedule vss : spawnSchedule) {
            if (vss.shouldSpawn(elapsedTime)) {
                vehicleManager.spawnVehicle();
                vss.markSpawned();
            }
        }
    }

    public void reset() {
        stop();
        elapsedTime = 0;
        vehicleManager.clearVehicles();
        vehicleManager.resetCountInts();
        canvasDrawer.drawCanvas(simCanvas);
        resetTrafficLights();
        DataCollector.clearReportContent();
        isSimFinished = false;
    }


    // Ustawienie prędkości symulacji
    public void setSimSpeed(double simSpeed) {
        this.simSpeed = simSpeed;
//        if (!isSimStopped) {
//            timelineSim.play();
//        }
    }

    // Usatwienie czasu trwania symulacji
    public void setSimTimeLength(long simTimeLength) {
        this.simTimeLength = simTimeLength * 60 * 1000;  // Czas w milisekundach
    }

    // Funkcja aktualizująca czas
    private void updateTime() {
        elapsedTime += (long) (runTimerInterval * simSpeed);
        //updateTimer();
        if (elapsedTime >= simTimeLength) {
            stop();
            isSimFinished = true;
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
