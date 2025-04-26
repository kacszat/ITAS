package com.itasoftware.itasoftware;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import java.io.IOException;

public class SettingsController {

    public static boolean isViewMaximized = false;

    // Powrót do głównego menu
    @FXML
    public void backToMainMenu() throws IOException  {
        MainApplication mainApp = new MainApplication();
        MainApplication.updateViewSize();
        mainApp.loadView("Main-view.fxml", mainApp.actualWidth, mainApp.actualHeight);
    }

    @FXML
    protected void onFullScreenButtonClick() {
        if (!MainApplication.primaryStage.isMaximized()) {
            isViewMaximized = true; // Jeśli nie jest zmaksymalizowany, ustaw na true
            MainApplication.primaryStage.setMaximized(true); // Maksymalizacja okna
        } else {
            isViewMaximized = false;
            MainApplication.primaryStage.setMaximized(false);
        }
    }

}
