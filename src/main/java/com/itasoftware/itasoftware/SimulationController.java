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

    private final Map<TextField, TextFieldCarsNumber> textfieldMap = new HashMap<>();
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
        GeneratorController genContrl = new GeneratorController();

        loadCanvasOrInfo(genContrl);

        loadTextField();

        updateTextFieldActivityAndDefaultValue();

    }

    // Załadowanie Canvas lub informacji, w przypadku jego braku
    private void loadCanvasOrInfo(GeneratorController genContrl) {
        if (!MovementRelations.movementRelations.isEmpty()) {
            // Reakcja na zmianę rozmiaru kontenera
            simCanvasContainer.layoutBoundsProperty().addListener((obs, oldVal, newVal) -> {
                genContrl.scaleCanvas(simCanvasContainer, simCanvas);
            });

            // Rysowanie
            genContrl.drawCanvas(simCanvas);

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
        textfieldMap.put(tfNorthLeft, new TextFieldCarsNumber(TextFieldCarsNumber.Localization.NORTH, TextFieldCarsNumber.Type.ENTRY, IntersectionLane.Localization.EAST));
        textfieldMap.put(tfNorthStraight, new TextFieldCarsNumber(TextFieldCarsNumber.Localization.NORTH, TextFieldCarsNumber.Type.ENTRY, TextFieldCarsNumber.Localization.SOUTH));
        textfieldMap.put(tfNorthRight, new TextFieldCarsNumber(TextFieldCarsNumber.Localization.NORTH, TextFieldCarsNumber.Type.ENTRY, TextFieldCarsNumber.Localization.WEST));
        textfieldMap.put(tfNorthBack, new TextFieldCarsNumber(TextFieldCarsNumber.Localization.NORTH, TextFieldCarsNumber.Type.ENTRY, TextFieldCarsNumber.Localization.NORTH));
        textfieldMap.put(tfSouthLeft, new TextFieldCarsNumber(TextFieldCarsNumber.Localization.SOUTH, TextFieldCarsNumber.Type.ENTRY, TextFieldCarsNumber.Localization.WEST));
        textfieldMap.put(tfSouthStraight, new TextFieldCarsNumber(TextFieldCarsNumber.Localization.SOUTH, TextFieldCarsNumber.Type.ENTRY, TextFieldCarsNumber.Localization.NORTH));
        textfieldMap.put(tfSouthRight, new TextFieldCarsNumber(TextFieldCarsNumber.Localization.SOUTH, TextFieldCarsNumber.Type.ENTRY, TextFieldCarsNumber.Localization.EAST));
        textfieldMap.put(tfSouthBack, new TextFieldCarsNumber(TextFieldCarsNumber.Localization.SOUTH, TextFieldCarsNumber.Type.ENTRY, TextFieldCarsNumber.Localization.SOUTH));
        textfieldMap.put(tfWestLeft, new TextFieldCarsNumber(TextFieldCarsNumber.Localization.WEST, TextFieldCarsNumber.Type.ENTRY, TextFieldCarsNumber.Localization.NORTH));
        textfieldMap.put(tfWestStraight, new TextFieldCarsNumber(TextFieldCarsNumber.Localization.WEST, TextFieldCarsNumber.Type.ENTRY, TextFieldCarsNumber.Localization.EAST));
        textfieldMap.put(tfWestRight, new TextFieldCarsNumber(TextFieldCarsNumber.Localization.WEST, TextFieldCarsNumber.Type.ENTRY, TextFieldCarsNumber.Localization.SOUTH));
        textfieldMap.put(tfWestBack, new TextFieldCarsNumber(TextFieldCarsNumber.Localization.WEST, TextFieldCarsNumber.Type.ENTRY, TextFieldCarsNumber.Localization.WEST));
        textfieldMap.put(tfEastLeft, new TextFieldCarsNumber(TextFieldCarsNumber.Localization.EAST, TextFieldCarsNumber.Type.ENTRY, TextFieldCarsNumber.Localization.SOUTH));
        textfieldMap.put(tfEastStraight, new TextFieldCarsNumber(TextFieldCarsNumber.Localization.EAST, TextFieldCarsNumber.Type.ENTRY, TextFieldCarsNumber.Localization.WEST));
        textfieldMap.put(tfEastRight, new TextFieldCarsNumber(TextFieldCarsNumber.Localization.EAST, TextFieldCarsNumber.Type.ENTRY, TextFieldCarsNumber.Localization.NORTH));
        textfieldMap.put(tfEastBack, new TextFieldCarsNumber(TextFieldCarsNumber.Localization.EAST, TextFieldCarsNumber.Type.ENTRY, TextFieldCarsNumber.Localization.EAST));
    }

    // Sprawdzenie stanu textfieldów
    public void updateTextFieldActivityAndDefaultValue() {
        // Filtr znaków (dopuszczalne tylko cyfry)
        UnaryOperator<TextFormatter.Change> digitFilter = change -> {
            String newText = change.getControlNewText();
            return (newText.matches("\\d*")) ? change : null;
        };

        for (Map.Entry<TextField, TextFieldCarsNumber> entry : textfieldMap.entrySet()) {
            TextField tf = entry.getKey();
            TextFieldCarsNumber tfCarNum = entry.getValue();

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
        for (Map.Entry<TextField, TextFieldCarsNumber> entry : textfieldMap.entrySet()) {
            TextField tf = entry.getKey();
            TextFieldCarsNumber tfCarNum = entry.getValue();
            tfCarNum.setCarsNumber(Double.parseDouble(tf.getText()));
        }
    }

    // Wyzerowanie wprowadzonych liczb pojazdów na różnych relacjach
    @FXML
    public void clearCarsNumbers() {
        for (Map.Entry<TextField, TextFieldCarsNumber> entry : textfieldMap.entrySet()) {
            TextField tf = entry.getKey();
            TextFieldCarsNumber tfCarNum = entry.getValue();
            tf.setText("0");
            tfCarNum.setCarsNumber(0);
        }
    }

}
