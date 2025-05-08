package com.itasoftware.itasoftware;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

import java.util.List;

public class CanvasDrawer {

    double laneWidth = 25;      // Szerokość pasa ruchu
    double laneHeight = 400;    // Długość pasa ruchu
    GeneratorController genContrl = new GeneratorController();

    // Skalowanie Canvas
    void scaleCanvas(StackPane canvasContainer, Canvas canvas) {
        double baseWidth = 800;
        double baseHeight = 800;

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
    void drawCanvas(Canvas canvas) {
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Tło
        drawBackground(gc, canvas);

        // Pasy ruchu
        for (IntersectionLane lane : GeneratorController.intersectionLanes) {
            drawLanes(gc, lane, canvas);
        }

        // Rysowanie relacji ruchu
        drawMovementRelations(gc);

        // Rysowanie przycisków
        drawIntersectionLaneButton(gc);

        // Linie środkowa skrzyżowania
        //gc.setStroke(Color.RED);
        //gc.setLineWidth(2);
        //gc.strokeLine(genCanvas.getWidth()/2, 0, genCanvas.getWidth()/2, genCanvas.getHeight());
        //gc.strokeLine(0, genCanvas.getHeight()/2, genCanvas.getWidth(), genCanvas.getHeight()/2);
    }

    // Rysowanie simVanvas z pojazdami
    void drawCanvasWithVehicles(Canvas canvas, List<Vehicle> vehicles) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        drawBackground(gc, canvas);     // Tło
        for (IntersectionLane lane : GeneratorController.intersectionLanes) {   // Pasy ruchu
            drawLanes(gc, lane, canvas);
        }
        drawMovementRelations(gc);      // Rysowanie relacji ruchu
        drawIntersectionLaneButton(gc);     // Rysowanie przycisków

        // Rysowanie pojazdów
        drawVehicles(gc, vehicles);
    }

