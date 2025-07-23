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
    static Point2D stopLine1, stopLine2;

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

        stopLine1 = trajectoryPoints.get(1);
        stopLine2 = trajectoryPoints.get(4);

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

    // Funkcja sprawdzająca, czy dane trajektorie ruchu się przecinają
    public static boolean doTrajectoriesIntersect(MovementTrajectory traj1, MovementTrajectory traj2) {
        // Pobranie listy punktów obu trajektorii dla dwóch różnych pojazdów
        List<Point2D> points1 = traj1.getPoints();
        List<Point2D> points2 = traj2.getPoints();

        for (int i = 0; i < points1.size() - 1; i++) {
            Point2D a1 = points1.get(i);
            Point2D a2 = points1.get(i + 1);

            for (int j = 0; j < points2.size() - 1; j++) {
                Point2D b1 = points2.get(j);
                Point2D b2 = points2.get(j + 1);

                if (segmentsIntersect(a1, a2, b1, b2)) {
                    return true;
                }
            }
        }
        return false;
    }

    // Sprawdzenie przecięcia dwóch odcinków różnych trajektorii
    private static boolean segmentsIntersect(Point2D p1, Point2D p2, Point2D q1, Point2D q2) {
        return counterClockwise(p1, q1, q2) != counterClockwise(p2, q1, q2)   // Sprawdza, czy punkty q1 i q2 leżą po różnych stronach odcinka p1-p2
                && counterClockwise(p1, p2, q1) != counterClockwise(p1, p2, q2);  // Sprawdza, czy punkty p1 i p2 leżą po różnych stronach odcinka q1-q2
    }

    // Orientacja trójkąta – pomocnicza funkcja dla przecięć (sprawdza, czy c znajduje się po lewej czy po prawej stronie odcinka a-b)
    private static boolean counterClockwise(Point2D a, Point2D b, Point2D c) {
        return (c.getY() - a.getY()) * (b.getX() - a.getX()) >
                (b.getY() - a.getY()) * (c.getX() - a.getX());
    }

    // Długości trasy do wskazanego punktu
    public double getDistanceToApproximatePoint(Point2D target) {
        double traveled = 0.0;
        double minDistance = Double.MAX_VALUE;
        double closestTraveled = 0.0;

        for (int i = 0; i < points.size() - 1; i++) {
            Point2D p1 = points.get(i);
            Point2D p2 = points.get(i + 1);
            double segmentLength = p1.distance(p2);

            // Oblicz punkt na odcinku najbliższy targetPoint
            double l2 = segmentLength * segmentLength;
            if (l2 == 0) continue;

            double t = ((target.getX() - p1.getX()) * (p2.getX() - p1.getX()) +
                    (target.getY() - p1.getY()) * (p2.getY() - p1.getY())) / l2;
            t = Math.max(0, Math.min(1, t));

            double projX = p1.getX() + t * (p2.getX() - p1.getX());
            double projY = p1.getY() + t * (p2.getY() - p1.getY());

            double dist = target.distance(projX, projY);

            if (dist < minDistance) {
                minDistance = dist;
                closestTraveled = traveled + segmentLength * t;
            }

            traveled += segmentLength;
        }

        return closestTraveled;
    }

    public double getTotalLength() {
        return totalLength;
    }

    public List<Point2D> getPoints() {
        return points;
    }

}
