package com.itasoftware.itasoftware;

import javafx.geometry.Point2D;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MovementTrajectory {

    private final List<Point2D> points;
    private final List<Double> segmentLengths = new ArrayList<>();  // Lista długości segmentów trasy
    private final double totalLength;   // Całkowita długość trasy
    public static Map<MovementRelations, MovementTrajectory> movementMap = new HashMap<>();   // Hash mapa z powiązanymi relacjami i trajektoriami ruchu

    // Zmiana wartości przebytej całkowitej drogi
    public MovementTrajectory(List<Point2D> points) {
        this.points = points;

        double total = 0;
        for (int i = 0; i < points.size() - 1; i++) {
            double length = points.get(i).distance(points.get(i + 1));
            segmentLengths.add(length);
            total += length;
        }

        this.totalLength = total;
    }

    // Funkcja określająca daną pozycję
    public Point2D getPosition(double distance) {
        if (distance <= 0) return points.get(0);
        if (distance >= totalLength) return points.get(points.size() - 1);

        double traveled = 0;    // Dotychczas pokonana trasa

        // Iteracja przez długość segmentów
        for (int i = 0; i < segmentLengths.size(); i++) {
            double segLen = segmentLengths.get(i);

            if (distance <= traveled + segLen) {    // Jeśli spełniony warunek, to ten segment zawiera szukany punkt
                double ratio = (distance - traveled) / segLen;  // Procent odległości przebytej w ramach segmentu
                Point2D p1 = points.get(i);     // Początek segmentu
                Point2D p2 = points.get(i + 1);     // Koniec segmentu

                double x = p1.getX() + (p2.getX() - p1.getX()) * ratio;
                double y = p1.getY() + (p2.getY() - p1.getY()) * ratio;

                return new Point2D(x, y);   // Punkt z współrzędnymi interpolowanymi liniowo
            }

            traveled += segLen;
        }

        return points.get(points.size() - 1); // Awaryjnie zwracany ostatni punkt
    }

    // Funkcja tworząca trajektorię ruchu
    public static void createTrajectory(MovementRelations mr, List<Point2D> trajectoryPoints) {
        Point2D p1 = trajectoryPoints.get(0);
        Point2D p2 = trajectoryPoints.get(1);
        Point2D p3 = trajectoryPoints.get(2);
        Point2D p4 = trajectoryPoints.get(3);
        Point2D p5 = trajectoryPoints.get(4);
        Point2D p6 = trajectoryPoints.get(5);

        // Generowanie odcinka do skrzyżowania
        List<Point2D> fullTraj = new ArrayList<>();
        fullTraj.add(p1);

        // Krzywa Béziera
        List<Point2D> bezier = generateBezierPoints(p2, p3, p4, p5, 50);
        fullTraj.addAll(bezier);

        // Odcinek od skrzyżowania
        fullTraj.add(p6);

        // Utwórz trajektorię
        MovementTrajectory MovTraj = new MovementTrajectory(fullTraj);
        MovementTrajectory.movementMap.put(mr, MovTraj);   // Dodanie do mapy danej relacji i trajektorii ruchu
    }

    // Funkcja generująca dokładną trajektorię po krzywej Beziera
    public static List<Point2D> generateBezierPoints(Point2D p0, Point2D p1, Point2D p2, Point2D p3, int numSteps) {
        List<Point2D> bezierPoints = new ArrayList<>();

        for (int i = 0; i <= numSteps; i++) {
            double t = i / (double) numSteps;

            double x = Math.pow(1 - t, 3) * p0.getX()
                    + 3 * Math.pow(1 - t, 2) * t * p1.getX()
                    + 3 * (1 - t) * Math.pow(t, 2) * p2.getX()
                    + Math.pow(t, 3) * p3.getX();

            double y = Math.pow(1 - t, 3) * p0.getY()
                    + 3 * Math.pow(1 - t, 2) * t * p1.getY()
                    + 3 * (1 - t) * Math.pow(t, 2) * p2.getY()
                    + Math.pow(t, 3) * p3.getY();

            bezierPoints.add(new Point2D(x, y));
        }

        return bezierPoints;
    }

    public double getTotalLength() {
        return totalLength;
    }

    public List<Point2D> getPoints() {
        return points;
    }

}