    private static void drawBackground(GraphicsContext gc, Canvas canvas) {
        gc.setFill(Color.rgb(71,71,71)); // Ten sam co #474747
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    // Rysowanie pasów ruchu skrzyżowania
    private void drawLanes(GraphicsContext gc, IntersectionLane lane, Canvas canvas) {
        double centerX = canvas.getWidth() / 2;
        double centerY = canvas.getHeight() / 2;

        double x = 0, y = 0, w = 0, h = 0;
        int offset = lane.getIndex();

        switch (lane.getLocalization()) {
            case NORTH -> {
                y = centerY - laneHeight;
                if (lane.getType() == IntersectionLane.Type.ENTRY)
                    x = centerX - (laneWidth * (offset+1));
                else
                    x = centerX + (laneWidth * offset);
                w = laneWidth;
                h = laneHeight;
            }
            case SOUTH -> {
                y = centerY;
                if (lane.getType() == IntersectionLane.Type.ENTRY)
                    x = centerX + (laneWidth * offset);
                else
                    x = centerX - (laneWidth * (offset+1));
                w = laneWidth;
                h = laneHeight;
            }
            case EAST -> {
                x = centerX;
                if (lane.getType() == IntersectionLane.Type.ENTRY)
                    y = centerY - (laneWidth * (offset+1));
                else
                    y = centerY + (laneWidth * offset);
                w = laneHeight;
                h = laneWidth;
            }
            case WEST -> {
                x = centerX - laneHeight;
                if (lane.getType() == IntersectionLane.Type.ENTRY)
                    y = centerY + (laneWidth * offset);
                else
                    y = centerY - (laneWidth * (offset+1));
                w = laneHeight;
                h = laneWidth;
            }
        }

        gc.setFill(Color.BLACK);
        gc.fillRect(x, y, w, h);

        double cutoff = intersectionCount(lane) * laneWidth;
        int stopLaneHeight = 2;
        int buttonSize = 15;
        double buttonBuffer = (laneWidth - buttonSize)/2;
        double position_zero = 0, position_max = (2*laneHeight-stopLaneHeight);

        // Rysowanie białych linii (z uwzględnieniem cutoff)
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(1);

        switch (lane.getLocalization()) {
            case NORTH -> {
                if (lane.getType() == IntersectionLane.Type.ENTRY) {
                    gc.strokeLine(x + w, y, x + w, centerY - cutoff);
                } else {
                    gc.strokeLine(x, y, x, centerY - cutoff);
                }
                drawStopLine(gc, x, (centerY - cutoff - stopLaneHeight), laneWidth, stopLaneHeight, lane);
                genContrl.addIntersectionLaneButton((x + buttonBuffer), (centerY - cutoff - stopLaneHeight - laneWidth), buttonSize, lane);
                genContrl.addBorderLine(x, (position_zero - Vehicle.getVehicleHeight()), laneWidth, stopLaneHeight, lane);
            }
            case SOUTH -> {
                if (lane.getType() == IntersectionLane.Type.ENTRY) {
                    gc.strokeLine(x, centerY + cutoff, x, y + h);
                } else {
                    gc.strokeLine(x + w, centerY + cutoff, x + w, y + h);
                }
                drawStopLine(gc, x, (centerY + cutoff), laneWidth, stopLaneHeight, lane);
                genContrl.addIntersectionLaneButton((x + buttonBuffer), (centerY + cutoff + laneWidth - buttonSize), buttonSize, lane);
                genContrl.addBorderLine(x, (position_max + Vehicle.getVehicleHeight()), laneWidth, stopLaneHeight, lane);
            }
            case EAST -> {
                if (lane.getType() == IntersectionLane.Type.ENTRY) {
                    gc.strokeLine(centerX + cutoff, y + h, x + w, y + h);
                } else {
                    gc.strokeLine(centerX + cutoff, y, x + w, y);
                }
                drawStopLine(gc, (centerX + cutoff), y, stopLaneHeight, laneWidth, lane);
                genContrl.addIntersectionLaneButton((centerX + cutoff + laneWidth - buttonSize), (y + buttonBuffer), buttonSize, lane);
                genContrl.addBorderLine((position_max + Vehicle.getVehicleHeight()), y, stopLaneHeight, laneWidth, lane);
            }
            case WEST -> {
                if (lane.getType() == IntersectionLane.Type.ENTRY) {
                    gc.strokeLine(centerX - cutoff, y, x - w, y);
                } else {
                    gc.strokeLine(centerX - cutoff, y + h, x - w, y + h);
                }
                drawStopLine(gc, (centerX - cutoff - stopLaneHeight), y, stopLaneHeight, laneWidth, lane);
                genContrl.addIntersectionLaneButton((centerX - cutoff - stopLaneHeight - laneWidth), (y + buttonBuffer), buttonSize, lane);
                genContrl.addBorderLine((position_zero - Vehicle.getVehicleHeight()), y, stopLaneHeight, laneWidth, lane);
            }
        }
    }

    // Obliczenie liczby przecięć dla skracania linii
    private int intersectionCount(IntersectionLane lane) {
        // Obliczenie liczby przecięć dla skracania linii
        int intersectionCount = 0;
        int intersectionCount_A;
        int intersectionCount_B;
        switch (lane.getLocalization()) {
            case NORTH -> {
                intersectionCount_A = (int) GeneratorController.intersectionLanes.stream()
                        .filter(l -> (l.getLocalization() == IntersectionLane.Localization.EAST && l.getType() == IntersectionLane.Type.ENTRY)).count();
                intersectionCount_B = (int) GeneratorController.intersectionLanes.stream()
                        .filter(l -> (l.getLocalization() == IntersectionLane.Localization.WEST && l.getType() == IntersectionLane.Type.EXIT)).count();
                intersectionCount = Math.max(intersectionCount_A, intersectionCount_B);
            }
            case SOUTH -> {
                intersectionCount_A = (int) GeneratorController.intersectionLanes.stream()
                        .filter(l -> (l.getLocalization() == IntersectionLane.Localization.EAST && l.getType() == IntersectionLane.Type.EXIT)).count();
                intersectionCount_B = (int) GeneratorController.intersectionLanes.stream()
                        .filter(l -> (l.getLocalization() == IntersectionLane.Localization.WEST && l.getType() == IntersectionLane.Type.ENTRY)).count();
                intersectionCount = Math.max(intersectionCount_A, intersectionCount_B);
            }
            case EAST-> {
                intersectionCount_A = (int) GeneratorController.intersectionLanes.stream()
                        .filter(l -> (l.getLocalization() == IntersectionLane.Localization.NORTH && l.getType() == IntersectionLane.Type.EXIT)).count();
                intersectionCount_B = (int) GeneratorController.intersectionLanes.stream()
                        .filter(l -> (l.getLocalization() == IntersectionLane.Localization.SOUTH && l.getType() == IntersectionLane.Type.ENTRY)).count();
                intersectionCount = Math.max(intersectionCount_A, intersectionCount_B);
            }
            case WEST -> {
                intersectionCount_A = (int) GeneratorController.intersectionLanes.stream()
                        .filter(l -> (l.getLocalization() == IntersectionLane.Localization.NORTH && l.getType() == IntersectionLane.Type.ENTRY)).count();
                intersectionCount_B = (int) GeneratorController.intersectionLanes.stream()
                        .filter(l -> (l.getLocalization() == IntersectionLane.Localization.SOUTH && l.getType() == IntersectionLane.Type.EXIT)).count();
                intersectionCount = Math.max(intersectionCount_A, intersectionCount_B);
            }
        }
        return intersectionCount;
    }

    // Funkcja rysująca linie stopu i dodająca obiekt do klasy StopLine
    private void drawStopLine(GraphicsContext gc, double x1, double y1, double x2, double y2, IntersectionLane lane) {

        if (lane.getType() == IntersectionLane.Type.ENTRY) {
            gc.setFill(Color.WHITE);
            gc.fillRect(x1, y1, x2, y2);
        }
//        else {
//            gc.setFill(Color.RED);
//            gc.fillRect(x1, y1, x2, y2);
//        }

        double centerX = x1+(x2/2);
        double centerY = y1+(y2/2);

        // Sprawdzenie, czy istnieje już linia stopu na danym pasie ruchu
        StopLine existingSL = GeneratorController.stopLines.stream()
                .filter(sl -> sl.getLocalization() == lane.getLocalization() &&
                        sl.getType() == lane.getType() &&
                        sl.getIndex() == lane.getIndex())
                .findFirst()
                .orElse(null);

        // Tworzenie nowej linii stopu w przypadku jej braku
        if (existingSL == null) {
            StopLine stopLine = new StopLine(lane.getLocalization(), lane.getType(), lane.getIndex(), centerX, centerY);
            GeneratorController.stopLines.add(stopLine);
        }
    }

    // Funkcja rysująca przycisk
    private void drawIntersectionLaneButton(GraphicsContext gc) {
        if (GeneratorController.isIntersectionLaneButtonShown) {
            for (IntersectionLaneButton existingButton : GeneratorController.intersectionLaneButtons) {
                Color buttonColor = null;

                if (existingButton.getType() == IntersectionLaneButton.Type.ENTRY && GeneratorController.activeExitButton == null) {
                    if (existingButton == GeneratorController.activeEntryButton) {
                        buttonColor = Color.LIME;
                    } else if (GeneratorController.activeEntryButton != null) {
                        buttonColor = Color.rgb(40, 40, 40, 1.0);
                    } else {
                        buttonColor = Color.rgb(200, 200, 0, 1.0);
                    }
                } else if (existingButton.getType() == IntersectionLaneButton.Type.EXIT && GeneratorController.activeExitButton == null) {
                    if (existingButton.isActive()) {
                        buttonColor = Color.GREEN;
                    } else if (GeneratorController.activeEntryButton != null) {
                        buttonColor = Color.RED;
                    } else {
                        buttonColor = Color.rgb(200, 200, 0, 1.0);
                    }
                }

                if (existingButton.getType() == IntersectionLaneButton.Type.EXIT && GeneratorController.activeEntryButton == null) {
                    if (existingButton == GeneratorController.activeExitButton) {
                        buttonColor = Color.LIME;
                    } else if (GeneratorController.activeExitButton != null) {
                        buttonColor = Color.rgb(40, 40, 40, 1.0);
                    } else {
                        buttonColor = Color.rgb(200, 200, 0, 1.0);
                    }
                } else if (existingButton.getType() == IntersectionLaneButton.Type.ENTRY && GeneratorController.activeEntryButton == null) {
                    if (existingButton.isActive()) {
                        buttonColor = Color.GREEN;
                    } else if (GeneratorController.activeExitButton != null) {
                        buttonColor = Color.RED;
                    } else {
                        buttonColor = Color.rgb(200, 200, 0, 1.0);
                    }
                }

                gc.setFill(buttonColor);
                gc.fillRect(existingButton.getX(), existingButton.getY(), existingButton.getSize(), existingButton.getSize());
            }
        }

    }

    // Funkcja rysująca relację ruchu
    private void drawMovementRelations(GraphicsContext gc) {
        if (GeneratorController.isMRNorthShown || GeneratorController.isMRSouthShown || GeneratorController.isMREastShown || GeneratorController.isMRWestShown) {
            double A_X = 0, A_Y = 0, B_X = 0, B_Y = 0;

            for (MovementRelations relation : genContrl.movementRelations.getMovementRelations()) {
                IntersectionLaneButton objectA = relation.getObjectA();
                IntersectionLaneButton objectB = relation.getObjectB();

                for (StopLine stopline : GeneratorController.stopLines) {
                    if (objectA.getLocalization().equals(stopline.getLocalization()) &&
                            objectA.getType().equals(stopline.getType()) &&
                            objectA.getIndex() == stopline.getIndex()) {
                        A_X = stopline.getPositionCenterX();
                        A_Y = stopline.getPositionCenterY();
                    }
                    if (objectB.getLocalization().equals(stopline.getLocalization()) &&
                            objectB.getType().equals(stopline.getType()) &&
                            objectB.getIndex() == stopline.getIndex()) {
                        B_X = stopline.getPositionCenterX();
                        B_Y = stopline.getPositionCenterY();
                    }
                }
                drawMovementRelationsLines(gc, A_X, A_Y, B_X, B_Y, objectA, objectB);

            }
        }
    }

    // Bezpośrednie rysowanie lini relacji
    private void drawMovementRelationsLines(GraphicsContext gc, double A_X, double A_Y, double B_X, double B_Y,
                                            IntersectionLaneButton btA, IntersectionLaneButton btB) {

        // Parametry linii relacji ruchu
        double control_X1 = 0, control_X2 = 0, control_Y1 = 0, control_Y2 = 0;
        int indexA = btA.getIndex() + 1, indexB = btB.getIndex() + 1;
        double control_Offset_Left = laneWidth * 1.5 * ((double) indexB / indexA);
        double control_Offset_Right = laneWidth * 0.5 * ((double) indexB / indexA);
        double control_Offset_Back = laneWidth * ((double) indexB / indexA);
        boolean drawCurve = false;
        //gc.setStroke(Color.rgb(50, 255, 50, 1.0));
        gc.setStroke(Color.RED);
        gc.setLineWidth(3);

        // Zdefiniowanie bazowych współrzędnych punktów kontrolnych
        control_X1 = A_X;
        control_Y1 = A_Y;
        control_X2 = B_X;
        control_Y2 = B_Y;

        // Wyznaczenie przebiegu linii w zależności od punktu początkowego i końcowego (modyfikacja punktów kontrolnych)
        if (btA.getLocalization() == IntersectionLaneButton.Localization.NORTH && GeneratorController.isMRNorthShown) { // Jazda z północy
            if (btB.getLocalization() == IntersectionLane.Localization.NORTH) { // Zawrócenie
                control_Y1 = A_Y + control_Offset_Back;
                control_Y2 = B_Y + control_Offset_Back;
                drawCurve = true;
                gc.strokeLine(B_X, B_Y, B_X,0);
            } else if (btB.getLocalization() == IntersectionLane.Localization.EAST) { // Skręt w lewo
                control_Y1 = A_Y + control_Offset_Left;
                control_X2 = B_X - control_Offset_Left;
                drawCurve = true;
                gc.strokeLine(B_X, B_Y, 2*laneHeight, B_Y);
            } else if (btB.getLocalization() == IntersectionLane.Localization.WEST) { // Skręt w prawo
                control_Y1 = A_Y + control_Offset_Right;
                control_X2 = B_X + control_Offset_Right;
                drawCurve = true;
                gc.strokeLine(B_X, B_Y, 0, B_Y);
            } else {    // Jeśli kierunek jazdy to na wprost, rysujemy prostą linię
                gc.strokeLine(A_X, A_Y, B_X, B_Y);
                gc.strokeLine(B_X, B_Y, B_X,2*laneHeight);
            }
            gc.strokeLine(A_X, A_Y, A_X,0);
        } else if (btA.getLocalization() == IntersectionLaneButton.Localization.SOUTH && GeneratorController.isMRSouthShown) { // Jazda z południa
            if (btB.getLocalization() == IntersectionLane.Localization.SOUTH) { // Zawrócenie
                control_Y1 = A_Y - control_Offset_Back;
                control_Y2 = B_Y - control_Offset_Back;
                drawCurve = true;
                gc.strokeLine(B_X, B_Y, B_X,2*laneHeight);
            } else if (btB.getLocalization() == IntersectionLane.Localization.WEST) { // Skręt w lewo
                control_Y1 = A_Y - control_Offset_Left;
                control_X2 = B_X + control_Offset_Left;
                drawCurve = true;
                gc.strokeLine(B_X, B_Y, 0, B_Y);
            } else if (btB.getLocalization() == IntersectionLane.Localization.EAST) { // Skręt w prawo
                control_Y1 = A_Y - control_Offset_Right;
                control_X2 = B_X - control_Offset_Right;
                drawCurve = true;
                gc.strokeLine(B_X, B_Y, 2*laneHeight, B_Y);
            } else {    // Jeśli kierunek jazdy to na wprost, rysujemy prostą linię
                gc.strokeLine(A_X, A_Y, B_X, B_Y);
                gc.strokeLine(B_X, B_Y, B_X,0);
            }
            gc.strokeLine(A_X, A_Y, A_X,2*laneHeight);
        } else if (btA.getLocalization() == IntersectionLaneButton.Localization.EAST && GeneratorController.isMREastShown) { // Jazda ze wschodu
            if (btB.getLocalization() == IntersectionLane.Localization.EAST) { // Zawrócenie
                control_X1 = A_X - control_Offset_Back;
                control_X2 = B_X - control_Offset_Back;
                drawCurve = true;
                gc.strokeLine(B_X, B_Y, 2*laneHeight,B_Y);
            } else if (btB.getLocalization() == IntersectionLane.Localization.SOUTH) { // Skręt w lewo
                control_X1 = A_X - control_Offset_Left;
                control_Y2 = B_Y - control_Offset_Left;
                drawCurve = true;
                gc.strokeLine(B_X, B_Y, B_X,2*laneHeight);
            } else if (btB.getLocalization() == IntersectionLane.Localization.NORTH) { // Skręt w prawo
                control_X1 = A_X - control_Offset_Right;
                control_Y2 = B_Y + control_Offset_Right;
                drawCurve = true;
                gc.strokeLine(B_X, B_Y, B_X,0);
            } else {    // Jeśli kierunek jazdy to na wprost, rysujemy prostą linię
                gc.strokeLine(A_X, A_Y, B_X, B_Y);
                gc.strokeLine(B_X, B_Y, 0,B_Y);
            }
            gc.strokeLine(A_X, A_Y, 2*laneHeight, A_Y);
        } else if (btA.getLocalization() == IntersectionLaneButton.Localization.WEST && GeneratorController.isMRWestShown) { // Jazda z zachodu
            if (btB.getLocalization() == IntersectionLane.Localization.WEST) { // Zawrócenie
                control_X1 = A_X + control_Offset_Back;
                control_X2 = B_X + control_Offset_Back;
                drawCurve = true;
                gc.strokeLine(B_X, B_Y, 0,B_Y);
            } else if (btB.getLocalization() == IntersectionLane.Localization.NORTH) { // Skręt w lewo
                control_X1 = A_X + control_Offset_Left;
                control_Y2 = B_Y + control_Offset_Left;
                drawCurve = true;
                gc.strokeLine(B_X, B_Y, B_X,0);
            } else if (btB.getLocalization() == IntersectionLane.Localization.SOUTH) { // Skręt w prawo
                control_X1 = A_X + control_Offset_Right;
                control_Y2 = B_Y - control_Offset_Right;
                drawCurve = true;
                gc.strokeLine(B_X, B_Y, B_X,2*laneHeight);
            } else {    // Jeśli kierunek jazdy to na wprost, rysujemy prostą linię
                gc.strokeLine(A_X, A_Y, B_X, B_Y);
                gc.strokeLine(B_X, B_Y, 2*laneHeight,B_Y);
            }
            gc.strokeLine(A_X, A_Y, 0, A_Y);
        }

        if (drawCurve) {
            gc.beginPath();
            gc.moveTo(A_X, A_Y); // Ustawienie początkowego punktu paraboli
            gc.bezierCurveTo(control_X1, control_Y1, control_X2, control_Y2, B_X, B_Y); // Ustawienie punktów kontrolnych i końcowego paraboli
            gc.stroke();
        }

    }

    // Rysowanie pojazdu
    private void drawVehicles(GraphicsContext gc, List<Vehicle> vehicles) {
        if (vehicles == null) {
            throw new IllegalArgumentException("Vehicle List nie może być null!");
        } else {
            for (Vehicle v : vehicles) {
                double dx = v.x2 - v.x1;
                double dy = v.y2 - v.y1;
                double length = Math.hypot(dx, dy);

                // Jednostkowy wektor normalny (prostopadły)
                double nx = -dy / length;
                double ny = dx / length;

                double halfWidth = v.vehicleWidth / 2.0;

                // Wierzchołki prostokąta (po dwa od każdego końca, przesunięte w górę i w dół względem osi pojazdu)
                double x1_left = v.x1 + nx * halfWidth;
                double y1_left = v.y1 + ny * halfWidth;
                double x1_right = v.x1 - nx * halfWidth;
                double y1_right = v.y1 - ny * halfWidth;

                double x2_left = v.x2 + nx * halfWidth;
                double y2_left = v.y2 + ny * halfWidth;
                double x2_right = v.x2 - nx * halfWidth;
                double y2_right = v.y2 - ny * halfWidth;

                // Rysowanie jako wielokąt (cztery punkty)
                gc.setFill(Color.RED);
                gc.fillPolygon(
                        new double[]{x1_left, x2_left, x2_right, x1_right},
                        new double[]{y1_left, y2_left, y2_right, y1_right},
                        4
                );
            }
        }
    }

}
