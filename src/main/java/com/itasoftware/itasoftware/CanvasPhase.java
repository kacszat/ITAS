package com.itasoftware.itasoftware;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class CanvasPhase {

    private final double space = 35;
    public static int rectNumber = 50;   // Domyślna liczba prostokątów
    double rectWidth = SinglePhaseButton.getRectWidth(), rectHeight = SinglePhaseButton.getRectHeight(), rectSpacing = SinglePhaseButton.getRectSpacing();

    // Skalowanie Canvas
    void scalePhaseCanvas(StackPane canvasContainer, Canvas canvas) {
        double baseWidth = 1350;
        double baseHeight = 850;

        double availableWidth = canvasContainer.getWidth();
        double availableHeight = canvasContainer.getHeight();

        if (availableWidth == 0 || availableHeight == 0) return;

        double scaleX = availableWidth / baseWidth;
        double scaleY = availableHeight / baseHeight;

        double scale = Math.min(scaleX, scaleY); // zachowujemy proporcje

        canvas.setScaleX(scale);
        canvas.setScaleY(scale);
    }

    // Rysowanie Canvas
    void drawPhaseCanvas(Canvas canvas) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        drawBackground(gc, canvas);
        drawText(gc);
        drawRectangles(gc);
    }

    // Rysowanie tła
    private static void drawBackground(GraphicsContext gc, Canvas canvas) {
        gc.setFill(Color.rgb(71,71,71)); // Ten sam co #474747
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    // Rysowanie tekstu
    private void drawText(GraphicsContext gc) {
        gc.setFont(new Font("Arial", 16));
        gc.setFill(Color.WHITE);

        gc.fillText("Wlot północy - Pasy główne:", 0, space);
        gc.fillText("Wlot północy - Lewoskręty:", 0, 3 * space);
        gc.fillText("Wlot północy - Prawoskręty:", 0, 5 * space);

        gc.fillText("Wlot południowy - Pasy główne:", 0, 7 * space);
        gc.fillText("Wlot południowy - Lewoskręty:", 0, 9 * space);
        gc.fillText("Wlot południowy - Prawoskręty:", 0, 11 * space);

        gc.fillText("Wlot zachodni - Pasy główne:", 0, 13 * space);
        gc.fillText("Wlot zachodni - Lewoskręty:", 0, 15 * space);
        gc.fillText("Wlot zachodni - Prawoskręty:", 0, 17 * space);

        gc.fillText("Wlot wschodni - Pasy główne:", 0, 19 * space);
        gc.fillText("Wlot wschodni - Lewoskręty:", 0, 21 * space);
        gc.fillText("Wlot wschodni - Prawoskręty:", 0, 23 * space);
    }

    // Funkcja do rysowania prostokątów
    private void drawRectangles(GraphicsContext gc) {
        // Przesunięcie w pionie między tekstem a prostokątami
        double textRectSpace = 10;

        // Pozycje prostokątów dla każdego z 12 wierszy
        for (int i = 0; i < 12; i++) {
            double y = (2 * i + 1) * space + textRectSpace;
            drawRectangleRow(gc, 0, y, rectNumber, rectWidth, rectHeight, rectSpacing, i);
        }
    }

    // Funkcja pomocnicza do rysowania prostokątów
    private void drawRectangleRow(GraphicsContext gc, double startX, double startY, int count, double width, double height, double spacing, int row) {
        double x = startX;

        for (int i = 0; i < count; i++) {
            // Sprawdzenie, czy istnieje już taki przycisk
            SinglePhaseButton spb = SinglePhaseButton.getSinglePhaseButton(row, i);

            RowDescriptor desc = RowDescriptor.getRowDescriptor(row);

            // Utworzenie przycisku, jeśli nie istnieje
            if (spb == null) {
                spb = new SinglePhaseButton(row, i, x, startY, desc);
                SinglePhaseButton.addSinglePhaseButton(spb);
            }

            takeButtonColor(gc, spb);
            gc.fillRect(x, startY, width, height);
            x += width;

            // Jeśli następny prostokąt to co 10-ty, to zwiększona zostaje przerwa
            if ((i + 1) % 10 == 0) {
                x += 6; // większa przerwa
            } else {
                x += spacing; // standardowa przerwa
            }
        }
    }

    // Pobranie koloru prostokąta
    private void takeButtonColor(GraphicsContext gc, SinglePhaseButton spb) {
        if (!spb.isActivated()) {
            gc.setFill(Color.rgb(50,50,50));
        } else {
            switch (spb.getPhase()) {
                case RED -> gc.setFill(Color.rgb(200,50,50));
                case YELLOW -> gc.setFill(Color.rgb(200,200,50));
                case GREEN -> gc.setFill(Color.rgb(0,200,0));
                case RED_YELLOW -> gc.setFill(Color.rgb(200,100,50));
                case GREEN_ARROW -> gc.setFill(Color.rgb(0,75,0));
            }
        }
    }

}
