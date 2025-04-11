package com.itasoftware.itasoftware;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import java.io.IOException;

public class GeneratorApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {

    }

    public static void main(String[] args) {
        launch();
    }

    public void loadGeneratorView() throws IOException {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Generator-view.fxml"));
            Pane generatorRoot = fxmlLoader.load();  // Ładowanie nowej sceny

            // Przełączenie na nową scenę
            Scene newScene = new Scene(generatorRoot);
            MainApplication.primaryStage.setScene(newScene);  // Zmiana sceny na Generator-view
            MainApplication.primaryStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
