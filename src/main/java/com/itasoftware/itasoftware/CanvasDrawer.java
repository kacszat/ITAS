package com.itasoftware.itasoftware;

import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;

import java.util.Arrays;
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

        // Rysowanie relacji ruchu i dodanie trajektorii
        drawRelationsAndAddTrajectory(gc);

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
        drawRelationsAndAddTrajectory(gc);      // Rysowanie relacji ruchu i dodanie trajektorii
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
        double marginDist = Vehicle.getVehicleHeight();

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
                drawTrafficLight(gc, x, (centerY - cutoff - laneWidth), laneWidth, laneWidth, lane);
                genContrl.addIntersectionLaneButton((x + buttonBuffer), (centerY - cutoff - stopLaneHeight - laneWidth), buttonSize, lane);
                genContrl.addBorderLine(x, (position_zero - marginDist), laneWidth, stopLaneHeight, lane);
            }
            case SOUTH -> {
                if (lane.getType() == IntersectionLane.Type.ENTRY) {
                    gc.strokeLine(x, centerY + cutoff, x, y + h);
                } else {
                    gc.strokeLine(x + w, centerY + cutoff, x + w, y + h);
                }
                drawStopLine(gc, x, (centerY + cutoff), laneWidth, stopLaneHeight, lane);
                drawTrafficLight(gc, x, (centerY + cutoff), laneWidth, laneWidth, lane);
                genContrl.addIntersectionLaneButton((x + buttonBuffer), (centerY + cutoff + laneWidth - buttonSize), buttonSize, lane);
                genContrl.addBorderLine(x, (position_max + marginDist), laneWidth, stopLaneHeight, lane);
            }
            case EAST -> {
                if (lane.getType() == IntersectionLane.Type.ENTRY) {
                    gc.strokeLine(centerX + cutoff, y + h, x + w, y + h);
                } else {
                    gc.strokeLine(centerX + cutoff, y, x + w, y);
                }
                drawStopLine(gc, (centerX + cutoff), y, stopLaneHeight, laneWidth, lane);
                drawTrafficLight(gc, (centerX + cutoff), y, laneWidth, laneWidth, lane);
                genContrl.addIntersectionLaneButton((centerX + cutoff + laneWidth - buttonSize), (y + buttonBuffer), buttonSize, lane);
                genContrl.addBorderLine((position_max + marginDist), y, stopLaneHeight, laneWidth, lane);
            }
            case WEST -> {
                if (lane.getType() == IntersectionLane.Type.ENTRY) {
                    gc.strokeLine(centerX - cutoff, y, x - w, y);
                } else {
                    gc.strokeLine(centerX - cutoff, y + h, x - w, y + h);
                }
                drawStopLine(gc, (centerX - cutoff - stopLaneHeight), y, stopLaneHeight, laneWidth, lane);
                drawTrafficLight(gc, (centerX - cutoff - laneWidth), y, laneWidth, laneWidth, lane);
                genContrl.addIntersectionLaneButton((centerX - cutoff - stopLaneHeight - laneWidth), (y + buttonBuffer), buttonSize, lane);
                genContrl.addBorderLine((position_zero - marginDist), y, stopLaneHeight, laneWidth, lane);
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
        } else {
            existingSL.setPositionCenterX(centerX);
            existingSL.setPositionCenterY(centerY);
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

    // Inicjalizacja parametrów punktów
    double X1 = 0, X2 = 0, X3 = 0, X4 = 0, X5 = 0, X6 = 0, Y1 = 0, Y2 = 0, Y3 = 0, Y4 = 0, Y5 = 0, Y6 = 0;
    boolean isCurve = true;

    // Utworzenie trajektorii ruchu i rysowanie relacji ruchu
    public void drawRelationsAndAddTrajectory(GraphicsContext gc) {

        // Przypisanie punktów początkowych i końcowych prostych (pasy ruchu od i do skrzyżowania)
        for (MovementRelations mr : genContrl.movementRelations.getMovementRelations()) {
            IntersectionLaneButton objA = mr.getObjectA();
            IntersectionLaneButton objB = mr.getObjectB();
            for (BorderLine bl : GeneratorController.borderLines) {
                for (StopLine sl : GeneratorController.stopLines) {
                    if (mr.getObjectA().getLocalization() == bl.getLocalization() && mr.getObjectA().getType() == bl.getType() && mr.getObjectA().getIndex() == bl.getIndex() &&
                            mr.getObjectA().getLocalization() == sl.getLocalization() && mr.getObjectA().getType() == sl.getType() && mr.getObjectA().getIndex() == sl.getIndex()) {
                        X1 = bl.getPositionCenterX();
                        Y1 = bl.getPositionCenterY();
                        X2 = sl.getPositionCenterX();
                        Y2 = sl.getPositionCenterY();
                    }
                    if (mr.getObjectB().getLocalization() == bl.getLocalization() && mr.getObjectB().getType() == bl.getType() && mr.getObjectB().getIndex() == bl.getIndex() &&
                            mr.getObjectB().getLocalization() == sl.getLocalization() && mr.getObjectB().getType() == sl.getType() && mr.getObjectB().getIndex() == sl.getIndex()) {
                        X5 = sl.getPositionCenterX();
                        Y5 = sl.getPositionCenterY();
                        X6 = bl.getPositionCenterX();
                        Y6 = bl.getPositionCenterY();
                    }
                }
            }

            // Obliczenie współrzędnych punktów krzywej Beziera
            calculateControlPoints(X2, Y2, X5, Y5, objA, objB);

            // Utworzenie trajektorii
            List<Point2D> trajectoryPoints = List.of(
                    new Point2D(X1, Y1),
                    new Point2D(X2, Y2),
                    new Point2D(X3, Y3),
                    new Point2D(X4, Y4),
                    new Point2D(X5, Y5),
                    new Point2D(X6, Y6)
            );
            MovementTrajectory.createTrajectory(mr, trajectoryPoints);

            // Pomijanie relacji nieaktywnej według flagi
            if ((objA.getLocalization() == IntersectionLaneButton.Localization.NORTH && !GeneratorController.isMRNorthShown) ||
                    (objA.getLocalization() == IntersectionLaneButton.Localization.SOUTH && !GeneratorController.isMRSouthShown) ||
                    (objA.getLocalization() == IntersectionLaneButton.Localization.EAST && !GeneratorController.isMREastShown) ||
                    (objA.getLocalization() == IntersectionLaneButton.Localization.WEST && !GeneratorController.isMRWestShown)) {
                continue;
            }

            gc.setStroke(Color.RED);
            gc.setLineWidth(3);
            // Rysowanie trajektorii ruchu
            if (isCurve) {    // Skręt lub zawrócenie
                drawCurve(gc, X2, Y2, X3, Y3, X4, Y4, X5, Y5);
            } else {    // Jazda na wprost
                gc.strokeLine(X2, Y2, X3, Y3);
                gc.strokeLine(X3, Y3, X4, Y4);
                gc.strokeLine(X4, Y4, X5, Y6);
            }
            // Rysowanie linii do i od skrzyżowania
            gc.strokeLine(X1, Y1, X2, Y2);
            gc.strokeLine(X5, Y5, X6, Y6);

        }
    }

    // Funkcja rysująca krzywą Beziera
    private void drawCurve(GraphicsContext gc, double X2, double Y2, double X3, double Y3, double X4, double Y4, double X5, double Y5) {
        gc.beginPath();
        gc.moveTo(X2, Y2); // Ustawienie początkowego punktu paraboli
        gc.bezierCurveTo(X3, Y3, X4, Y4, X5, Y5); // Ustawienie punktów kontrolnych i końcowego paraboli
        gc.stroke();
    }

    // Funkcja obliczająca współrzędne punktów krzywej Beziera
    private void calculateControlPoints(double X2, double Y2, double X5, double Y5, IntersectionLaneButton objA, IntersectionLaneButton objB) {

        // Różnica odległości pomiędzy punktami StopLine
        double dx = Math.abs(X2 - X5);
        double dy = Math.abs(Y2 - Y5);

        // Wyliczenie wartości offset-ów
        double offset_X = dx * 0.7;
        double offset_Y = dy * 0.7;
        double offset_Back = laneWidth;

        // Ustawienie bazowych wartości parametrów
        isCurve = true;
        X3 = X2;
        Y3 = Y2;
        X4 = X5;
        Y4 = Y5;

        // Wyznaczenie przebiegu linii w zależności od punktu początkowego i końcowego (modyfikacja punktów kontrolnych)
        if (objA.getLocalization() == IntersectionLaneButton.Localization.NORTH) { // Jazda z północy
            if (objB.getLocalization() == IntersectionLane.Localization.NORTH) { // Zawrócenie
                Y3 = Y2 + offset_Back;
                Y4 = Y5 + offset_Back;
            } else if (objB.getLocalization() == IntersectionLane.Localization.EAST) { // Skręt w lewo
                Y3 = Y2 + offset_Y;
                X4 = X5 - offset_X;
            } else if (objB.getLocalization() == IntersectionLane.Localization.WEST) { // Skręt w prawo
                Y3 = Y2 + offset_Y;
                X4 = X5 + offset_X;
            } else {
                isCurve = false;
            }
        } else if (objA.getLocalization() == IntersectionLaneButton.Localization.SOUTH) { // Jazda z południa
            if (objB.getLocalization() == IntersectionLane.Localization.SOUTH) { // Zawrócenie
                Y3 = Y2 - offset_Back;
                Y4 = Y5 - offset_Back;
            } else if (objB.getLocalization() == IntersectionLane.Localization.WEST) { // Skręt w lewo
                Y3 = Y2 - offset_Y;
                X4 = X5 + offset_X;
            } else if (objB.getLocalization() == IntersectionLane.Localization.EAST) { // Skręt w prawo
                Y3 = Y2 - offset_Y;
                X4 = X5 - offset_X;
            } else {
                isCurve = false;
            }
        } else if (objA.getLocalization() == IntersectionLaneButton.Localization.EAST) { // Jazda ze wschodu
            if (objB.getLocalization() == IntersectionLane.Localization.EAST) { // Zawrócenie
                X3 = X2 - offset_Back;
                X4 = X5 - offset_Back;
            } else if (objB.getLocalization() == IntersectionLane.Localization.SOUTH) { // Skręt w lewo
                X3 = X2 - offset_X;
                Y4 = Y5 - offset_Y;
            } else if (objB.getLocalization() == IntersectionLane.Localization.NORTH) { // Skręt w prawo
                X3 = X2 - offset_X;
                Y4 = Y5 + offset_Y;
            } else {
                isCurve = false;
            }
        } else if (objA.getLocalization() == IntersectionLaneButton.Localization.WEST) { // Jazda z zachodu
            if (objB.getLocalization() == IntersectionLane.Localization.WEST) { // Zawrócenie
                X3 = X2 + offset_Back;
                X4 = X5 + offset_Back;
            } else if (objB.getLocalization() == IntersectionLane.Localization.NORTH) { // Skręt w lewo
                X3 = X2 + offset_X;
                Y4 = Y5 + offset_Y;
            } else if (objB.getLocalization() == IntersectionLane.Localization.SOUTH) { // Skręt w prawo
                X3 = X2 + offset_X;
                Y4 = Y5 - offset_Y;
            } else {
                isCurve = false;
            }
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
                gc.setFill(Color.GRAY);
                gc.fillPolygon(
                        new double[]{x1_left, x2_left, x2_right, x1_right},
                        new double[]{y1_left, y2_left, y2_right, y1_right},
                        4
                );

                // Dodanie FOV-ów
                setFOV(gc, v);
            }
        }
    }

    // Ustawienie parametrów do rysowania FOV
    private void setFOV(GraphicsContext gc, Vehicle v) {
        double fovX, fovY, fovRadius, fovSmallRadius, fovStartAngle, fovLength;

        fovX = v.getFovX();
        fovY = v.getFovY();
        fovRadius = v.getFovRadius();
        fovSmallRadius = v.getFovSmallRadius();
        fovStartAngle = v.getFovStartAngle();
        fovLength = v.getFovLength();

        drawVehicleFOV(gc, Color.CYAN, fovX, fovY, fovRadius, fovStartAngle, fovLength);
        //drawVehicleFOV(gc, Color.BLUE, fovX, fovY, fovSmallRadius, fovStartAngle, fovLength);
        drawVehicleSquareFOV(gc, Color.BLUE, v, false);
        drawVehicleSquareFOV(gc, Color.PURPLE, v, true);
    }

    // Rysowanie FOV

    private void drawVehicleFOV(GraphicsContext gc, Color color, double fovX, double fovY, double fovRadius, double fovStartAngle, double fovLength) {
        if (SimulationController.isFOVshown) {
            gc.setStroke(color);
            gc.setLineWidth(2);
            gc.strokeArc(
                    fovX - fovRadius, fovY - fovRadius,
                    fovRadius * 2, fovRadius * 2,
                    fovStartAngle, fovLength,
                    ArcType.CHORD
            );
        }
    }

    private void drawVehicleSquareFOV(GraphicsContext gc, Color color, Vehicle v, Boolean smallFOV) {
        if (SimulationController.isFOVshown) {
            Point2D[] FOV = !smallFOV ? v.squareFOVCorners : v.squareSmallFOVCorners;
            if (FOV == null) return;

            double[] xPoints = Arrays.stream(FOV).mapToDouble(Point2D::getX).toArray();
            double[] yPoints = Arrays.stream(FOV).mapToDouble(Point2D::getY).toArray();

            gc.setStroke(color);
            gc.setLineWidth(2);
            gc.strokePolygon(xPoints, yPoints, 4);
        }
    }

    // Funkcja rysująca daną fazę sygnalizacji świetlnej
    public void drawTrafficLight(GraphicsContext gc, double x1, double y1, double x2, double y2, IntersectionLane lane) {
        if (SimulationController.areTLshown) {
            for (TrafficLight tl : TrafficLight.trafficLights) {
                if (tl.getLocalization() == lane.getLocalization() && tl.getType() == lane.getType() && tl.getIndex() == lane.getIndex()) {
                    switch (tl.getCurrentPhase()) {
                        case TrafficLight.Phase.GREEN -> gc.setFill(Color.LIME);
                        case TrafficLight.Phase.YELLOW -> gc.setFill(Color.YELLOW);
                        case TrafficLight.Phase.RED -> gc.setFill(Color.RED);
                        case TrafficLight.Phase.RED_YELLOW -> gc.setFill(Color.DARKORANGE);
                        case TrafficLight.Phase.GREEN_ARROW -> gc.setFill(Color.DARKGREEN);
                    }
                    gc.fillRect(x1, y1, x2, y2);
                }
            }
        }
    }

}
