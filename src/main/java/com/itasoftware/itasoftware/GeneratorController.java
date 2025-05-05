package com.itasoftware.itasoftware;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Slider;
import javafx.scene.layout.StackPane;
import javafx.event.ActionEvent;

import javafx.scene.canvas.Canvas;

import java.io.IOException;
import java.util.*;

public class GeneratorController {

    @FXML Canvas genCanvas;
    @FXML private StackPane genCanvasContainer;
    static final List<IntersectionLane> intersectionLanes = new ArrayList<>();
    static final List<StopLine> stopLines = new ArrayList<>();
    static final List<IntersectionLaneButton> intersectionLaneButtons = new ArrayList<>();
    static final List<BorderLine> borderLines = new ArrayList<>();
    final Map<Slider, SliderLane> sliderMap = new HashMap<>();
    @FXML Slider sliderNorthEntry, sliderNorthExit, sliderSouthEntry, sliderSouthExit, sliderEastEntry, sliderEastExit, sliderWestEntry, sliderWestExit;
    static boolean isIntersectionLaneButtonShown = false;
    static boolean isMRNorthShown, isMRSouthShown, isMREastShown, isMRWestShown;
    static IntersectionLaneButton activeEntryButton = null;
    static IntersectionLaneButton activeExitButton = null;
    List<IntersectionLaneButton> listActiveEntryButtons = new ArrayList<>();
    List<IntersectionLaneButton> listActiveExitButtons = new ArrayList<>();
    MovementRelations movementRelations = new MovementRelations(null, null); // Inicjalziacja relacji
    SaveLoadIntersection SLI = new SaveLoadIntersection(this);

    // Powrót do głównego menu
    @FXML
    public void backToMainMenu() throws IOException  {
        if (AlertPopUp.showAlertPopUp("Quit Generator")) {
            MainApplication mainApp = new MainApplication();
            MainApplication.updateViewSize();
            mainApp.loadView("Main-view.fxml", mainApp.actualWidth, mainApp.actualHeight);
        }
    }

    // Przejście do okna symulacji
    @FXML
    public void goToSimulation() throws IOException  {
        if (AlertPopUp.showAlertPopUp("Quit Generator")) {
            MainApplication mainApp = new MainApplication();
            MainApplication.updateViewSize();
            mainApp.loadView("Simulation-view.fxml", mainApp.actualWidth, mainApp.actualHeight);
        }

    }

    // Przejście do ustawień
    @FXML
    public void goToSettings() throws IOException  {
        if (AlertPopUp.showAlertPopUp("Quit Generator")) {
            MainApplication mainApp = new MainApplication();
            MainApplication.updateViewSize();
            mainApp.loadView("Settings-view.fxml", mainApp.actualWidth, mainApp.actualHeight);
        }
    }

    // Przejście do ustawień
    @FXML
    public void exitITAS() throws IOException  {
        if (AlertPopUp.showAlertPopUp("Save")) {
            Platform.exit();
        }
    }

    // Przejście do okna symulacji (dolny przycisk)
    @FXML
    public void goToSimulationBottomButton() throws IOException  {
        if (MovementRelations.movementRelations.isEmpty()) {
            if (AlertPopUp.showAlertPopUp("Lack Relations")) {
                MainApplication mainApp = new MainApplication();
                MainApplication.updateViewSize();
                mainApp.loadView("Simulation-view.fxml", mainApp.actualWidth, mainApp.actualHeight);
            }
        } else {
            MainApplication mainApp = new MainApplication();
            MainApplication.updateViewSize();
            mainApp.loadView("Simulation-view.fxml", mainApp.actualWidth, mainApp.actualHeight);
        }

    }

    // Zapisanie skrzyżowania
    @FXML
    public void saveIntersection(ActionEvent event) {
        SLI.saveIntersection(event);
    }

    // Wczytanie skrzyżowania
    @FXML
    public void loadIntersection(ActionEvent event) {
        SLI.loadIntersection(event);
    }

