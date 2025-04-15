package com.itasoftware.itasoftware;

import javafx.fxml.FXML;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.event.ActionEvent;

import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GeneratorController {

    @FXML private Canvas genCanvas;
    @FXML private StackPane genCanvasContainer;
    private static final List<IntersectionLane> intersectionLanes = new ArrayList<>();

    // Powrót do głównego menu
    @FXML
    public void backToMainMenu(ActionEvent event) {
        try {
            MainApplication mainApp = new MainApplication();
            mainApp.loadMainView(); // Wywołanie metody instancyjnej w MainApplication
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @FXML
    public void initialize() {
        // Reakcja na zmianę rozmiaru kontenera
        genCanvasContainer.layoutBoundsProperty().addListener((obs, oldVal, newVal) -> {
            scaleCanvas();
        });

        if (intersectionLanes.isEmpty()) {
            intersectionLanes.add(new IntersectionLane(IntersectionLane.Localization.NORTH, IntersectionLane.Type.ENTRY, 0));
            intersectionLanes.add(new IntersectionLane(IntersectionLane.Localization.NORTH, IntersectionLane.Type.EXIT, 0));
            intersectionLanes.add(new IntersectionLane(IntersectionLane.Localization.SOUTH, IntersectionLane.Type.ENTRY, 0));
            intersectionLanes.add(new IntersectionLane(IntersectionLane.Localization.SOUTH, IntersectionLane.Type.EXIT, 0));
            intersectionLanes.add(new IntersectionLane(IntersectionLane.Localization.EAST, IntersectionLane.Type.ENTRY, 0));
            intersectionLanes.add(new IntersectionLane(IntersectionLane.Localization.EAST, IntersectionLane.Type.EXIT, 0));
            intersectionLanes.add(new IntersectionLane(IntersectionLane.Localization.WEST, IntersectionLane.Type.ENTRY, 0));
            intersectionLanes.add(new IntersectionLane(IntersectionLane.Localization.WEST, IntersectionLane.Type.EXIT, 0));

            intersectionLanes.add(new IntersectionLane(IntersectionLane.Localization.NORTH, IntersectionLane.Type.ENTRY, 1));
            intersectionLanes.add(new IntersectionLane(IntersectionLane.Localization.NORTH, IntersectionLane.Type.EXIT, 1));
            intersectionLanes.add(new IntersectionLane(IntersectionLane.Localization.SOUTH, IntersectionLane.Type.ENTRY, 1));
            intersectionLanes.add(new IntersectionLane(IntersectionLane.Localization.SOUTH, IntersectionLane.Type.EXIT, 1));
            intersectionLanes.add(new IntersectionLane(IntersectionLane.Localization.EAST, IntersectionLane.Type.ENTRY, 1));
            intersectionLanes.add(new IntersectionLane(IntersectionLane.Localization.EAST, IntersectionLane.Type.EXIT, 1));
            intersectionLanes.add(new IntersectionLane(IntersectionLane.Localization.WEST, IntersectionLane.Type.ENTRY, 1));
            intersectionLanes.add(new IntersectionLane(IntersectionLane.Localization.WEST, IntersectionLane.Type.EXIT, 1));

//            intersectionLanes.add(new IntersectionLane(IntersectionLane.Localization.WEST, IntersectionLane.Type.ENTRY, 2));
//            intersectionLanes.add(new IntersectionLane(IntersectionLane.Localization.WEST, IntersectionLane.Type.ENTRY, 3));
//            intersectionLanes.add(new IntersectionLane(IntersectionLane.Localization.EAST, IntersectionLane.Type.ENTRY, 2));
//            intersectionLanes.add(new IntersectionLane(IntersectionLane.Localization.EAST, IntersectionLane.Type.ENTRY, 3));

        }

        drawCanvas(); // pierwsze rysowanie
    }

    // Skalowanie genCanvas
    private void scaleCanvas() {
        double baseWidth = 800;
        double baseHeight = 800;

        double availableWidth = genCanvasContainer.getWidth();
        double availableHeight = genCanvasContainer.getHeight();

        if (availableWidth == 0 || availableHeight == 0) return;

        double scaleX = availableWidth / baseWidth;
        double scaleY = availableHeight / baseHeight;

        double scale = Math.min(scaleX, scaleY); // zachowujemy proporcje

        genCanvas.setScaleX(scale);
        genCanvas.setScaleY(scale);
    }

    // Rysowanie genCanvas
    private void drawCanvas() {
        GraphicsContext gc = genCanvas.getGraphicsContext2D();

        // Tło
        gc.setFill(Color.GRAY);
        gc.fillRect(0, 0, genCanvas.getWidth(), genCanvas.getHeight());

        // Pasy ruchu
        for (IntersectionLane lane : intersectionLanes) {
            drawLanes(gc, lane);
        }

        // Linie środkowa skrzyżowania
        //gc.setStroke(Color.RED);
        //gc.setLineWidth(2);
        //gc.strokeLine(genCanvas.getWidth()/2, 0, genCanvas.getWidth()/2, genCanvas.getHeight());
        //gc.strokeLine(0, genCanvas.getHeight()/2, genCanvas.getWidth(), genCanvas.getHeight()/2);
    }

    // Rysowanie pasów ruchu skrzyżowania
    private void drawLanes(GraphicsContext gc, IntersectionLane lane) {
        double centerX = genCanvas.getWidth() / 2;
        double centerY = genCanvas.getHeight() / 2;
        double laneWidth = 20;
        double laneHeight = 400;

        double x = 0, y = 0, w = 0, h = 0;
        int offset = lane.getIndex();

        switch (lane.getLocalization()) {
            case NORTH -> {
                y = centerY - laneHeight;
                if (lane.getType() == IntersectionLane.Type.ENTRY)
                    x = centerX - (laneWidth * (offset+1));
                else
                    x = centerX + (laneWidth * offset);
                w = laneWidth;
                h = laneHeight;
            }
            case SOUTH -> {
                y = centerY;
                if (lane.getType() == IntersectionLane.Type.ENTRY)
                    x = centerX + (laneWidth * offset);
                else
                    x = centerX - (laneWidth * (offset+1));
                w = laneWidth;
                h = laneHeight;
            }
            case EAST -> {
                x = centerX;
                if (lane.getType() == IntersectionLane.Type.ENTRY)
                    y = centerY - (laneWidth * (offset+1));
                else
                    y = centerY + (laneWidth * offset);
                w = laneHeight;
                h = laneWidth;
            }
            case WEST -> {
                x = centerX - laneHeight;
                if (lane.getType() == IntersectionLane.Type.ENTRY)
                    y = centerY + (laneWidth * offset);
                else
                    y = centerY - (laneWidth * (offset+1));
                w = laneHeight;
                h = laneWidth;
            }
        }

        gc.setFill(Color.BLACK);
        gc.fillRect(x, y, w, h);

        // Obliczenie liczby przecięć dla skracania linii
        int intersectionCount = 0;
        int intersectionCount_A;
        int intersectionCount_B;
        switch (lane.getLocalization()) {
            case NORTH -> {
                intersectionCount_A = (int) intersectionLanes.stream()
                        .filter(l -> (l.getLocalization() == IntersectionLane.Localization.EAST && l.getType() == IntersectionLane.Type.ENTRY)).count();
                intersectionCount_B = (int) intersectionLanes.stream()
                        .filter(l -> (l.getLocalization() == IntersectionLane.Localization.WEST && l.getType() == IntersectionLane.Type.EXIT)).count();
                intersectionCount = Math.max(intersectionCount_A, intersectionCount_B);
            }
            case SOUTH -> {
                intersectionCount_A = (int) intersectionLanes.stream()
                        .filter(l -> (l.getLocalization() == IntersectionLane.Localization.EAST && l.getType() == IntersectionLane.Type.EXIT)).count();
                intersectionCount_B = (int) intersectionLanes.stream()
                        .filter(l -> (l.getLocalization() == IntersectionLane.Localization.WEST && l.getType() == IntersectionLane.Type.ENTRY)).count();
                intersectionCount = Math.max(intersectionCount_A, intersectionCount_B);
            }
            case EAST-> {
                intersectionCount_A = (int) intersectionLanes.stream()
                        .filter(l -> (l.getLocalization() == IntersectionLane.Localization.NORTH && l.getType() == IntersectionLane.Type.EXIT)).count();
                intersectionCount_B = (int) intersectionLanes.stream()
                        .filter(l -> (l.getLocalization() == IntersectionLane.Localization.SOUTH && l.getType() == IntersectionLane.Type.ENTRY)).count();
                intersectionCount = Math.max(intersectionCount_A, intersectionCount_B);
            }
            case WEST -> {
                intersectionCount_A = (int) intersectionLanes.stream()
                        .filter(l -> (l.getLocalization() == IntersectionLane.Localization.NORTH && l.getType() == IntersectionLane.Type.ENTRY)).count();
                intersectionCount_B = (int) intersectionLanes.stream()
                        .filter(l -> (l.getLocalization() == IntersectionLane.Localization.SOUTH && l.getType() == IntersectionLane.Type.EXIT)).count();
                intersectionCount = Math.max(intersectionCount_A, intersectionCount_B);
            }
        }

        double cutoff_size = 20;
        double cutoff = intersectionCount * cutoff_size;

        // Rysowanie białych linii (z uwzględnieniem cutoff)
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(1);

        switch (lane.getLocalization()) {
            case NORTH -> {
                if (lane.getType() == IntersectionLane.Type.ENTRY) {
                    gc.strokeLine(x + w, y, x + w, centerY - cutoff);
                } else {
                    gc.strokeLine(x, y, x, centerY - cutoff);
                }
            }
            case SOUTH -> {
                if (lane.getType() == IntersectionLane.Type.ENTRY) {
                    gc.strokeLine(x, centerY + cutoff, x, y + h);
                } else {
                    gc.strokeLine(x + w, centerY + cutoff, x + w, y + h);
                }
            }
            case EAST -> {
                if (lane.getType() == IntersectionLane.Type.ENTRY) {
                    gc.strokeLine(centerX + cutoff, y + h, x + w, y + h);
                } else {
                    gc.strokeLine(centerX + cutoff, y, x + w, y);
                }
            }
            case WEST -> {
                if (lane.getType() == IntersectionLane.Type.ENTRY) {
                    gc.strokeLine(centerX - cutoff, y, x - w, y);
                } else {
                    gc.strokeLine(centerX - cutoff, y + h, x - w, y + h);
                }
            }
        }

//        // Linie Stop
//        double stopLineY = centerY - intersectionHeight / 2;
//        double stopLineX = x; // x z drawLane
//
//        gc.setStroke(Color.WHITE);
//        gc.setLineWidth(2);
//        gc.strokeLine(stopLineX, stopLineY, stopLineX + laneWidth, stopLineY);

    }
}
