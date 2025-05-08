package com.itasoftware.itasoftware;

import javafx.geometry.Point2D;

import java.util.ArrayList;
import java.util.List;

public class MovementTrajectory {

    private final List<Point2D> points;
    private final List<Double> segmentLengths = new ArrayList<>();  // Lista długości segmentów trasy
    private final double totalLength;   // Całkowita długość trasy

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

    public double getTotalLength() {
        return totalLength;
    }

    public List<Point2D> getPoints() {
        return points;
    }


}
