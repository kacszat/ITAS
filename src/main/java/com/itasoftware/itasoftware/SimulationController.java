package com.itasoftware.itasoftware;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import java.io.IOException;

public class SimulationController {

    // Powrót do głównego menu
    @FXML
    public void backToMainMenu(ActionEvent event) {
        try {
            MainApplication mainApp = new MainApplication();
            mainApp.loadMainView(); // Wywołanie metody instancyjnej w MainApplication
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
