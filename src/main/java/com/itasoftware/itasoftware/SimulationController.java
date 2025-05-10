package com.itasoftware.itasoftware;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.control.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.StackPane;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SimulationController {

    @FXML private Label simInfoLabel;
    @FXML private Canvas simCanvas;
    @FXML private StackPane simCanvasContainer;

    // Utworzenie nowych instancji klas CanvasDrawer, SimulationLoop i VehicleManager
    CanvasDrawer canvasDrawer = new CanvasDrawer();
    SimulationLoop simLoop;
    VehicleManager vehicleManager = new VehicleManager();

    private final Map<TextField, TextFieldVehicleNumber> textfieldMap = new HashMap<>();
    private List<TextFieldVehicleNumber> tfVNInput = new ArrayList<>();
    @FXML private TextField tfNorthLeft, tfNorthStraight, tfNorthRight, tfNorthBack, tfSouthLeft, tfSouthStraight, tfSouthRight, tfSouthBack,
                            tfWestLeft, tfWestStraight, tfWestRight, tfWestBack, tfEastLeft, tfEastStraight, tfEastRight, tfEastBack;
    @FXML Slider sliderTimeSpeed;
    @FXML Spinner<Integer> spinnerTimeHours, spinnerTimeMinutes;
    @FXML Label labelTime;
    @FXML Button buttonStart;
    static double simSpeed = 1.0, tfVehicleSum = 0;
    Integer simTimeLength = 0;
    boolean isSimulationActive = false;


    MovementTrajectory movTraj;     // Trajektoria ruchu
    Map<MovementRelations, MovementTrajectory> movementMap = new HashMap<>();   // Hash mapa z powiązanymi relacjami i trajektoriami ruchu

    // Powrót do głównego menu
    @FXML
    public void backToMainMenu() throws IOException  {
        if (AlertPopUp.showAlertPopUp("Quit Simulation")) {
            MainApplication mainApp = new MainApplication();
            MainApplication.updateViewSize();
            mainApp.loadView("Main-view.fxml", mainApp.actualWidth, mainApp.actualHeight);
        }
    }

    // Przejście do okna symulacji
    @FXML
    public void goToGenerator() throws IOException  {
        if (AlertPopUp.showAlertPopUp("Quit Simulation")) {
            MainApplication mainApp = new MainApplication();
            MainApplication.updateViewSize();
            mainApp.loadView("Generator-view.fxml", mainApp.actualWidth, mainApp.actualHeight);
        }

    }

    // Przejście do ustawień
    @FXML
    public void goToSettings() throws IOException  {
        if (AlertPopUp.showAlertPopUp("Quit Simulation")) {
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

    @FXML
    public void initialize() {
        loadCanvasOrInfo(canvasDrawer);
        loadTextField();
        configureTimeSpeedSlider();
        configureTimeSpinners();
        updateTextFieldActivityAndDefaultValue();

        // Utworzenie pętli symulacji
        simLoop = new SimulationLoop(simCanvas, canvasDrawer, vehicleManager, simSpeed, this);
    }

    // Załadowanie Canvas lub informacji, w przypadku jego braku
    private void loadCanvasOrInfo(CanvasDrawer drawer) {
        if (!MovementRelations.movementRelations.isEmpty()) {
            // Reakcja na zmianę rozmiaru kontenera
            simCanvasContainer.layoutBoundsProperty().addListener((obs, oldVal, newVal) -> {
                drawer.scaleCanvas(simCanvasContainer, simCanvas);
            });

            // Ukrycie przycisków wyznaczania relacji
            GeneratorController.isIntersectionLaneButtonShown = false;

            // Rysowanie
            drawer.drawCanvas(simCanvas);

            simCanvas.setVisible(true);
            simInfoLabel.setVisible(false);
        } else {
            simInfoLabel.setText("Brak załadowanego pliku symulacji lub skrzyżowania");
            simCanvas.setVisible(false);
            simInfoLabel.setVisible(true);
        }
    }

    private void configureTimeSpeedSlider() {
        sliderTimeSpeed.setMin(1);
        sliderTimeSpeed.setMax(10);
        sliderTimeSpeed.setValue(1);
        sliderTimeSpeed.setBlockIncrement(1);
        sliderTimeSpeed.setMajorTickUnit(1);
        sliderTimeSpeed.setMinorTickCount(0);
        sliderTimeSpeed.setSnapToTicks(true);
        sliderTimeSpeed.setShowTickMarks(true);
        sliderTimeSpeed.setShowTickLabels(true);

        // Pobranie aktualnie ustawionej wartości na sliderze
        sliderTimeSpeed.valueProperty().addListener((obs, oldVal, newVal) -> {
            simSpeed = newVal.doubleValue();
            simLoop.setSimSpeed(simSpeed);
        });
    }

    private void configureTimeSpinners() {
        spinnerTimeHours.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 0));
        spinnerTimeMinutes.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0));
    }

    // Funkcja pobierajaca wartości ze spinnerów
    private void loadSimTimeLength() {
        int hours = spinnerTimeHours.getValue();
        int minutes = spinnerTimeMinutes.getValue();
        simTimeLength = hours * 60 + minutes;
        simLoop.setSimTimeLength(simTimeLength);
    }

    // Załadowanie TextFieldów (sam textfield, potem obiekt klasy tfCarNumber z lokalizacją startową, typem lokalizacji startowej i lokalizacją końcową)
    private void loadTextField() {
        textfieldMap.put(tfNorthLeft, new TextFieldVehicleNumber(TextFieldVehicleNumber.Localization.NORTH, TextFieldVehicleNumber.Type.ENTRY, IntersectionLane.Localization.EAST));
        textfieldMap.put(tfNorthStraight, new TextFieldVehicleNumber(TextFieldVehicleNumber.Localization.NORTH, TextFieldVehicleNumber.Type.ENTRY, TextFieldVehicleNumber.Localization.SOUTH));
        textfieldMap.put(tfNorthRight, new TextFieldVehicleNumber(TextFieldVehicleNumber.Localization.NORTH, TextFieldVehicleNumber.Type.ENTRY, TextFieldVehicleNumber.Localization.WEST));
        textfieldMap.put(tfNorthBack, new TextFieldVehicleNumber(TextFieldVehicleNumber.Localization.NORTH, TextFieldVehicleNumber.Type.ENTRY, TextFieldVehicleNumber.Localization.NORTH));
        textfieldMap.put(tfSouthLeft, new TextFieldVehicleNumber(TextFieldVehicleNumber.Localization.SOUTH, TextFieldVehicleNumber.Type.ENTRY, TextFieldVehicleNumber.Localization.WEST));
        textfieldMap.put(tfSouthStraight, new TextFieldVehicleNumber(TextFieldVehicleNumber.Localization.SOUTH, TextFieldVehicleNumber.Type.ENTRY, TextFieldVehicleNumber.Localization.NORTH));
        textfieldMap.put(tfSouthRight, new TextFieldVehicleNumber(TextFieldVehicleNumber.Localization.SOUTH, TextFieldVehicleNumber.Type.ENTRY, TextFieldVehicleNumber.Localization.EAST));
        textfieldMap.put(tfSouthBack, new TextFieldVehicleNumber(TextFieldVehicleNumber.Localization.SOUTH, TextFieldVehicleNumber.Type.ENTRY, TextFieldVehicleNumber.Localization.SOUTH));
        textfieldMap.put(tfWestLeft, new TextFieldVehicleNumber(TextFieldVehicleNumber.Localization.WEST, TextFieldVehicleNumber.Type.ENTRY, TextFieldVehicleNumber.Localization.NORTH));
        textfieldMap.put(tfWestStraight, new TextFieldVehicleNumber(TextFieldVehicleNumber.Localization.WEST, TextFieldVehicleNumber.Type.ENTRY, TextFieldVehicleNumber.Localization.EAST));
        textfieldMap.put(tfWestRight, new TextFieldVehicleNumber(TextFieldVehicleNumber.Localization.WEST, TextFieldVehicleNumber.Type.ENTRY, TextFieldVehicleNumber.Localization.SOUTH));
        textfieldMap.put(tfWestBack, new TextFieldVehicleNumber(TextFieldVehicleNumber.Localization.WEST, TextFieldVehicleNumber.Type.ENTRY, TextFieldVehicleNumber.Localization.WEST));
        textfieldMap.put(tfEastLeft, new TextFieldVehicleNumber(TextFieldVehicleNumber.Localization.EAST, TextFieldVehicleNumber.Type.ENTRY, TextFieldVehicleNumber.Localization.SOUTH));
        textfieldMap.put(tfEastStraight, new TextFieldVehicleNumber(TextFieldVehicleNumber.Localization.EAST, TextFieldVehicleNumber.Type.ENTRY, TextFieldVehicleNumber.Localization.WEST));
        textfieldMap.put(tfEastRight, new TextFieldVehicleNumber(TextFieldVehicleNumber.Localization.EAST, TextFieldVehicleNumber.Type.ENTRY, TextFieldVehicleNumber.Localization.NORTH));
        textfieldMap.put(tfEastBack, new TextFieldVehicleNumber(TextFieldVehicleNumber.Localization.EAST, TextFieldVehicleNumber.Type.ENTRY, TextFieldVehicleNumber.Localization.EAST));
        tfVNInput = new ArrayList<>(textfieldMap.values());
    }

    // Sprawdzenie stanu textfieldów
    public void updateTextFieldActivityAndDefaultValue() {
        // Filtr znaków (dopuszczalne tylko cyfry)
        UnaryOperator<TextFormatter.Change> digitFilter = change -> {
            String newText = change.getControlNewText();
            return (newText.matches("\\d*")) ? change : null;
        };

        for (Map.Entry<TextField, TextFieldVehicleNumber> entry : textfieldMap.entrySet()) {
            TextField tf = entry.getKey();
            TextFieldVehicleNumber tfCarNum = entry.getValue();

            tf.setTextFormatter(new TextFormatter<>(digitFilter)); // Dodanie filtra do TF
            tf.setText("0"); // Ustawienie domyślnej wartości
            tfCarNum.setCarsNumber(0); // Ustawienie liczby samochodów równej zero na każdym tfCarNum

            boolean textFieldAndRelationMatch = false;

            for (MovementRelations relation : MovementRelations.movementRelations) {
                if (relation.getObjectA().getLocalization().equals(tfCarNum.getLocalization()) &&
                        relation.getObjectA().getType().equals(tfCarNum.getType()) &&
                        relation.getObjectB().getLocalization().equals(tfCarNum.getDestination())) {

                    textFieldAndRelationMatch = true;
                    break;
                }
            }

            tf.setDisable(!textFieldAndRelationMatch); // Tylko pasujące TF będą aktywne
        }
    }

    // Utworzenie trajektorii ruchu
    public void addMovementTrajectory() {
        double X1 = 0, X2 = 0, X3 = 0, X4 = 0, Y1 = 0, Y2 = 0, Y3 = 0, Y4 = 0;

        for (MovementRelations mr : MovementRelations.movementRelations) {
            for (BorderLine bl : GeneratorController.borderLines) {
                for (StopLine sl : GeneratorController.stopLines) {
                    if (mr.getObjectA().getLocalization() == bl.getLocalization() && mr.getObjectA().getType() == bl.getType() &&
                            mr.getObjectA().getLocalization() == sl.getLocalization() && mr.getObjectA().getType() == sl.getType()) {
                        X1 = bl.getPositionCenterX();
                        Y1 = bl.getPositionCenterY();
                        X2 = sl.getPositionCenterX();
                        Y2 = sl.getPositionCenterY();
                    }
                    if (mr.getObjectB().getLocalization() == bl.getLocalization() && mr.getObjectB().getType() == bl.getType() &&
                            mr.getObjectB().getLocalization() == sl.getLocalization() && mr.getObjectB().getType() == sl.getType()) {
                        X3 = sl.getPositionCenterX();
                        Y3 = sl.getPositionCenterY();
                        X4 = bl.getPositionCenterX();
                        Y4 = bl.getPositionCenterY();
                    }
                }
            }

            List<Point2D> trajectoryPoints = List.of(
                    new Point2D(X1, Y1),
                    new Point2D(X2, Y2),
                    new Point2D(X3, Y3),
                    new Point2D(X4, Y4)
//                    new Point2D(700, 310),
//                    new Point2D(800, 310)
            );
            movTraj = new MovementTrajectory(trajectoryPoints);
            movementMap.put(mr, movTraj);   // Dodanie do mapy danej relacji i trajektorii ruchu
        }
    }

    // Załadowanie wprowadzonych liczb pojazdów na różnych relacjach
    public void loadVehicleNumbers() {
        for (Map.Entry<TextField, TextFieldVehicleNumber> entry : textfieldMap.entrySet()) {
            TextField tf = entry.getKey();
            TextFieldVehicleNumber tfVehNum = entry.getValue();
            tfVehNum.setCarsNumber(Double.parseDouble(tf.getText()));

            tfVehicleSum = textfieldMap.values()
                    .stream()
                    .mapToDouble(TextFieldVehicleNumber::getCarsNumber)
                    .sum();
        }
        addMovementTrajectory();
    }

    @FXML
    public void loadVehicleNumbersDedicatedButton() {
        loadVehicleNumbers();
        resetSimulation();
    }

    // Wyzerowanie wprowadzonych liczb pojazdów na różnych relacjach
    @FXML
    public void clearVehicleNumbers() {
        for (Map.Entry<TextField, TextFieldVehicleNumber> entry : textfieldMap.entrySet()) {
            TextField tf = entry.getKey();
            TextFieldVehicleNumber tfVehNum = entry.getValue();
            tf.setText("0");
            tfVehNum.setCarsNumber(0);
        }
    }

    // Uruchomienie symulacji
    @FXML
    public void startSimulation() {
        if (!MovementRelations.movementRelations.isEmpty()) {
            loadSimTimeLength();
            loadVehicleNumbers();
            if (!isSimulationActive) {
                isSimulationActive = true;
                simLoop.spawn(tfVNInput, movementMap);
            }
            if (checkSimParameters()) {
                simLoop.run();
                changeButtonsText();
            }
        }
    }

    // Zatrzymanie symulacji
    @FXML
    public void stopSimulation() {
        if (!MovementRelations.movementRelations.isEmpty()) {
            simLoop.stop();
        }
        changeButtonsText();
    }

    // Zresetowanie symulacji
    @FXML
    public void resetSimulation() {
        if (!MovementRelations.movementRelations.isEmpty()) {
            isSimulationActive = false;
            simLoop.reset();
        }
        setLabelTime("00:00:00");
        changeButtonsText();
    }

    // Zresetowanie parametrów czasu symulacji
    @FXML
    public void resetSimTime() {
        if (!isSimulationActive) {
            sliderTimeSpeed.setValue(1);
            simSpeed = 0;
            configureTimeSpinners();
            simTimeLength = 0;
        }
    }

    @FXML
    public void setLabelTime(String string) {
        labelTime.setText(string);
    }

    private void changeButtonsText() {
        if (!isSimulationActive) {
            buttonStart.setText("Uruchom symulację");
        } else {
            buttonStart.setText("Wznów symulację");
        }
    }

    // Sprawdzenie, czy wprowadzono wymagane parametry symulacji
    private boolean checkSimParameters() {
        System.out.println(tfVehicleSum);
        if (tfVehicleSum == 0) {
            AlertPopUp.showAlertPopUp("Bad VehicleNumber");
            resetSimulation();
            return false;
        } else if (simTimeLength == 0) {
            AlertPopUp.showAlertPopUp("Bad SimTime");
            resetSimulation();
            return false;
        }
        return true;
    }

}
