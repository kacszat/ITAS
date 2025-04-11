package com.itasoftware.itasoftware;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.stage.Stage;

import java.io.IOException;

public class SimulationController extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {

    }

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