    @FXML
    public void initialize() {
        // Reakcja na zmianę rozmiaru kontenera
        genCanvasContainer.layoutBoundsProperty().addListener((obs, oldVal, newVal) -> {
            CanvasDrawer drawer = new CanvasDrawer();
            drawer.scaleCanvas(genCanvasContainer, genCanvas);
        });

        // Wczytanie sliderów i pasów ruchu
        loadSlidersAndIntersectionLanes();

        // Wykrywanie kliknięć myszką
        mouseClickHandler();

        // Czyszczenie listy przycisków
        intersectionLaneButtons.clear();

        // Rysowanie
        drawCanvas(genCanvas);

        // Pokazywanie relacji (od początku, jeśli nie zakomentowane)
        showMovementRelations();
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

    // Rysowanie genCanvas
    void drawCanvas(Canvas canvas) {
        CanvasDrawer drawer = new CanvasDrawer();
        drawer.drawCanvas(canvas);
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
        drawCanvas(genCanvas);
    }

    @FXML
    private void defaultIntersectionMenuBar() {     // Załadowanie domyślnego skrzyżowania (ale z menubar)
        if (AlertPopUp.showAlertPopUp("New Generator")) {
            defaultIntersection();
        }
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
        clearMovementRelations();
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
        clearMovementRelations();
        MovementRelations.clearMovementRelations();
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

                    drawCanvas(genCanvas);
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
        drawCanvas(genCanvas);
    }

    // Funkcja dodająca obiekt do klasy IntersectionLaneButton
    void addIntersectionLaneButton(double x1, double y1, double buttonSize, IntersectionLane lane) {
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

    // Funkcja mogąca rysować linie granicy i dodająca obiekt do klasy BorderLine
    void addBorderLine(double x1, double y1, double x2, double y2, IntersectionLane lane) {
        double centerX = x1+(x2/2);
        double centerY = y1+(y2/2);

        // Sprawdzenie, czy istnieje już linia graniczna na danym pasie ruchu
        BorderLine existingBL = GeneratorController.borderLines.stream()
                .filter(sl -> sl.getLocalization() == lane.getLocalization() &&
                        sl.getType() == lane.getType() &&
                        sl.getIndex() == lane.getIndex())
                .findFirst()
                .orElse(null);

        // Tworzenie nowej linii stopu w przypadku jej braku
        if (existingBL == null) {
            BorderLine borderLine = new BorderLine(lane.getLocalization(), lane.getType(), lane.getIndex(), centerX, centerY);
            GeneratorController.borderLines.add(borderLine);
        }
    }

    @FXML   // Pokazanie wszystkich relacji ruchu
    private void showMovementRelations() {
        isMRNorthShown = true;
        isMRSouthShown = true;
        isMREastShown = true;
        isMRWestShown = true;
        drawCanvas(genCanvas);
    }

    @FXML   // Ukrycie wszystkich relacji ruchu
    private void hideMovementRelations() {
        isMRNorthShown = false;
        isMRSouthShown = false;
        isMREastShown = false;
        isMRWestShown = false;
        drawCanvas(genCanvas);
    }

    @FXML   // Pokazanie relacji ruchu z północy
    private void showMovementRelationsNorth() {
        isMRNorthShown = !isMRNorthShown;
        drawCanvas(genCanvas);
    }

    @FXML   // Pokazanie relacji ruchu z południa
    private void showMovementRelationsSouth() {
        isMRSouthShown = !isMRSouthShown;
        drawCanvas(genCanvas);
    }

    @FXML   // Pokazanie relacji ruchu ze wschodu
    private void showMovementRelationsEast() {
        isMREastShown = !isMREastShown;
        drawCanvas(genCanvas);
    }

    @FXML   // Pokazanie relacji ruchu z zachodu
    private void showMovementRelationsWest() {
        isMRWestShown = !isMRWestShown;
        drawCanvas(genCanvas);
    }

    @FXML   // Pokazanie relacji ruchu
    private void clearMovementRelations() {
        MovementRelations.clearMovementRelations();
        drawCanvas(genCanvas);
    }

}
