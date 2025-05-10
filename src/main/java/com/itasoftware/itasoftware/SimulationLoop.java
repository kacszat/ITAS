package com.itasoftware.itasoftware;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.canvas.Canvas;
import javafx.util.Duration;

import java.util.List;
import java.util.Map;

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

    public void spawn(List<TextFieldVehicleNumber> tfVNInput, Map<MovementRelations, MovementTrajectory> movementMap) {
        vehicleManager.spawnVehicle(tfVNInput, movementMap);
    }

    public void update() {
        vehicleManager.updateVehicles(simSpeed);
        canvasDrawer.drawCanvasWithVehicles(simCanvas, vehicleManager.getVehicles());
        updateTime();
    }

    public void reset() {
        stop();
        elapsedTime = 0;
        vehicleManager.clearVehicles();
        canvasDrawer.drawCanvas(simCanvas);
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
        System.out.println("et: " + elapsedTime);
        System.out.println("simtime: " + simTimeLength);
        updateTimer();
        if (elapsedTime >= simTimeLength) {
            stop();
        }
    }

    // Wyświetlanie czasu jako timer
    private void updateTimer() {
        long timeToEnd = (simTimeLength - elapsedTime) / 1000;  // Czas w sekundach
        System.out.println("time to end: " + timeToEnd);

        long hours = timeToEnd / 3600;
        long minutes = (timeToEnd % 3600) / 60;
        long seconds = timeToEnd % 60;

        // Formatowanie czasu jako hh:mm:ss
        String timeString = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        simController.setLabelTime(timeString);
    }
}
