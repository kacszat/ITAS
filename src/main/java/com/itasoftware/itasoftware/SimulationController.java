package com.itasoftware.itasoftware;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import java.io.IOException;

public class SimulationController {

    // Powrót do głównego menu
    @FXML
    public void backToMainMenu() throws IOException  {
        MainApplication mainApp = new MainApplication();
        MainApplication.updateViewSize();
        mainApp.loadView("Main-view.fxml", mainApp.actualWidth, mainApp.actualHeight);
    }
}
