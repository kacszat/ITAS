package com.itasoftware.itasoftware;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class MainController {
    @FXML
    private Label welcomeText;

    @FXML
    protected void onGeneratorButtonClick() {
        welcomeText.setText("Generator");
    }

    @FXML
    protected void onSimulationButtonClick() {
        welcomeText.setText("Symulacja");
    }

    @FXML
    protected void onSettingsButtonClick() {
        welcomeText.setText("Ustawienia");
    }
}