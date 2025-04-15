package com.itasoftware.itasoftware;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GeneratorApplication {

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
