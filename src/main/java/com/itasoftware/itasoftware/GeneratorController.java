package com.itasoftware.itasoftware;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;
import javafx.event.ActionEvent;

import java.io.IOException;


public class GeneratorController extends Application {

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
