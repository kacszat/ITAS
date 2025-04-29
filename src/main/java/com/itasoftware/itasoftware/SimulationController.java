package com.itasoftware.itasoftware;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SimulationController {

    @FXML private Label simInfoLabel;
    @FXML private Canvas simCanvas;
    @FXML private StackPane simCanvasContainer;

    private final Map<TextField, TextFieldCarNumber> textfieldMap = new HashMap<>();
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

        loadTextField();
        updateTextFieldActivity();

    }

    // Załadowanie TextFieldów (sam textfield, potem obiekt klasy tfCarNumber z lokalizacją startową, typem lokalizacji startowej i lokalizacją końcową)
    private void loadTextField() {
        textfieldMap.put(tfNorthLeft, new TextFieldCarNumber(TextFieldCarNumber.Localization.NORTH, TextFieldCarNumber.Type.ENTRY, IntersectionLane.Localization.EAST));
        textfieldMap.put(tfNorthStraight, new TextFieldCarNumber(TextFieldCarNumber.Localization.NORTH, TextFieldCarNumber.Type.ENTRY, TextFieldCarNumber.Localization.SOUTH));
        textfieldMap.put(tfNorthRight, new TextFieldCarNumber(TextFieldCarNumber.Localization.NORTH, TextFieldCarNumber.Type.ENTRY, TextFieldCarNumber.Localization.WEST));
        textfieldMap.put(tfNorthBack, new TextFieldCarNumber(TextFieldCarNumber.Localization.NORTH, TextFieldCarNumber.Type.ENTRY, TextFieldCarNumber.Localization.NORTH));
        textfieldMap.put(tfSouthLeft, new TextFieldCarNumber(TextFieldCarNumber.Localization.SOUTH, TextFieldCarNumber.Type.ENTRY, TextFieldCarNumber.Localization.WEST));
        textfieldMap.put(tfSouthStraight, new TextFieldCarNumber(TextFieldCarNumber.Localization.SOUTH, TextFieldCarNumber.Type.ENTRY, TextFieldCarNumber.Localization.NORTH));
        textfieldMap.put(tfSouthRight, new TextFieldCarNumber(TextFieldCarNumber.Localization.SOUTH, TextFieldCarNumber.Type.ENTRY, TextFieldCarNumber.Localization.EAST));
        textfieldMap.put(tfSouthBack, new TextFieldCarNumber(TextFieldCarNumber.Localization.SOUTH, TextFieldCarNumber.Type.ENTRY, TextFieldCarNumber.Localization.SOUTH));
        textfieldMap.put(tfWestLeft, new TextFieldCarNumber(TextFieldCarNumber.Localization.WEST, TextFieldCarNumber.Type.ENTRY, TextFieldCarNumber.Localization.NORTH));
        textfieldMap.put(tfWestStraight, new TextFieldCarNumber(TextFieldCarNumber.Localization.WEST, TextFieldCarNumber.Type.ENTRY, TextFieldCarNumber.Localization.EAST));
        textfieldMap.put(tfWestRight, new TextFieldCarNumber(TextFieldCarNumber.Localization.WEST, TextFieldCarNumber.Type.ENTRY, TextFieldCarNumber.Localization.SOUTH));
        textfieldMap.put(tfWestBack, new TextFieldCarNumber(TextFieldCarNumber.Localization.WEST, TextFieldCarNumber.Type.ENTRY, TextFieldCarNumber.Localization.WEST));
        textfieldMap.put(tfEastLeft, new TextFieldCarNumber(TextFieldCarNumber.Localization.EAST, TextFieldCarNumber.Type.ENTRY, TextFieldCarNumber.Localization.SOUTH));
        textfieldMap.put(tfEastStraight, new TextFieldCarNumber(TextFieldCarNumber.Localization.EAST, TextFieldCarNumber.Type.ENTRY, TextFieldCarNumber.Localization.WEST));
        textfieldMap.put(tfEastRight, new TextFieldCarNumber(TextFieldCarNumber.Localization.EAST, TextFieldCarNumber.Type.ENTRY, TextFieldCarNumber.Localization.NORTH));
        textfieldMap.put(tfEastBack, new TextFieldCarNumber(TextFieldCarNumber.Localization.EAST, TextFieldCarNumber.Type.ENTRY, TextFieldCarNumber.Localization.EAST));
    }

    // Sprawdzenie stanu textfieldów
    public void updateTextFieldActivity() {
        for (Map.Entry<TextField, TextFieldCarNumber> entry : textfieldMap.entrySet()) {
            TextField tf = entry.getKey();
            TextFieldCarNumber tfCarNum = entry.getValue();

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
    
}
