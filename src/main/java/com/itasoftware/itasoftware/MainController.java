package com.itasoftware.itasoftware;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;

import java.io.IOException;

public class MainController {
    @FXML
    private Label welcomeText;

    @FXML
    private Pane rootPane;  // Korzystamy z Pane, by zamienić zawartość

    @FXML
    protected void onGeneratorButtonClick() {
        loadNewView("Generator-view.fxml");
    }

    @FXML
    protected void onSimulationButtonClick() {
        loadNewView("Simulation-view.fxml");
    }

    @FXML
    protected void onSettingsButtonClick() {
        loadNewView("Settings-view.fxml");
    }

    @FXML
    protected void onExitButtonClick() {
        Platform.exit();
    }

    @FXML
    protected void loadNewView(String viewName) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(viewName));
            Pane newRoot = fxmlLoader.load();  // Ładowanie nowej sceny

            // Ustawienie ciemnego tła
            newRoot.getStyleClass().clear();
            newRoot.getStyleClass().add("root-gray");

            // Zastosowanie skalowania
            double scale = ViewScale.getScale();
            newRoot.setScaleX(scale);
            newRoot.setScaleY(scale);

            // Ustawienie fontu dla całej sceny
            newRoot.setStyle(ViewScale.getFontStyle());

            // Pobranie aktualnego rozmiaru
            MainApplication.updateViewSize();

//            // Przełączenie na nową scenę
//            Scene newScene;
//            if (SettingsController.isViewMaximized) {
//                // Nie ustawiamy rozmiarów sceny - system sam to zrobi
//                newScene = new Scene(newRoot);
//                MainApplication.primaryStage.setScene(newScene);
//                MainApplication.primaryStage.setMaximized(true);
//            } else {
//                MainApplication.updateViewSize(); // Aktualizacja rozmiarów
//                newScene = new Scene(newRoot, MainApplication.actualWidth, MainApplication.actualHeight);
//                MainApplication.primaryStage.setScene(newScene);
//                MainApplication.primaryStage.setMaximized(false);
//            }
//            System.out.println("Scene size: " + MainApplication.primaryStage.getScene().getWidth() + "x" + MainApplication.primaryStage.getScene().getHeight());
//            System.out.println("Maximized: " + MainApplication.primaryStage.isMaximized());
//            MainApplication.primaryStage.show();

            // Przełączenie na nową scenę
            Scene newScene = new Scene(newRoot, MainApplication.actualWidth, MainApplication.actualHeight);
            MainApplication.primaryStage.setScene(newScene);  // Zmiana sceny na nową
            MainApplication.primaryStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}