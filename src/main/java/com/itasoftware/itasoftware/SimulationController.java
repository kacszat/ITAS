package com.itasoftware.itasoftware;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.StackPane;
import javafx.util.StringConverter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SimulationController {

    @FXML private Label simInfoLabel;
    @FXML Canvas simCanvas;
    @FXML private StackPane simCanvasContainer;

    // Utworzenie nowych instancji klas CanvasDrawer, SimulationLoop, VehicleManager i SaveLoadSimulation itd.
    CanvasDrawer canvasDrawer = new CanvasDrawer();
    SimulationLoop simLoop;
    VehicleManager vehicleManager = new VehicleManager();
    SaveLoadSimulation SLS = new SaveLoadSimulation(this);
    SaveReport SR = new SaveReport();

    static final Map<TextField, TextFieldVehicleNumber> textfieldMap = new HashMap<>();
    List<TextFieldVehicleNumber> tfVehNumInputs = new ArrayList<>();    // Lista liczby pojazdów z danych textfieldów
    @FXML private TextField tfNorthLeft, tfNorthStraight, tfNorthRight, tfNorthBack, tfSouthLeft, tfSouthStraight, tfSouthRight, tfSouthBack,
                            tfWestLeft, tfWestStraight, tfWestRight, tfWestBack, tfEastLeft, tfEastStraight, tfEastRight, tfEastBack;
    @FXML Slider sliderTimeSpeed;
    @FXML Spinner<Integer> spinnerTimeHours, spinnerTimeMinutes;
    @FXML Label labelTime;
    @FXML Button buttonStart, buttonTurnOnTL, buttonReport, buttonQuickSim;
    @FXML MenuItem menuitemShowMR, menuitemShowFOV, menuitemShowTL;
    static double simSpeed = 1.0, tfVehicleSum = 0;
    static Integer simTimeLength = 0;
    boolean isSimulationActive = false;
    static boolean areTrafficLightsActive = false;
    static boolean isFOVshown = false, areMRshown = false, areTLshown = false;
    static boolean isBackFromTLView = false, idLoadedFromSaveFile = false;
    static boolean tooManyVehiclesOnLane = false;
    private Map<String, Double> vehiclesPerRelation = new HashMap<>();  // Mapa przechowująca liczbę pojazdów dla danej relacji (np. SOUTH->NORTH)
    private Map<String, Integer> lanesPerRelation = new HashMap<>();    // Mapa przechowująca ilość pasów obsługujących daną relację

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

    @FXML
    public void goToTrafficLightController() throws IOException {
        MainApplication mainApp = new MainApplication();
        MainApplication.updateViewSize();
        mainApp.loadView("TrafficLight-view.fxml", mainApp.actualWidth, mainApp.actualHeight);
        isBackFromTLView = true;
        SLS.saveToTempFile();
    }

    // Przejście do ustawień
    @FXML
    public void exitITAS() throws IOException  {
        if (AlertPopUp.showAlertPopUp("Save")) {
            Platform.exit();
        }
    }


    // Załadowanie domyślnej symulacji
    @FXML
    public void defaultSimulation() {
        if (AlertPopUp.showAlertPopUp("New Simulation")) {
            resetSimulation();
            resetSimTime();
            clearVehicleNumbers();
            //GeneratorController.clearIntersection();
        }
    }

    // Zapisanie symulacji
    @FXML
    public void saveSimulation(ActionEvent event) {
        if (MovementRelations.movementRelations.isEmpty()) {
            AlertPopUp.showAlertPopUp("Can't Save Sim");
        } else {
            SLS.saveSimulation(event);
        }
    }

    // Załadowanie symulacji
    @FXML
    public void loadSimulation(ActionEvent event) {
        if (!MovementRelations.movementRelations.isEmpty()) {
            if (AlertPopUp.showAlertPopUp("Load")) {
                SLS.loadSimulation(event);
                resetSimulation();
                idLoadedFromSaveFile = true;
            }
        } else {
            SLS.loadSimulation(event);
            resetSimulation();
            idLoadedFromSaveFile = true;
        }
    }

    // Pokazanie relacji
    @FXML
    public void showMovementRelations() {
        if (!areMRshown) {
            GeneratorController.isMRNorthShown = true;
            GeneratorController.isMRSouthShown = true;
            GeneratorController.isMREastShown = true;
            GeneratorController.isMRWestShown = true;
            areMRshown = true;
        } else {
            GeneratorController.isMRNorthShown = false;
            GeneratorController.isMRSouthShown = false;
            GeneratorController.isMREastShown = false;
            GeneratorController.isMRWestShown = false;
            areMRshown = false;
        }
        simLoop.update();
        updateMenuItemText();
    }

    // Pokazanie FOV-ów pojazdów
    @FXML
    public void showVehiclesFOVs() {
        isFOVshown = (!isFOVshown);
        updateMenuItemText();
    }

    // Pokazanie sygnalizacji świetlnej
    @FXML
    public void showTrafficLights() {
        areTLshown = (!areTLshown);
        simLoop.update();
        updateMenuItemText();
    }

    private void updateMenuItemText() {
        if (areMRshown) {
            menuitemShowMR.setText("Ukryj relacje ruchu");
        } else {
            menuitemShowMR.setText("Pokaż relacje ruchu");
        }
        if (isFOVshown) {
            menuitemShowFOV.setText("Ukryj pola widzenia pojazdów");
        } else {
            menuitemShowFOV.setText("Pokaż pola widzenia pojazdów");
        }
        if (areTLshown) {
            menuitemShowTL.setText("Ukryj sygnalizację świetlną");
        } else {
            menuitemShowTL.setText("Pokaż sygnalizację świetlną");
        }
    }

    @FXML
    public void initialize() {
        loadCanvasOrInfo(canvasDrawer);
        loadTextField();
        configureTimeSpeedSlider();
        configureTimeSpinners();
        updateTextFieldActivityAndDefaultValue();

        simSpeed = 1.0;
        tfVehicleSum = 0;
        simTimeLength = 0;

        // Utworzenie pętli symulacji
        simLoop = new SimulationLoop(simCanvas, canvasDrawer, vehicleManager, simSpeed, this);
        resetSimulation();

        if (isBackFromTLView) {
            SLS.restoreFromTempFile();
            isBackFromTLView = false;
            areTrafficLightsActive = false;
            turnOnTrafficLights();  // Obecnie widoczność TL jest False, to po wywołaniu funkcji będzie True
        }
    }

    // Załadowanie Canvas lub informacji, w przypadku jego braku
    public void loadCanvasOrInfo(CanvasDrawer drawer) {
        if (!MovementRelations.movementRelations.isEmpty()) {
            // Reakcja na zmianę rozmiaru kontenera
            simCanvasContainer.layoutBoundsProperty().addListener((obs, oldVal, newVal) -> {
                drawer.scaleCanvas(simCanvasContainer, simCanvas);
            });

            // Ukrycie przycisków wyznaczania relacji i samych relacji
            GeneratorController.isIntersectionLaneButtonShown = false;
            GeneratorController.isMRNorthShown = false;
            GeneratorController.isMRSouthShown = false;
            GeneratorController.isMREastShown = false;
            GeneratorController.isMRWestShown = false;

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
        List<Integer> sliderAllowedValues = List.of(1,2,3,4,5,10,15,20);    // Dozwolone prędkości symulacji

        sliderTimeSpeed.setMin(0);
        sliderTimeSpeed.setMax(sliderAllowedValues.size() - 1);
        sliderTimeSpeed.setValue(0);
        sliderTimeSpeed.setBlockIncrement(1);
        sliderTimeSpeed.setMajorTickUnit(1);
        sliderTimeSpeed.setMinorTickCount(0);
        sliderTimeSpeed.setSnapToTicks(true);
        sliderTimeSpeed.setShowTickMarks(true);
        sliderTimeSpeed.setShowTickLabels(true);

        // Pokazywanie etykiet zgodnie z sliderAllowedValues
        sliderTimeSpeed.setLabelFormatter(new StringConverter<Double>() {
            @Override
            public String toString(Double dbl) {    // Służy do konwersji liczby z suwaka (Double) na tekstową etykietę
                return sliderAllowedValues.get(dbl.intValue()).toString();
            }
            @Override
            public Double fromString(String str) {  // Służy do konwersji tekstu etykiety na wartość liczbową suwaka
                return (double) sliderAllowedValues.indexOf(Integer.valueOf(str));
            }
        });

        // Pobranie aktualnie ustawionej wartości na sliderze
        sliderTimeSpeed.valueProperty().addListener((obs, oldVal, newVal) -> {
            simSpeed = sliderAllowedValues.get(newVal.intValue());
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

    // Załadowanie TextFieldów (sam textfield, potem obiekt klasy tfVehNumber z lokalizacją startową, typem lokalizacji startowej i lokalizacją końcową)
    private void loadTextField() {
        textfieldMap.clear();
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
        tfVehNumInputs = new ArrayList<>(textfieldMap.values());
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
            TextFieldVehicleNumber tfVehNum = entry.getValue();

            tf.setTextFormatter(new TextFormatter<>(digitFilter)); // Dodanie filtra do TF
            tf.setText("0"); // Ustawienie domyślnej wartości
            tfVehNum.setVehiclesNumber(0); // Ustawienie liczby samochodów równej zero na każdym tfCarNum

            boolean textFieldAndRelationMatch = false;

            for (MovementRelations relation : MovementRelations.movementRelations) {
                if (relation.getObjectA().getLocalization().equals(tfVehNum.getLocalization()) &&
                        relation.getObjectA().getType().equals(tfVehNum.getType()) &&
                        relation.getObjectB().getLocalization().equals(tfVehNum.getDestination())) {

                    textFieldAndRelationMatch = true;
                    break;
                }
            }

            tf.setDisable(!textFieldAndRelationMatch); // Tylko pasujące TF będą aktywne
        }
    }

    // Załadowanie wprowadzonych liczb pojazdów na różnych relacjach
    public void loadVehicleNumbers() {
        for (Map.Entry<TextField, TextFieldVehicleNumber> entry : textfieldMap.entrySet()) {
            TextField tf = entry.getKey();
            TextFieldVehicleNumber tfVehNum = entry.getValue();
            double vehiclesNumbers = Double.parseDouble(tf.getText());
            tfVehNum.setVehiclesNumber(vehiclesNumbers);

            tfVehicleSum = textfieldMap.values()
                    .stream()
                    .mapToDouble(TextFieldVehicleNumber::getVehiclesNumber)
                    .sum();
        }
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
            tfVehNum.setVehiclesNumber(0);
        }
        clearCheckVehiclesLists();
    }

    // Uruchomienie symulacji
    @FXML
    public void startSimulation() {
        if (!MovementRelations.movementRelations.isEmpty() && !SimulationLoop.isSimFinished) {
            loadSimTimeLength();
            loadVehicleNumbers();
            checkVehicleNumbers();
            if (!isSimulationActive) {
                isSimulationActive = true;
                simLoop.createSpawnSchedule();
                VehicleManager.addVehiclesToSpawn();
            }
            if (checkSimParameters()) {
                simLoop.run();
                changeButtonsText();
            }
            DataCollector.clearReportContent();
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
            sliderTimeSpeed.setValue(0);
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

    // Sprawdzenie, czy liczba pojazdów nie jest zbyt duża
    private void checkVehicleNumbers() {
        // Zliczenie pojazdów z TextFieldów
        for (TextFieldVehicleNumber tfVN : tfVehNumInputs) {
            String key = tfVN.getLocalization() + "->" + tfVN.getDestination();
            vehiclesPerRelation.put(key,
                    vehiclesPerRelation.getOrDefault(key, 0.0) + tfVN.getVehiclesNumber());
        }

        // Zliczenie relacji (czyli "pasów") obsługujących daną trasę
        for (MovementRelations mr : MovementRelations.movementRelations) {
            if (mr.getObjectA().getType() == TextFieldVehicleNumber.Type.ENTRY) {
                String key = mr.getObjectA().getLocalization() + "->" + mr.getObjectB().getLocalization();
                lanesPerRelation.put(key,
                        lanesPerRelation.getOrDefault(key, 0) + 1);
            }
        }

        // Sprawdzenie czy liczba pojazdów nie przekracza dozwolonej wartości
        tooManyVehiclesOnLane = false;

        for (Map.Entry<String, Double> entry : vehiclesPerRelation.entrySet()) {
            String relation = entry.getKey();
            double vehicles = entry.getValue();
            int lanes = lanesPerRelation.getOrDefault(relation, 0);

            if (lanes == 0) continue; // brak dostępnych pasów - nie sprawdzamy

            if ((vehicles / (simTimeLength * 60)) / lanes > 1) { // True, jeśli na jeden pas na jedną sekundę przypada więcej niż 1 pojazd
                tooManyVehiclesOnLane = true;
                System.out.println("Za dużo pojazdów dla relacji: " + relation +
                        " (pojazdy: " + vehicles + ", pasy: " + lanes + ")");
            }
        }
    }

    private void clearCheckVehiclesLists() {
        vehiclesPerRelation.clear();
        lanesPerRelation.clear();
    }

    // Sprawdzenie, czy wprowadzono wymagane parametry symulacji
    private boolean checkSimParameters() {
        if (tfVehicleSum == 0) {
            AlertPopUp.showAlertPopUp("Bad VehicleNumber");
            resetSimulation();
            return false;
        } else if (simTimeLength == 0) {
            AlertPopUp.showAlertPopUp("Bad SimTime");
            resetSimulation();
            return false;
        } else if (tooManyVehiclesOnLane) {
            AlertPopUp.showAlertPopUp("Bad Data");
            resetSimulation();
            return false;
        }
        return true;
    }

    @FXML
    public void turnOnTrafficLights() {
        if (TrafficLight.trafficLights.isEmpty()) {
            AlertPopUp.showAlertPopUp("Can't Show TL");
        } else {
            if (areTrafficLightsActive) {
                buttonTurnOnTL.setText("Włącz sygnalizację świetlną");
                areTrafficLightsActive = false;
                areTLshown = false;
            } else {
                buttonTurnOnTL.setText("Wyłącz sygnalizację świetlną");
                areTrafficLightsActive = true;
                areTLshown = true;
            }
        }
        simLoop.update();
        updateMenuItemText();
    }

    // Generacja raportu
    @FXML
    public void showReport(ActionEvent event){
       DataCollector.generateData();    // Funkcja agregująca dane
//       ChartCreator chartCreator = new ChartCreator();
//       List<ChartCreator.ChartImage> charts = chartCreator.createChartsAsImages();
       List<ChartCreator.ChartImage> charts = null;
       SR.saveReport(event, charts);
    }

}
