package com.itasoftware.itasoftware;

import javafx.fxml.FXML;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Slider;
import javafx.scene.layout.StackPane;
import javafx.event.ActionEvent;

import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GeneratorController {

    @FXML private Canvas genCanvas;
    @FXML private StackPane genCanvasContainer;
    private static final List<IntersectionLane> intersectionLanes = new ArrayList<>();
    private static final List<StopLine> stopLines = new ArrayList<>();
    private final Map<Slider, SliderLane> sliderMap = new HashMap<>();
    @FXML private Slider sliderNorthEntry, sliderNorthExit, sliderSouthEntry, sliderSouthExit, sliderEastEntry, sliderEastExit, sliderWestEntry, sliderWestExit;


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
        }

        sliderMap.put(sliderNorthEntry, new SliderLane(SliderLane.Localization.NORTH, SliderLane.Type.ENTRY));
        sliderMap.put(sliderNorthExit, new SliderLane(SliderLane.Localization.NORTH, SliderLane.Type.EXIT));
        sliderMap.put(sliderSouthEntry, new SliderLane(SliderLane.Localization.SOUTH, SliderLane.Type.ENTRY));
        sliderMap.put(sliderSouthExit, new SliderLane(SliderLane.Localization.SOUTH, SliderLane.Type.EXIT));
        sliderMap.put(sliderEastEntry, new SliderLane(SliderLane.Localization.EAST, SliderLane.Type.ENTRY));
        sliderMap.put(sliderEastExit, new SliderLane(SliderLane.Localization.EAST, SliderLane.Type.EXIT));
        sliderMap.put(sliderWestEntry, new SliderLane(SliderLane.Localization.WEST, SliderLane.Type.ENTRY));
        sliderMap.put(sliderWestExit, new SliderLane(SliderLane.Localization.WEST, SliderLane.Type.EXIT));

        for (Slider slider : sliderMap.keySet()) {
            configureSlider(slider);
            slider.valueProperty().addListener((obs, oldVal, newVal) -> {
                SliderLane info = sliderMap.get(slider);
                System.out.println("Zmiana suwaka: " + info.getLocalization() + " " + info.getType() + " -> " + newVal.intValue());
                updateIntersectionLanes(info.getLocalization(), info.getType(), newVal.intValue());
            });
        }

        drawCanvas(); // pierwsze rysowanie
    }

    // Ustawienia sliderów
    private void configureSlider(Slider slider) {
        slider.setMin(0);
        slider.setMax(8);
        slider.setValue(1);
        slider.setBlockIncrement(1);
        slider.setMajorTickUnit(1);
        slider.setMinorTickCount(0);
        slider.setSnapToTicks(true);
        slider.setShowTickMarks(true);
        slider.setShowTickLabels(true);
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
        drawBackground(gc);

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

    private void drawBackground(GraphicsContext gc) {
        gc.setFill(Color.rgb(71,71,71)); // Ten sam co #474747
        gc.fillRect(0, 0, genCanvas.getWidth(), genCanvas.getHeight());
    }

    private void updateIntersectionLanes(SliderLane.Localization slider_localization, SliderLane.Type slider_type, int count) {
        // Usunięcie starych pasów
        intersectionLanes.removeIf(lane -> lane.getLocalization().name().equals(slider_localization.name()) && lane.getType().name().equals(slider_type.name()));

        // Dodawanie nowych pasów
        for (int i = 0; i < count; i++) {
            intersectionLanes.add(new IntersectionLane(IntersectionLane.Localization.valueOf(slider_localization.name()),
                    IntersectionLane.Type.valueOf(slider_type.name()), i));
        }

        // Ponowne wygenerowanie grafiki
        drawCanvas();
    }

    private void clearLists() {
        intersectionLanes.clear();
        stopLines.clear();
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

        double cutoff = intersectionCount * laneWidth;
        int stopLaneHeight = 2;

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
                drawStopLine(gc, x, (centerY - cutoff - stopLaneHeight), laneWidth, stopLaneHeight, lane);
            }
            case SOUTH -> {
                if (lane.getType() == IntersectionLane.Type.ENTRY) {
                    gc.strokeLine(x, centerY + cutoff, x, y + h);
                } else {
                    gc.strokeLine(x + w, centerY + cutoff, x + w, y + h);
                }
                drawStopLine(gc, x, (centerY + cutoff), laneWidth, stopLaneHeight, lane);
            }
            case EAST -> {
                if (lane.getType() == IntersectionLane.Type.ENTRY) {
                    gc.strokeLine(centerX + cutoff, y + h, x + w, y + h);
                } else {
                    gc.strokeLine(centerX + cutoff, y, x + w, y);
                }
                drawStopLine(gc, (centerX + cutoff), y, stopLaneHeight, laneWidth, lane);
            }
            case WEST -> {
                if (lane.getType() == IntersectionLane.Type.ENTRY) {
                    gc.strokeLine(centerX - cutoff, y, x - w, y);
                } else {
                    gc.strokeLine(centerX - cutoff, y + h, x - w, y + h);
                }
                drawStopLine(gc, (centerX - cutoff - stopLaneHeight), y, stopLaneHeight, laneWidth, lane);
            }
        }
    }

    // Funkcja rysująca linie stopu i dodająca obiekt do klasy StopLine
    private void drawStopLine(GraphicsContext gc, double x1, double y1, double x2, double y2, IntersectionLane lane) {

        if (lane.getType() == IntersectionLane.Type.ENTRY) {
            gc.setFill(Color.WHITE);
            gc.fillRect(x1, y1, x2, y2);
        }
//        else {
//            gc.setFill(Color.RED);
//            gc.fillRect(x1, y1, x2, y2);
//        }

        double centerX = (x1+x2)/2;
        double centerY = (y1+y2)/2;

        StopLine stopLine = new StopLine(lane.getLocalization(), lane.getType(), lane.getIndex(), centerX, centerY);
        stopLines.add(stopLine);
    }
}
