package com.itasoftware.itasoftware;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;

import java.io.IOException;

public class SettingsController {

    public static boolean isViewMaximized = false;
    public static String speedSelected = "50";
    public static double speedMultiplier = 1;
    @FXML ComboBox<String> speedComboBox;
    @FXML Button fullScreenButton;

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

}
