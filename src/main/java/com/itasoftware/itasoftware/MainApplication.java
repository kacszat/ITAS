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
    static Stage primaryStage; // Przechowywanie Stage
    static double startWidth = 1280.0; // Startowa szerokość okna
    static double startHeight = 720.0; // Startowa wysokość okna
    static double actualWidth;  // Aktualna szerokość okna
    static double actualHeight; // Aktualna wysokość okna
    static double lastWidth;  // Ostatnia szerokość okna
    static double lastHeight; // Ostatnia wysokość okna

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;  // Zapamiętanie głównego Stage
        loadMainView();
    }

    // Ładowanie Main-view.fxml
    public void loadMainView() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("Main-view.fxml"));
        Pane root = fxmlLoader.load();

        // Skalowanie korzenia UI
        double scale = ViewScale.getScale();
        root.setScaleX(scale);
        root.setScaleY(scale);

        // Przeskalowanie font-u w całej scenie
        baseFontStyle = ViewScale.getFontStyle();
        root.setStyle(baseFontStyle);

        // Dopasowanie sceny do skalowanego UI
        Scene scene = new Scene(root, startWidth * scale, startHeight * scale);

//        // Dopasowanie sceny do skalowanego UI
//        Scene scene;
//        if (SettingsController.isViewMaximized) {
//            scene = new Scene(root); // bez rozmiaru
//            primaryStage.setScene(scene);
//            primaryStage.setMaximized(true);
//        } else {
//            scene = new Scene(root, startWidth * scale, startHeight * scale);
//            primaryStage.setScene(scene);
//            primaryStage.setMaximized(false);
//        }
//        System.out.println("Scene size: " + MainApplication.primaryStage.getScene().getWidth() + "x" + MainApplication.primaryStage.getScene().getHeight());
//        System.out.println("Maximized: " + MainApplication.primaryStage.isMaximized());

        primaryStage.setTitle("ITAS");
        primaryStage.setScene(scene);
        primaryStage.show();
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

    // Pobranie aktualnych rozmiarów okna (bez ramki okna systemowego Windows)
    public static void updateViewSize() {
        Scene currentScene = primaryStage.getScene();
        actualWidth = currentScene.getWidth();
        actualHeight = currentScene.getHeight();

        startWidth = actualWidth;
        startHeight = actualHeight;
    }

    public static void main(String[] args) {
        launch();
    }
}