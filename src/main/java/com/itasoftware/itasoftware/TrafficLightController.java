package com.itasoftware.itasoftware;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.util.*;

public class TrafficLightController {

    @FXML Canvas phaseCanvas;
    @FXML private StackPane phaseCanvasContainer;
    @FXML private ScrollPane scrollPane;
    SaveLoadTrafficLight SLTL = new SaveLoadTrafficLight(this);

    @FXML Spinner<Integer> spinnerSinglePhase, spinnerCompletePhase;
    @FXML Button buttonPhaseRed, buttonPhaseYellow, buttonPhaseGreen, buttonPhaseRedYellow, buttonPhaseGreenArrow, buttonShowDiagram, buttonSaveDiagram;
    public static int singlePhase = 1, completePhase, maxCompletePhase = 240;
    private boolean isMousePressed = false;
    public static boolean areTrafficLightsON = false;
    private SinglePhaseButton lastHoveredButton = null;
    private TrafficLight.Phase selectedPhase = TrafficLight.Phase.RED;
    public static final Map<IntersectionLane.Localization, Boolean> hasDedicatedLeftTurnLane = new EnumMap<>(IntersectionLane.Localization.class);
    public static final Map<IntersectionLane.Localization, Boolean> hasDedicatedRightTurnLane = new EnumMap<>(IntersectionLane.Localization.class);
    public static final Map<IntersectionLane.Localization, Boolean> hasDedicatedMainLane = new EnumMap<>(IntersectionLane.Localization.class);

    // Powrót do głównego menu
    @FXML
    public void backToMainMenu() throws IOException  {
        MainApplication mainApp = new MainApplication();
        MainApplication.updateViewSize();
        mainApp.loadView("Main-view.fxml", mainApp.actualWidth, mainApp.actualHeight);
    }

    // Powrót do ekranu symulacji
    @FXML
    public void backToSimulation() throws IOException  {
        MainApplication mainApp = new MainApplication();
        MainApplication.updateViewSize();
        mainApp.loadView("Simulation-view.fxml", mainApp.actualWidth, mainApp.actualHeight);
    }

    // Zapisanie programu faz sygnalizacji świetlnej
    @FXML
    public void saveTrafficLight(ActionEvent event) {
        if (MovementRelations.movementRelations.isEmpty()) {
            AlertPopUp.showAlertPopUp("Can't Save TL");
        } else {
            SLTL.saveTrafficLightPhaseProgram(event);
        }
    }

    // Załadowanie programu faz sygnalizacji świetlnej
    @FXML
    public void loadTrafficLight(ActionEvent event) {
        if (!MovementRelations.movementRelations.isEmpty()) {
            if (AlertPopUp.showAlertPopUp("Load")) {
                SLTL.loadTrafficLightPhaseProgram(event);
            }
        } else {
            SLTL.loadTrafficLightPhaseProgram(event);
        }
    }

    // Utworzenie nowego programu faz sygnalizacji świetlnej
    @FXML
    public void newTrafficLight(ActionEvent event) {
        if (AlertPopUp.showAlertPopUp("New TL")) {
            clearButtons();
            CanvasPhase.rectNumber = 60;
            configureSpinners();
            handleSpinnerSinglePhaseClick();
            loadSpinnersButton();
        }
    }

