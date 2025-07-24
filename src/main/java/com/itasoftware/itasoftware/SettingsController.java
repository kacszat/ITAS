package com.itasoftware.itasoftware;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;

import java.io.IOException;
import java.util.Objects;

public class SettingsController {

    public static boolean isViewMaximized = false;
    // Zapis raportu z diagramem programu faz sygnalizacji świetlnej
    public static boolean saveReportWithPhaseDiagram = true, saveReportWithTextPhaseProgram = true;
    public static String speedSelected = "50", optionSelected = "Tekstowej i graficznej";
    public static double speedMultiplier = 1;
    @FXML ComboBox<String> speedComboBox, reportComboBox;
    @FXML Button fullScreenButton, saveSettingsButton;

    // Powrót do głównego menu
    @FXML
    public void backToMainMenu() throws IOException  {
        MainApplication mainApp = new MainApplication();
        MainApplication.updateViewSize();
        mainApp.loadView("Main-view.fxml", mainApp.actualWidth, mainApp.actualHeight);
    }

    // Przejście do okna symulacji
    @FXML
    public void goToGenerator() throws IOException  {
        MainApplication mainApp = new MainApplication();
        MainApplication.updateViewSize();
        mainApp.loadView("Generator-view.fxml", mainApp.actualWidth, mainApp.actualHeight);
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
    public void exitITAS() throws IOException  {
        Platform.exit();
    }

    @FXML
    public void initialize() {
        speedComboBox.setValue(speedSelected);
        reportComboBox.setValue(optionSelected);
        if (isViewMaximized) {
            fullScreenButton.setText("Zminimalizuj");
        } else {
            fullScreenButton.setText("Zmaksymalizuj");
        }
    }

    @FXML
    protected void onFullScreenButtonClick() {
        if (!MainApplication.primaryStage.isMaximized()) {
            isViewMaximized = true; // Jeśli nie jest zmaksymalizowany, ustaw na true
            MainApplication.primaryStage.setMaximized(true); // Maksymalizacja okna
            fullScreenButton.setText("Zminimalizuj");
        } else {
            isViewMaximized = false;
            MainApplication.primaryStage.setMaximized(false);
            fullScreenButton.setText("Zmaksymalizuj");
        }
    }

    @FXML
    private void loadSpeedValueFromComboBox() {
        speedSelected = speedComboBox.getSelectionModel().getSelectedItem();
        speedMultiplier = Double.parseDouble(speedSelected) / 50;
    }

    @FXML
    private void addDiagramToReportComboBox() {
        optionSelected = reportComboBox.getSelectionModel().getSelectedItem();
        if (Objects.equals(optionSelected, "Tekstowej")) {
            saveReportWithTextPhaseProgram = true;
            saveReportWithPhaseDiagram = false;
        } else if (Objects.equals(optionSelected, "Graficznej")) {
            saveReportWithTextPhaseProgram = false;
            saveReportWithPhaseDiagram = true;
        } else if (Objects.equals(optionSelected, "Tekstowej i graficznej")) {
            saveReportWithTextPhaseProgram = true;
            saveReportWithPhaseDiagram = true;
        }
    }

}
