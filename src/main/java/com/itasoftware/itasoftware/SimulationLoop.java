package com.itasoftware.itasoftware;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.util.Duration;

import java.util.List;
import java.util.Map;

public class SimulationLoop {

    Timeline timelineSim;
    double runTimerInterval = 10; // Interwał dla timeline w milisekundach
    private final VehicleManager vehicleManager;
    private final CanvasDrawer canvasDrawer;
    private final Canvas simCanvas;
    private double timeSpeed;
    private boolean isSimStopped = true;

    public SimulationLoop(Canvas simCanvas, CanvasDrawer canvasDrawer, VehicleManager vehicleManager, double timeSpeed) {
        if (simCanvas == null) {
            throw new IllegalArgumentException("Canvas nie może być null!");
        }

        this.vehicleManager = vehicleManager;
        this.canvasDrawer = canvasDrawer;
        this.simCanvas = simCanvas;
        this.timeSpeed = timeSpeed;

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
        vehicleManager.updateVehicles();
        canvasDrawer.drawCanvasWithVehicles(simCanvas, vehicleManager.getVehicles());
    }

    public void reset() {
        stop();
        vehicleManager.clearVehicles();
        canvasDrawer.drawCanvas(simCanvas);
    }

    // Usatwienie prędkości symulacji
    public void setTimeSpeed(double timeSpeed) {
        timelineSim.stop();
        timelineSim.getKeyFrames().clear();
        timelineSim.getKeyFrames().add(new KeyFrame(Duration.millis(runTimerInterval / timeSpeed), e -> update()));
        if (!isSimStopped) {
            timelineSim.play();
        }
    }

}
