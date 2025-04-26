package com.itasoftware.itasoftware;

import javafx.application.Platform;
import javafx.fxml.FXML;

import java.io.IOException;

public class MainController {
    @FXML
    protected void onGeneratorButtonClick() throws IOException {
        loadNewView("Generator-view.fxml");
    }

    @FXML
    protected void onSimulationButtonClick() throws IOException {
        loadNewView("Simulation-view.fxml");
    }

    @FXML
    protected void onSettingsButtonClick() throws IOException {
        loadNewView("Settings-view.fxml");
    }

    @FXML
    protected void onExitButtonClick() {
        Platform.exit();
    }

    @FXML
    protected void loadNewView(String viewName) throws IOException {
        MainApplication mainApp = new MainApplication();
        MainApplication.updateViewSize();
        mainApp.loadView(viewName, mainApp.actualWidth, mainApp.actualHeight);
    }
}