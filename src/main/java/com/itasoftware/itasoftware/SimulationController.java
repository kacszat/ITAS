package com.itasoftware.itasoftware;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.control.TextFormatter;

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
    @FXML private TextField tfNorthLeft, tfNorthStraight, tfNorthRight, tfNorthBack, tfSouthLeft, tfSouthStraight, tfSouthRight, tfSouthBack,
                            tfWestLeft, tfWestStraight, tfWestRight, tfWestBack, tfEastLeft, tfEastStraight, tfEastRight, tfEastBack;

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
        updateTextFieldActivityAndDefaultValue();

        simLoop = new SimulationLoop(simCanvas, canvasDrawer, vehicleManager); // Tworzenie obiektu, gdy `simCanvas` nie jest już `null`
    }

    // Załadowanie Canvas lub informacji, w przypadku jego braku
    private void loadCanvasOrInfo(CanvasDrawer drawer) {
        if (!MovementRelations.movementRelations.isEmpty()) {
            // Reakcja na zmianę rozmiaru kontenera
            simCanvasContainer.layoutBoundsProperty().addListener((obs, oldVal, newVal) -> {
                drawer.scaleCanvas(simCanvasContainer, simCanvas);
            });

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

    // Załadowanie wprowadzonych liczb pojazdów na różnych relacjach
    @FXML
    public void loadCarsNumbers() {
        for (Map.Entry<TextField, TextFieldVehicleNumber> entry : textfieldMap.entrySet()) {
            TextField tf = entry.getKey();
            TextFieldVehicleNumber tfVehNum = entry.getValue();
            tfVehNum.setCarsNumber(Double.parseDouble(tf.getText()));
        }
    }

    // Wyzerowanie wprowadzonych liczb pojazdów na różnych relacjach
    @FXML
    public void clearCarsNumbers() {
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
        // Jeśli relacje są załadowane – start symulacji
        if (!MovementRelations.movementRelations.isEmpty()) {
            simLoop.run();
            simLoop.spawn();
            System.out.println("Button start");
        }
    }

    // Zatrzymanie symulacji
    @FXML
    public void stopSimulation() {
        // Jeśli relacje są załadowane – start symulacji
        if (!MovementRelations.movementRelations.isEmpty()) {
            simLoop.stop();
            System.out.println("Button stop");
        }
    }

}
