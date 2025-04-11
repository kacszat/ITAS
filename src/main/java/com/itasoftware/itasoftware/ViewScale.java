package com.itasoftware.itasoftware;

public class ViewScale {
    static double baseWidth = 1920.0;
    static double baseHeight = 1080.0;
    static double scale = calculateScale();

    private static double calculateScale() {
        // Rzeczywista rozdzielczość ekranu
        double screenWidth = javafx.stage.Screen.getPrimary().getBounds().getWidth();
        double screenHeight = javafx.stage.Screen.getPrimary().getBounds().getHeight();

        // Skala (najmniejszy współczynnik z szerokości i wysokości)
        double scaleX = screenWidth / baseWidth;
        double scaleY = screenHeight / baseHeight;

        return Math.min(scaleX, scaleY); // Utrzymanie proporcji
    }

    public static double getScale() {
        return scale;
    }

    // Przeskalowanie font-u w całej scenie
    public static String getFontStyle() {
        return "-fx-font-size: " + (15 * scale) + "px;";
    }
}