    @FXML
    public void initialize() {
//        // Reakcja na zmianę rozmiaru kontenera
//        phaseCanvasContainer.layoutBoundsProperty().addListener((obs, oldVal, newVal) -> {
//            CanvasPhase cp = new CanvasPhase();
//            cp.scalePhaseCanvas(phaseCanvasContainer, phaseCanvas);
//        });

        // Blokowanie przewijanie scrolla w pionie
        scrollPane.setVvalue(0);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setPannable(false);
        scrollPane.addEventFilter(ScrollEvent.SCROLL, event -> {
            if (event.getDeltaY() != 0) {
                event.consume();
            }
        });

        // Sprawdzenie, czy istnieją wydzielone lewo i prawoskręty
        areTurningLanesAvailable();

        // Rysowanie
        drawPhaseCanvas(phaseCanvas);

        // Konfiguracja spinnerów
        configureSpinners();
        loadValuesFromSpinners();

        // Załadowanie danych z pliku symulacji (potrzebne, gdyż nie wczytano wcześniej tej instancji) - Rozwiązanie niedocelowe
        if (SimulationController.idLoadedFromSaveFile) {
            SLTL.loadFromTempFile();
            SimulationController.idLoadedFromSaveFile = false;
        }

        // Wykrywanie kliknięcia myszką
        mouseClickHandler();
    }

    // Rysowanie phaseCanvas
    void drawPhaseCanvas(Canvas canvas) {
        CanvasPhase cf = new CanvasPhase();
        cf.drawPhaseCanvas(canvas);
    }

