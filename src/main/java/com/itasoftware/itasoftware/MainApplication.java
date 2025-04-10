package com.itasoftware.itasoftware;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.List;

public class MainApplication extends Application {

    // Lista obrazów tła
    private static final List<String> BACKGROUND_PHOTOS = List.of(
            "/com/itasoftware/itasoftware/photos/background-photo-v1.jpg",
            "/com/itasoftware/itasoftware/photos/background-photo-v2.jpg",
            "/com/itasoftware/itasoftware/photos/background-photo-v3.jpg",
            "/com/itasoftware/itasoftware/photos/background-photo-v4.jpg",
            "/com/itasoftware/itasoftware/photos/background-photo-v5.jpg",
            "/com/itasoftware/itasoftware/photos/background-photo-v6.jpg",
            "/com/itasoftware/itasoftware/photos/background-photo-v7.jpg",
            "/com/itasoftware/itasoftware/photos/background-photo-v8.jpg"
    );

    private int currentPhotoIndex = 0;
    private String baseFontStyle;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("Main-view.fxml"));
        Pane root = fxmlLoader.load();

        double baseWidth = 1920.0;
        double baseHeight = 1080.0;
        double startWidth = 1280.0;
        double startHeight = 720.0;

        // Rzeczywista rozdzielczość ekranu
        double screenWidth = javafx.stage.Screen.getPrimary().getBounds().getWidth();
        double screenHeight = javafx.stage.Screen.getPrimary().getBounds().getHeight();

        // Skala (najmniejszy współczynnik z szerokości i wysokości)
        double scaleX = screenWidth / baseWidth;
        double scaleY = screenHeight / baseHeight;
        double scale = Math.min(scaleX, scaleY); // zachowaj proporcje

        // Skalowanie korzenia UI
        root.setScaleX(scale);
        root.setScaleY(scale);

        // Przeskalowanie font-u w całej scenie
        baseFontStyle = "-fx-font-size: " + (15 * scale) + "px;";
        root.setStyle(baseFontStyle);

        // Dopasowanie sceny do skalowanego UI
        Scene scene = new Scene(root, startWidth * scale, startHeight * scale);

        stage.setTitle("ITAS");
        stage.setScene(scene);
        stage.show();
        startBackgroundRotation(root);
    }

    // Zmiana obrazu tła co 10 sekund
    private void startBackgroundRotation(Pane root) {
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(10), event -> {
            currentPhotoIndex = (currentPhotoIndex + 1) % BACKGROUND_PHOTOS.size();
            URL imageUrl = getClass().getResource(BACKGROUND_PHOTOS.get(currentPhotoIndex));
            if (imageUrl != null) {
                String backgroundStyle = "-fx-background-image: url('" + imageUrl.toExternalForm() + "');" +
                        "-fx-background-size: cover; -fx-background-position: top;";
                root.setStyle(backgroundStyle + baseFontStyle);
            } else {
                System.err.println("Nie znaleziono obrazu: " + BACKGROUND_PHOTOS.get(currentPhotoIndex));
            }
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    public static void main(String[] args) {
        launch();
    }
}