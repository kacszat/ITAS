package com.itasoftware.itasoftware;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Slider;
import javafx.scene.layout.StackPane;
import javafx.event.ActionEvent;
import javafx.stage.FileChooser;

import java.io.BufferedReader;
import java.io.File;

import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;
import javafx.stage.Window;

import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class GeneratorController {

    @FXML private Canvas genCanvas;
    @FXML private StackPane genCanvasContainer;
    private static final List<IntersectionLane> intersectionLanes = new ArrayList<>();
    private static final List<StopLine> stopLines = new ArrayList<>();
    private static final List<IntersectionLaneButton> intersectionLaneButtons = new ArrayList<>();
    private final Map<Slider, SliderLane> sliderMap = new HashMap<>();
    @FXML private Slider sliderNorthEntry, sliderNorthExit, sliderSouthEntry, sliderSouthExit, sliderEastEntry, sliderEastExit, sliderWestEntry, sliderWestExit;
    double laneWidth = 25;      // Szerokość pasa ruchu
    double laneHeight = 400;    // Długość pasa ruchu
    boolean isIntersectionLaneButtonShown = false;
    boolean isMRNorthShown, isMRSouthShown, isMREastShown, isMRWestShown = false;
    private IntersectionLaneButton activeEntryButton = null;
    private IntersectionLaneButton activeExitButton = null;
    List<IntersectionLaneButton> listActiveEntryButtons = new ArrayList<>();
    List<IntersectionLaneButton> listActiveExitButtons = new ArrayList<>();
    private MovementRelations movementRelations = new MovementRelations(null, null); // Inicjalziacja relacji

    // Powrót do głównego menu
    @FXML
    public void backToMainMenu() throws IOException  {
        MainApplication mainApp = new MainApplication();
        MainApplication.updateViewSize();
        mainApp.loadView("Main-view.fxml", mainApp.actualWidth, mainApp.actualHeight);
    }

    // Przejście do okna symulacji
    @FXML
    public void goToSimulation() throws IOException  {
        MainApplication mainApp = new MainApplication();
        MainApplication.updateViewSize();
        mainApp.loadView("Simulation-view.fxml", mainApp.actualWidth, mainApp.actualHeight);
    }

    // Przejście do ustawień
    @FXML
    public void goToSettings() throws IOException  {
        MainApplication mainApp = new MainApplication();
        MainApplication.updateViewSize();
        mainApp.loadView("Settings-view.fxml", mainApp.actualWidth, mainApp.actualHeight);
    }

    // Przejście do ustawień
    @FXML
    public void exitITAS() throws IOException  {
        Platform.exit();
    }

    // Zapisanie skrzyżowania
    @FXML
    public void saveIntersection(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Zapisz plik");

        // Filtr rozszerzeń
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Plik skrzyżowania ITAS (*.itaint)", "*.itaint")
        );

        // Okno zapisu
        Window window = ((MenuItem) event.getSource()).getParentPopup().getOwnerWindow();
        File file = fileChooser.showSaveDialog(window);

        if (file != null) {
            SaveLoadIntersection.saveIntersectionLane(file.getAbsolutePath(), intersectionLanes);
            SaveLoadIntersection.saveStopLine(file.getAbsolutePath(), stopLines);
            SaveLoadIntersection.saveIntersectionLaneButton(file.getAbsolutePath(), intersectionLaneButtons);
            SaveLoadIntersection.saveMovementRelations(file.getAbsolutePath(), MovementRelations.movementRelations);
        }
    }

    // Wczytanie skrzyżowania
    @FXML
    public void loadIntersection(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Wczytaj plik");

        // Filtr rozszerzeń
        FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("Pliki ITAINT (*.itaint)", "*.itaint");
        fileChooser.getExtensionFilters().add(filter);

        // Okno wyboru pliku
        Window window = ((MenuItem) event.getSource()).getParentPopup().getOwnerWindow();
        File file = fileChooser.showOpenDialog(window);

        if (file != null) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                intersectionLanes.clear();
                intersectionLaneButtons.clear();
                stopLines.clear();
                MovementRelations.clearMovementRelations();

                String line;
                while ((line = reader.readLine()) != null) {

                    if (line.startsWith("ila,")) {
                        String[] tokens = line.split(",");
                        if (tokens.length == 4) {
                            IntersectionLane.Localization localization = IntersectionLane.Localization.valueOf(tokens[1]);
                            IntersectionLane.Type type = IntersectionLane.Type.valueOf(tokens[2]);
                            int index = Integer.parseInt(tokens[3]);
                            intersectionLanes.add(new IntersectionLane(localization, type, index));

                            // Ustawienie sliderów na bazie danych z wczytanego skrzyżowania
                            if (localization == IntersectionLane.Localization.NORTH && type == IntersectionLane.Type.ENTRY) {
                                sliderNorthEntry.setValue(index + 1);
                            } else if (localization == IntersectionLane.Localization.NORTH && type == IntersectionLane.Type.EXIT) {
                                sliderNorthExit.setValue(index + 1);
                            } else if (localization == IntersectionLane.Localization.SOUTH && type == IntersectionLane.Type.ENTRY) {
                                sliderSouthEntry.setValue(index + 1);
                            } else if (localization == IntersectionLane.Localization.SOUTH && type == IntersectionLane.Type.EXIT) {
                                sliderSouthExit.setValue(index + 1);
                            } else if (localization == IntersectionLane.Localization.EAST && type == IntersectionLane.Type.ENTRY) {
                                sliderEastEntry.setValue(index + 1);
                            } else if (localization == IntersectionLane.Localization.EAST && type == IntersectionLane.Type.EXIT) {
                                sliderEastExit.setValue(index + 1);
                            } else if (localization == IntersectionLane.Localization.WEST && type == IntersectionLane.Type.ENTRY) {
                                sliderWestEntry.setValue(index + 1);
                            } else if (localization == IntersectionLane.Localization.WEST && type == IntersectionLane.Type.EXIT) {
                                sliderWestExit.setValue(index + 1);
                            }
                        }
                    }

                    if (line.startsWith("sl,")) {
                        String[] tokens = line.split(",");
                        if (tokens.length == 6) {
                            IntersectionLane.Localization localization = IntersectionLane.Localization.valueOf(tokens[1]);
                            IntersectionLane.Type type = IntersectionLane.Type.valueOf(tokens[2]);
                            int index = Integer.parseInt(tokens[3]);
                            double x = Double.parseDouble(tokens[4]);
                            double y = Double.parseDouble(tokens[5]);
                            stopLines.add(new StopLine(localization, type, index, x, y));
                        }
                    }

                    if (line.startsWith("ilb,")) {
                        String[] tokens = line.split(",");
                        if (tokens.length == 7) {
                            IntersectionLane.Localization localization = IntersectionLane.Localization.valueOf(tokens[1]);
                            IntersectionLane.Type type = IntersectionLane.Type.valueOf(tokens[2]);
                            int index = Integer.parseInt(tokens[3]);
                            double x = Double.parseDouble(tokens[4]);
                            double y = Double.parseDouble(tokens[5]);
                            double size = Double.parseDouble(tokens[6]);
                            intersectionLaneButtons.add(new IntersectionLaneButton(localization, type, index, x, y, size));
                        }
                    }

                    if (line.startsWith("mr,")) {
                        String[] tokens = line.split(",");
                        if (tokens.length == 13) {
                            // Pierwszy przycisk
                            IntersectionLane.Localization locA = IntersectionLane.Localization.valueOf(tokens[1]);
                            IntersectionLane.Type typeA = IntersectionLane.Type.valueOf(tokens[2]);
                            int indexA = Integer.parseInt(tokens[3]);
                            double xA = Double.parseDouble(tokens[4]);
                            double yA = Double.parseDouble(tokens[5]);
                            double sizeA = Double.parseDouble(tokens[6]);

                            // Drugi przycisk
                            IntersectionLane.Localization locB = IntersectionLane.Localization.valueOf(tokens[7]);
                            IntersectionLane.Type typeB = IntersectionLane.Type.valueOf(tokens[8]);
                            int indexB = Integer.parseInt(tokens[9]);
                            double xB = Double.parseDouble(tokens[10]);
                            double yB = Double.parseDouble(tokens[11]);
                            double sizeB = Double.parseDouble(tokens[12]);

                            // Szukamy pasujących przycisków z listy
                            IntersectionLaneButton a = findIntersectionLaneButton(intersectionLaneButtons, locA, typeA, indexA, xA, yA, sizeA);
                            IntersectionLaneButton b = findIntersectionLaneButton(intersectionLaneButtons, locB, typeB, indexB, xB, yB, sizeB);

                            if (a != null && b != null) {
                                movementRelations.getMovementRelations().add(new MovementRelations(a, b));
                            }
                        }
                    }

                }

                drawCanvas();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Funkcja pomocnicza poszukująca danego intersectionLaneButton
    private IntersectionLaneButton findIntersectionLaneButton(List<IntersectionLaneButton> buttons,
                        IntersectionLane.Localization loc, IntersectionLane.Type type, int index, double x, double y, double size) {
        for (IntersectionLaneButton button : buttons) {
            if (button.getLocalization() == loc &&
                    button.getType() == type &&
                    button.getIndex() == index &&
                    button.getX() == x &&
                    button.getY() == y &&
                    button.getSize() == size) {
                return button;
            }
        }
        return null;
    }

    @FXML
    public void initialize() {
        // Reakcja na zmianę rozmiaru kontenera
        genCanvasContainer.layoutBoundsProperty().addListener((obs, oldVal, newVal) -> {
            scaleCanvas();
        });

        // Wczytanie sliderów i pasów ruchu
        loadSlidersAndIntersectionLanes();

        // Wykrywanie kliknięć myszką
        mouseClickHandler();

        // Czyszczenie listy przycisków
        intersectionLaneButtons.clear();

        // Rysowanie
        drawCanvas();
    }

    // Funkcja wczytująca slidery
    private void loadSlidersAndIntersectionLanes() {
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
                //System.out.println("Zmiana suwaka: " + info.getLocalization() + " " + info.getType() + " -> " + newVal.intValue());
                updateIntersectionLanes(info.getLocalization(), info.getType(), newVal.intValue());
            });
        }
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

        // Rysowanie relacji ruchu
        drawMovementRelations(gc);

        // Rysowanie przycisków
        drawIntersectionLaneButton(gc);

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

        // Czyszczenie listy przycisków
        intersectionLaneButtons.clear();

        // Dodawanie nowych pasów
        for (int i = 0; i < count; i++) {
            intersectionLanes.add(new IntersectionLane(IntersectionLane.Localization.valueOf(slider_localization.name()),
                    IntersectionLane.Type.valueOf(slider_type.name()), i));
        }

        // Ponowne wygenerowanie grafiki
        drawCanvas();
    }

    @FXML
    private void defaultIntersection() {    // Załadowanie domyślnego skrzyżowania
        for (Slider slider : sliderMap.keySet()) {
            slider.setValue(1);
            slider.valueProperty().addListener((obs, oldVal, newVal) -> {
                SliderLane info = sliderMap.get(slider);
                updateIntersectionLanes(info.getLocalization(), info.getType(), newVal.intValue());
            });
        }
        MovementRelations.clearMovementRelations();
    }

    @FXML
    private void clearIntersection() {      // Wyczyszczenie skrzyżowania
        for (Slider slider : sliderMap.keySet()) {
            slider.setValue(0);
            slider.valueProperty().addListener((obs, oldVal, newVal) -> {
                SliderLane info = sliderMap.get(slider);
                updateIntersectionLanes(info.getLocalization(), info.getType(), newVal.intValue());
            });
        }
        MovementRelations.clearMovementRelations();
    }

    // Rysowanie pasów ruchu skrzyżowania
    private void drawLanes(GraphicsContext gc, IntersectionLane lane) {
        double centerX = genCanvas.getWidth() / 2;
        double centerY = genCanvas.getHeight() / 2;

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

        double cutoff = intersectionCount(lane) * laneWidth;
        int stopLaneHeight = 2;
        int buttonSize = 15;
        double buttonBuffer = (laneWidth - buttonSize)/2;

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
                addIntersectionLaneButton((x + buttonBuffer), (centerY - cutoff - stopLaneHeight - laneWidth), buttonSize, lane);
            }
            case SOUTH -> {
                if (lane.getType() == IntersectionLane.Type.ENTRY) {
                    gc.strokeLine(x, centerY + cutoff, x, y + h);
                } else {
                    gc.strokeLine(x + w, centerY + cutoff, x + w, y + h);
                }
                drawStopLine(gc, x, (centerY + cutoff), laneWidth, stopLaneHeight, lane);
                addIntersectionLaneButton((x + buttonBuffer), (centerY + cutoff + laneWidth - buttonSize), buttonSize, lane);
            }
            case EAST -> {
                if (lane.getType() == IntersectionLane.Type.ENTRY) {
                    gc.strokeLine(centerX + cutoff, y + h, x + w, y + h);
                } else {
                    gc.strokeLine(centerX + cutoff, y, x + w, y);
                }
                drawStopLine(gc, (centerX + cutoff), y, stopLaneHeight, laneWidth, lane);
                addIntersectionLaneButton((centerX + cutoff + laneWidth - buttonSize), (y + buttonBuffer), buttonSize, lane);
            }
            case WEST -> {
                if (lane.getType() == IntersectionLane.Type.ENTRY) {
                    gc.strokeLine(centerX - cutoff, y, x - w, y);
                } else {
                    gc.strokeLine(centerX - cutoff, y + h, x - w, y + h);
                }
                drawStopLine(gc, (centerX - cutoff - stopLaneHeight), y, stopLaneHeight, laneWidth, lane);
                addIntersectionLaneButton((centerX - cutoff - stopLaneHeight - laneWidth), (y + buttonBuffer), buttonSize, lane);
            }
        }
    }

    // Obliczenie liczby przecięć dla skracania linii
    private int intersectionCount(IntersectionLane lane) {
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
        return intersectionCount;
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

        double centerX = x1+(x2/2);
        double centerY = y1+(y2/2);

        // Sprawdzenie, czy istnieje już linia stopu na danym pasie ruchu
        StopLine existingSL = stopLines.stream()
                .filter(sl -> sl.getLocalization() == lane.getLocalization() &&
                        sl.getType() == lane.getType() &&
                        sl.getIndex() == lane.getIndex())
                .findFirst()
                .orElse(null);

        // Tworzenie nowej linii stopu w przypadku jej braku
        if (existingSL == null) {
            StopLine stopLine = new StopLine(lane.getLocalization(), lane.getType(), lane.getIndex(), centerX, centerY);
            stopLines.add(stopLine);
        }
    }

    // Funkcja wykrywająca kliknięcie w przycisk
    private void mouseClickHandler() {
        genCanvas.setOnMouseClicked(event -> {
            double clickX = event.getX() / genCanvas.getScaleX();
            double clickY = event.getY() / genCanvas.getScaleY();
            listActiveEntryButtons.clear();
            listActiveExitButtons.clear();

            for (IntersectionLaneButton iLButton : intersectionLaneButtons) {
                if (iLButton.contains(clickX, clickY)) {
                    if (iLButton.getType() == IntersectionLane.Type.ENTRY && activeExitButton == null) {
                        if (iLButton.isActive()) {      // Jeśli jakiś przycisk typu Entry jest aktywny, wyłączamy go
                            iLButton.toggle();
                            activeEntryButton = null;
                            // Wyłączenie włączonych Exitów dla danego Entry
                            for (IntersectionLaneButton b : intersectionLaneButtons) {
                                if (b.getType() == IntersectionLane.Type.EXIT && b.isActive()) {
                                    movementRelations.addMovementRelation(iLButton, b);     // Dodanie relacji pomiędzy dwoma punktami przyciskami
                                    b.toggle();
                                }
                            }
                        } else {
                            // Jeśli kliknięto jakiś przycisk typu Entry, to ustawiamy go jako aktywny
                            if (activeEntryButton == null) {
                                iLButton.toggle();
                                activeEntryButton = iLButton;   // Zapamiętujemy jaki przycisk Entry jest aktywny
                            }
                            // Kliknięcie innego przycisku Entry nie powoduje żadnej akcji, są zablokowane
                        }
                    } else if (iLButton.getType() == IntersectionLane.Type.EXIT && activeEntryButton != null) {
                        iLButton.toggle(); // Przyciski typu Exit nie są zablokowane
                    }

                    if (iLButton.getType() == IntersectionLane.Type.EXIT && activeEntryButton == null) {
                        if (iLButton.isActive()) {      // Jeśli jakiś przycisk typu Exit jest aktywny, wyłączamy go
                            iLButton.toggle();
                            activeExitButton = null;
                            // Wyłączenie włączonych Entrów dla danego Exit
                            for (IntersectionLaneButton b : intersectionLaneButtons) {
                                if (b.getType() == IntersectionLane.Type.ENTRY && b.isActive()) {
                                    movementRelations.addMovementRelation(b, iLButton);     // Dodanie relacji pomiędzy dwoma punktami przyciskami
                                    b.toggle();
                                }
                            }
                        } else {
                            // Jeśli kliknięto jakiś przycisk typu Exit, to ustawiamy go jako aktywny
                            if (activeExitButton == null) {
                                iLButton.toggle();
                                activeExitButton = iLButton;   // Zapamiętujemy jaki przycisk Exit jest aktywny
                            }
                            // Kliknięcie innego przycisku Exit nie powoduje żadnej akcji, są zablokowane
                        }
                    } else if (iLButton.getType() == IntersectionLane.Type.ENTRY && activeExitButton != null) {
                        iLButton.toggle(); // Przyciski typu Entry nie są zablokowane
                        listActiveEntryButtons.add(iLButton);
                    }

                    drawCanvas();
                    break;
                }
            }
        });
    }

    @FXML   // Pokazanie przycisków na pasach ruchu
    private void showIntersectionLaneButton() {
        // Wyzerowanie stanu przycisków przed ponownym włączeniem
        intersectionLaneButtons.clear();
        activeEntryButton = null;
        activeExitButton = null;
        isIntersectionLaneButtonShown = !isIntersectionLaneButtonShown;
        drawCanvas();
    }

    // Funkcja dodająca obiekt do klasy IntersectionLaneButton
    private void addIntersectionLaneButton(double x1, double y1, double buttonSize, IntersectionLane lane) {
        if (isIntersectionLaneButtonShown) {
            // Sprawdzenie, czy istnieje już przycisk na danym pasie ruchu
            IntersectionLaneButton existingButton = intersectionLaneButtons.stream()
                    .filter(b -> b.getLocalization() == lane.getLocalization() &&
                            b.getType() == lane.getType() &&
                            b.getIndex() == lane.getIndex())
                    .findFirst()
                    .orElse(null);
            // Tworzenie nowego przycisku w przypadku jego braku
            if (existingButton == null) {
                IntersectionLaneButton button = new IntersectionLaneButton(lane.getLocalization(), lane.getType(), lane.getIndex(), x1, y1, buttonSize);
                intersectionLaneButtons.add(button);
            }
        }
    }

    // Funkcja rysująca przycisk
    private void drawIntersectionLaneButton(GraphicsContext gc) {
        if (isIntersectionLaneButtonShown) {
            for (IntersectionLaneButton existingButton : intersectionLaneButtons) {
                Color buttonColor = null;

                if (existingButton.getType() == IntersectionLaneButton.Type.ENTRY && activeExitButton == null) {
                    if (existingButton == activeEntryButton) {
                        buttonColor = Color.LIME;
                    } else if (activeEntryButton != null) {
                        buttonColor = Color.rgb(40, 40, 40, 1.0);
                    } else {
                        buttonColor = Color.rgb(200, 200, 0, 1.0);
                    }
                } else if (existingButton.getType() == IntersectionLaneButton.Type.EXIT && activeExitButton == null) {
                    if (existingButton.isActive()) {
                        buttonColor = Color.GREEN;
                    } else if (activeEntryButton != null) {
                        buttonColor = Color.RED;
                    } else {
                        buttonColor = Color.rgb(200, 200, 0, 1.0);
                    }
                }

                if (existingButton.getType() == IntersectionLaneButton.Type.EXIT && activeEntryButton == null) {
                    if (existingButton == activeExitButton) {
                        buttonColor = Color.LIME;
                    } else if (activeExitButton != null) {
                        buttonColor = Color.rgb(40, 40, 40, 1.0);
                    } else {
                        buttonColor = Color.rgb(200, 200, 0, 1.0);
                    }
                } else if (existingButton.getType() == IntersectionLaneButton.Type.ENTRY && activeEntryButton == null) {
                    if (existingButton.isActive()) {
                        buttonColor = Color.GREEN;
                    } else if (activeExitButton != null) {
                        buttonColor = Color.RED;
                    } else {
                        buttonColor = Color.rgb(200, 200, 0, 1.0);
                    }
                }

                gc.setFill(buttonColor);
                gc.fillRect(existingButton.getX(), existingButton.getY(), existingButton.getSize(), existingButton.getSize());
            }
        }

    }

    @FXML   // Pokazanie wszystkich relacji ruchu
    private void showMovementRelations() {
        isMRNorthShown = true;
        isMRSouthShown = true;
        isMREastShown = true;
        isMRWestShown = true;
        drawCanvas();
    }

    @FXML   // Ukrycie wszystkich relacji ruchu
    private void hideMovementRelations() {
        isMRNorthShown = false;
        isMRSouthShown = false;
        isMREastShown = false;
        isMRWestShown = false;
        drawCanvas();
    }

    @FXML   // Pokazanie relacji ruchu z północy
    private void showMovementRelationsNorth() {
        isMRNorthShown = !isMRNorthShown;
        drawCanvas();
    }

    @FXML   // Pokazanie relacji ruchu z południa
    private void showMovementRelationsSouth() {
        isMRSouthShown = !isMRSouthShown;
        drawCanvas();
    }

    @FXML   // Pokazanie relacji ruchu ze wschodu
    private void showMovementRelationsEast() {
        isMREastShown = !isMREastShown;
        drawCanvas();
    }

    @FXML   // Pokazanie relacji ruchu z zachodu
    private void showMovementRelationsWest() {
        isMRWestShown = !isMRWestShown;
        drawCanvas();
    }

    @FXML   // Pokazanie relacji ruchu
    private void clearMovementRelations() {
        MovementRelations.clearMovementRelations();
        drawCanvas();
    }

    // Funkcja rysująca relację ruchu
    private void drawMovementRelations(GraphicsContext gc) {
        if (isMRNorthShown || isMRSouthShown || isMREastShown || isMRWestShown) {
            double A_X = 0, A_Y = 0, B_X = 0, B_Y = 0;

            for (MovementRelations relation : movementRelations.getMovementRelations()) {
                IntersectionLaneButton objectA = relation.getObjectA();
                IntersectionLaneButton objectB = relation.getObjectB();

                for (StopLine stopline : stopLines) {
                    if (objectA.getLocalization().equals(stopline.getLocalization()) &&
                            objectA.getType().equals(stopline.getType()) &&
                            objectA.getIndex() == stopline.getIndex()) {
                        A_X = stopline.getPositionCenterX();
                        A_Y = stopline.getPositionCenterY();
                    }
                    if (objectB.getLocalization().equals(stopline.getLocalization()) &&
                            objectB.getType().equals(stopline.getType()) &&
                            objectB.getIndex() == stopline.getIndex()) {
                        B_X = stopline.getPositionCenterX();
                        B_Y = stopline.getPositionCenterY();
                    }
                }
                drawMovementRelationsLines(gc, A_X, A_Y, B_X, B_Y, objectA, objectB);

            }
        }
    }

    // Bezpośrednie rysowanie lini relacji
    private void drawMovementRelationsLines(GraphicsContext gc, double A_X, double A_Y, double B_X, double B_Y,
                                            IntersectionLaneButton btA, IntersectionLaneButton btB) {

        // Parametry linii relacji ruchu
        double control_X1 = 0, control_X2 = 0, control_Y1 = 0, control_Y2 = 0;
        int indexA = btA.getIndex() + 1, indexB = btB.getIndex() + 1;
        double control_Offset_Left = laneWidth * 1.5 * ((double) indexB / indexA);
        double control_Offset_Right = laneWidth * 0.5 * ((double) indexB / indexA);
        double control_Offset_Back = laneWidth * ((double) indexB / indexA);
        boolean drawCurve = false;
        //gc.setStroke(Color.rgb(50, 255, 50, 1.0));
        gc.setStroke(Color.RED);
        gc.setLineWidth(3);

        // Zdefiniowanie bazowych współrzędnych punktów kontrolnych
        control_X1 = A_X;
        control_Y1 = A_Y;
        control_X2 = B_X;
        control_Y2 = B_Y;

        // Wyznaczenie przebiegu linii w zależności od punktu początkowego i końcowego (modyfikacja punktów kontrolnych)
        if (btA.getLocalization() == IntersectionLaneButton.Localization.NORTH && isMRNorthShown) { // Jazda z północy
            if (btB.getLocalization() == IntersectionLane.Localization.NORTH) { // Zawrócenie
                control_Y1 = A_Y + control_Offset_Back;
                control_Y2 = B_Y + control_Offset_Back;
                drawCurve = true;
                gc.strokeLine(B_X, B_Y, B_X,0);
            } else if (btB.getLocalization() == IntersectionLane.Localization.EAST) { // Skręt w lewo
                control_Y1 = A_Y + control_Offset_Left;
                control_X2 = B_X - control_Offset_Left;
                drawCurve = true;
                gc.strokeLine(B_X, B_Y, 2*laneHeight, B_Y);
            } else if (btB.getLocalization() == IntersectionLane.Localization.WEST) { // Skręt w prawo
                control_Y1 = A_Y + control_Offset_Right;
                control_X2 = B_X + control_Offset_Right;
                drawCurve = true;
                gc.strokeLine(B_X, B_Y, 0, B_Y);
            } else {    // Jeśli kierunek jazdy to na wprost, rysujemy prostą linię
                gc.strokeLine(A_X, A_Y, B_X, B_Y);
                gc.strokeLine(B_X, B_Y, B_X,2*laneHeight);
            }
            gc.strokeLine(A_X, A_Y, A_X,0);
        } else if (btA.getLocalization() == IntersectionLaneButton.Localization.SOUTH && isMRSouthShown) { // Jazda z południa
            if (btB.getLocalization() == IntersectionLane.Localization.SOUTH) { // Zawrócenie
                control_Y1 = A_Y - control_Offset_Back;
                control_Y2 = B_Y - control_Offset_Back;
                drawCurve = true;
                gc.strokeLine(B_X, B_Y, B_X,2*laneHeight);
            } else if (btB.getLocalization() == IntersectionLane.Localization.WEST) { // Skręt w lewo
                control_Y1 = A_Y - control_Offset_Left;
                control_X2 = B_X + control_Offset_Left;
                drawCurve = true;
                gc.strokeLine(B_X, B_Y, 0, B_Y);
            } else if (btB.getLocalization() == IntersectionLane.Localization.EAST) { // Skręt w prawo
                control_Y1 = A_Y - control_Offset_Right;
                control_X2 = B_X - control_Offset_Right;
                drawCurve = true;
                gc.strokeLine(B_X, B_Y, 2*laneHeight, B_Y);
            } else {    // Jeśli kierunek jazdy to na wprost, rysujemy prostą linię
                gc.strokeLine(A_X, A_Y, B_X, B_Y);
                gc.strokeLine(B_X, B_Y, B_X,0);
            }
            gc.strokeLine(A_X, A_Y, A_X,2*laneHeight);
        } else if (btA.getLocalization() == IntersectionLaneButton.Localization.EAST && isMREastShown) { // Jazda ze wschodu
            if (btB.getLocalization() == IntersectionLane.Localization.EAST) { // Zawrócenie
                control_X1 = A_X - control_Offset_Back;
                control_X2 = B_X - control_Offset_Back;
                drawCurve = true;
                gc.strokeLine(B_X, B_Y, 2*laneHeight,B_Y);
            } else if (btB.getLocalization() == IntersectionLane.Localization.SOUTH) { // Skręt w lewo
                control_X1 = A_X - control_Offset_Left;
                control_Y2 = B_Y - control_Offset_Left;
                drawCurve = true;
                gc.strokeLine(B_X, B_Y, B_X,2*laneHeight);
            } else if (btB.getLocalization() == IntersectionLane.Localization.NORTH) { // Skręt w prawo
                control_X1 = A_X - control_Offset_Right;
                control_Y2 = B_Y + control_Offset_Right;
                drawCurve = true;
                gc.strokeLine(B_X, B_Y, B_X,0);
            } else {    // Jeśli kierunek jazdy to na wprost, rysujemy prostą linię
                gc.strokeLine(A_X, A_Y, B_X, B_Y);
                gc.strokeLine(B_X, B_Y, 0,B_Y);
            }
            gc.strokeLine(A_X, A_Y, 2*laneHeight, A_Y);
        } else if (btA.getLocalization() == IntersectionLaneButton.Localization.WEST && isMRWestShown) { // Jazda z zachodu
            if (btB.getLocalization() == IntersectionLane.Localization.WEST) { // Zawrócenie
                control_X1 = A_X + control_Offset_Back;
                control_X2 = B_X + control_Offset_Back;
                drawCurve = true;
                gc.strokeLine(B_X, B_Y, 0,B_Y);
            } else if (btB.getLocalization() == IntersectionLane.Localization.NORTH) { // Skręt w lewo
                control_X1 = A_X + control_Offset_Left;
                control_Y2 = B_Y + control_Offset_Left;
                drawCurve = true;
                gc.strokeLine(B_X, B_Y, B_X,0);
            } else if (btB.getLocalization() == IntersectionLane.Localization.SOUTH) { // Skręt w prawo
                control_X1 = A_X + control_Offset_Right;
                control_Y2 = B_Y - control_Offset_Right;
                drawCurve = true;
                gc.strokeLine(B_X, B_Y, B_X,2*laneHeight);
            } else {    // Jeśli kierunek jazdy to na wprost, rysujemy prostą linię
                gc.strokeLine(A_X, A_Y, B_X, B_Y);
                gc.strokeLine(B_X, B_Y, 2*laneHeight,B_Y);
            }
            gc.strokeLine(A_X, A_Y, 0, A_Y);
        }

        if (drawCurve) {
            gc.beginPath();
            gc.moveTo(A_X, A_Y); // Ustawienie początkowego punktu paraboli
            gc.bezierCurveTo(control_X1, control_Y1, control_X2, control_Y2, B_X, B_Y); // Ustawienie punktów kontrolnych i końcowego paraboli
            gc.stroke();
        }

    }

}