    private void configureSpinners() {
        spinnerSinglePhase.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 5, singlePhase));
        spinnerCompletePhase.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, maxCompletePhase, CanvasPhase.rectNumber * singlePhase));
    }

    // Funkcja pobierajaca wartości ze spinnerów
    private void loadValuesFromSpinners() {
        singlePhase = spinnerSinglePhase.getValue();
        completePhase = spinnerCompletePhase.getValue();
    }

    // Funkcja zmieniająca całkowity okres czasu możliwy do ustawienia w zależności od minimalnego okresu
    @FXML
    public void handleSpinnerSinglePhaseClick() {
        singlePhase = spinnerSinglePhase.getValue();
        int max = singlePhase * maxCompletePhase;
        spinnerCompletePhase.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(singlePhase, max, CanvasPhase.rectNumber * singlePhase, singlePhase));
    }

    // Wyczyszczenie przycisków
    @FXML
    public void clearButtons() {
        SinglePhaseButton.clear();
        drawPhaseCanvas(phaseCanvas);
    }

    // Funkcja obsługująca przycisk do wczytania danych
    @FXML
    public void loadSpinnersButton() {
        //SinglePhaseButton.clear();
        loadValuesFromSpinners();
        CanvasPhase.rectNumber = completePhase/singlePhase;
        drawPhaseCanvas(phaseCanvas);
    }

    // Funkcja obsługująca zapis danych
    @FXML
    private void save() throws IOException {
        assignPhaseSequencesToTrafficLights();
        backToSimulation();
        areTrafficLightsON = true;
    }

    // Funkcje obsługujące zmianę wybranej fazy
    @FXML private void changePhaseRed() { selectedPhase = TrafficLight.Phase.RED; buttonSelectedColor(buttonPhaseRed);}
    @FXML private void changePhaseYellow() { selectedPhase = TrafficLight.Phase.YELLOW; buttonSelectedColor(buttonPhaseYellow);}
    @FXML private void changePhaseGreen() { selectedPhase = TrafficLight.Phase.GREEN; buttonSelectedColor(buttonPhaseGreen);}
    @FXML private void changePhaseRedYellow() { selectedPhase = TrafficLight.Phase.RED_YELLOW; buttonSelectedColor(buttonPhaseRedYellow);}
    @FXML private void changePhaseGreenArrow() { selectedPhase = TrafficLight.Phase.GREEN_ARROW; buttonSelectedColor(buttonPhaseGreenArrow);}

    // Funkcja nadająca danemu przyciskowi kolor oznaczający jego wybranie
    private void buttonSelectedColor(Button button) {
        buttonsStylesReset();
        buttonsDefaultColor();
        button.getStyleClass().remove("obj-button");
        button.getStyleClass().add("obj-button-reverse");
        drawPhaseCanvas(phaseCanvas);
    }

    // Funkcja przywracająca wszystkim przyciskom domyślny kolor
    private void buttonsDefaultColor() {
        buttonPhaseRed.getStyleClass().add("obj-button");
        buttonPhaseYellow.getStyleClass().add("obj-button");
        buttonPhaseGreen.getStyleClass().add("obj-button");
        buttonPhaseRedYellow.getStyleClass().add("obj-button");
        buttonPhaseGreenArrow.getStyleClass().add("obj-button");
        drawPhaseCanvas(phaseCanvas);
    }

    // Wyczyszczenie stylów przycisków
    private void buttonsStylesReset() {
        buttonPhaseRed.getStyleClass().removeAll("obj-button", "obj-button-reverse");
        buttonPhaseYellow.getStyleClass().removeAll("obj-button", "obj-button-reverse");
        buttonPhaseGreen.getStyleClass().removeAll("obj-button", "obj-button-reverse");
        buttonPhaseRedYellow.getStyleClass().removeAll("obj-button", "obj-button-reverse");
        buttonPhaseGreenArrow.getStyleClass().removeAll("obj-button", "obj-button-reverse");
    }

    private void mouseClickHandler() {
        phaseCanvas.setOnMousePressed(event -> {    // Reakcja na naciśnięcie myszy
            isMousePressed = true;
            lastHoveredButton = null;
            handleButtonSelection(event.getX(), event.getY());
        });

        phaseCanvas.setOnMouseDragged(event -> {   // Reakcja na przesunięcie myszy (przyciśniętej)
            if (isMousePressed) {
                handleButtonSelection(event.getX(), event.getY());
            }
        });

        phaseCanvas.setOnMouseReleased(event -> {   // Reakcja, na puszczenie myszy
            isMousePressed = false;
            lastHoveredButton = null; // Resetowanie, żeby po puszczeniu można było znowu zaznaczać
        });
    }

    // Funkcja sprawdzająca, jaka faza jest przypisana do danego przycisku
    private void handleButtonSelection(double rawX, double rawY) {
        double x = rawX / phaseCanvas.getScaleX();
        double y = rawY / phaseCanvas.getScaleY();

        for (SinglePhaseButton spb : SinglePhaseButton.singlePhaseButtons) {
            if (spb.contains(x, y)) {
                if (spb.isActivated()) {
                    if (spb != lastHoveredButton) {
                        spb.changePhase(selectedPhase);
                        lastHoveredButton = spb;
                        drawPhaseCanvas(phaseCanvas);
                    }
                    return;
                }
            }
        }

        // Brak dopasowania - reset
        lastHoveredButton = null;
    }

    // Sprawdzenie, czy istnieją wydzielone lewo i prawoskręty
    private void areTurningLanesAvailable() {
        // Inicjalizacja
        for (IntersectionLane.Localization loc : IntersectionLane.Localization.values()) {
            hasDedicatedLeftTurnLane.put(loc, false);
            hasDedicatedRightTurnLane.put(loc, false);
            hasDedicatedMainLane.put(loc, false);
        }

        // Grupowanie relacji po pasie źródłowym
        Map<IntersectionLane, List<IntersectionLane.Localization>> relationsFromLane = new HashMap<>();

        for (MovementRelations mr : MovementRelations.movementRelations) {
            IntersectionLane from = mr.getObjectA();
            IntersectionLane.Localization toLoc = mr.getObjectB().getLocalization();

            relationsFromLane
                    .computeIfAbsent(from, k -> new ArrayList<>())
                    .add(toLoc);
        }

        // Analiza dla każdego pasa
        for (Map.Entry<IntersectionLane, List<IntersectionLane.Localization>> entry : relationsFromLane.entrySet()) {
            IntersectionLane from = entry.getKey();
            List<IntersectionLane.Localization> targetsLoc = entry.getValue();
            IntersectionLane.Localization fromLoc = from.getLocalization();

            boolean allowsLeft = false;
            boolean allowsRight = false;
            boolean allowsStraight = false;

            for (IntersectionLane.Localization toLoc : targetsLoc) {
                if (isLeftTurn(fromLoc, toLoc)) allowsLeft = true;
                else if (isRightTurn(fromLoc, toLoc)) allowsRight = true;
                else if (isStraight(fromLoc, toLoc)) allowsStraight = true;
            }

            // Sprawdzenie czy to dedykowany pas skrętu i dodanie sygnalizatora
            if (allowsLeft && !allowsStraight && !allowsRight) {
                hasDedicatedLeftTurnLane.put(fromLoc, true);
                TrafficLight.addTrafficLight(from, TrafficLight.LaneType.LEFT);
            }
            if (allowsRight && !allowsStraight && !allowsLeft) {
                hasDedicatedRightTurnLane.put(fromLoc, true);
                TrafficLight.addTrafficLight(from, TrafficLight.LaneType.RIGHT);
            }
            if (allowsStraight || (allowsLeft && allowsRight)) {
                hasDedicatedMainLane.put(fromLoc, true);
                TrafficLight.addTrafficLight(from, TrafficLight.LaneType.MAIN);
            }
        }
    }

    private boolean isLeftTurn(IntersectionLane.Localization from, IntersectionLane.Localization to) {
        return switch (from) {
            case NORTH -> to == IntersectionLane.Localization.EAST;
            case EAST  -> to == IntersectionLane.Localization.SOUTH;
            case SOUTH -> to == IntersectionLane.Localization.WEST;
            case WEST  -> to == IntersectionLane.Localization.NORTH;
        };
    }

    private boolean isRightTurn(IntersectionLane.Localization from, IntersectionLane.Localization to) {
        return switch (from) {
            case NORTH -> to == IntersectionLane.Localization.WEST;
            case EAST  -> to == IntersectionLane.Localization.NORTH;
            case SOUTH -> to == IntersectionLane.Localization.EAST;
            case WEST  -> to == IntersectionLane.Localization.SOUTH;
        };
    }

    private boolean isStraight(IntersectionLane.Localization from, IntersectionLane.Localization to) {
        return switch (from) {
            case NORTH -> to == IntersectionLane.Localization.SOUTH;
            case EAST  -> to == IntersectionLane.Localization.WEST;
            case SOUTH -> to == IntersectionLane.Localization.NORTH;
            case WEST  -> to == IntersectionLane.Localization.EAST;
        };
    }

    // Przypisanie programu faz do danego sygnalizatora
    private void assignPhaseSequencesToTrafficLights() {
        for (TrafficLight light : TrafficLight.trafficLights) {
            List<TrafficLight.Phase> sequence = new ArrayList<>();

            // Znalezienie wiersza na podstawie lokalizacji i typu pasa
            int row = RowDescriptor.getRowNumber(light.getLocalization(), light.getLaneType());
            //System.out.println(light.getLocalization() + " " + light.getLaneType());

            for (int col = 0; col < CanvasPhase.rectNumber; col++) {
                SinglePhaseButton spb = SinglePhaseButton.getSinglePhaseButton(row, col);
                if (spb != null && spb.isActivated()) {
                    // Dodanie odpowiedniej fazy
                    sequence.add(spb.getPhase());
                } else {
                    // Domyślnie RED, w przypadku braku przycisku
                    sequence.add(TrafficLight.Phase.RED);
                }
            }

            light.setPhaseSequence(sequence);
            //System.out.println(sequence);
        }
    }

    // Pokazanie programu faz sygnalizacji świetlnej jako PNG
    @FXML
    public void showDiagram(){
        TrafficLightDiagram.getTrafficLightsDiagramWindow();
    }

    // Zapis programu faz sygnalizacji świetlnej jako PNG
    @FXML
    public void saveDiagramToPNG(ActionEvent event){
        TrafficLightDiagram.saveTrafficDiagramAsPng(event);
    }

}
