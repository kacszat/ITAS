package com.itasoftware.itasoftware;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import java.io.IOException;

public class SettingsController {

    public static boolean isViewMaximized = false;

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

    @FXML
    protected void onFullScreenButtonClick() {
//        if (!MainApplication.primaryStage.isMaximized()) {
//            isViewMaximized = true; // Jeśli nie jest zmaksymalizowany, ustaw na true
//            MainApplication.primaryStage.setMaximized(true); // Maksymalizacja okna
//        } else {
//            isViewMaximized = false;
//            MainApplication.primaryStage.setMaximized(false);
//        }
//
//        Platform.runLater(() -> {
//            System.out.println("Scene size: " + MainApplication.primaryStage.getScene().getWidth() + "x" + MainApplication.primaryStage.getScene().getHeight());
//            System.out.println("Maximized: " + MainApplication.primaryStage.isMaximized());
//
//            MainApplication.updateViewSize(); // aktualizuj tylko po czasie
//        });
//
//        System.out.println("Scene size: " + MainApplication.primaryStage.getScene().getWidth() + "x" + MainApplication.primaryStage.getScene().getHeight());
//        System.out.println("Maximized: " + MainApplication.primaryStage.isMaximized());
    }

}
