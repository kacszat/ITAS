package com.itasoftware.itasoftware;

import java.util.ArrayList;
import java.util.List;

public class MovementRelations {

    private double objectA_X, objectA_Y, objectB_X, objectB_Y;
    private IntersectionLaneButton objectA;
    private IntersectionLaneButton objectB;
    public static final List<MovementRelations> movementRelations = new ArrayList<>();

    public MovementRelations(IntersectionLaneButton objectA, IntersectionLaneButton objectB) {
        this.objectA = objectA;
        this.objectB = objectB;
    }

    public IntersectionLaneButton getObjectA() {
        return objectA;
    }

    public IntersectionLaneButton getObjectB() {
        return objectB;
    }

    // Funkcja dodająca lub usuwająca daną relację
    public void addMovementRelation(IntersectionLaneButton pointA, IntersectionLaneButton pointB) {
        MovementRelations relation = new MovementRelations(pointA, pointB);
        if (!movementRelations.contains(relation)) {
            movementRelations.add(relation);
        } else {
            movementRelations.remove(relation);
        }
    }

    public List<MovementRelations> getMovementRelations() {
        return movementRelations;
    }

    public static void clearMovementRelations() {
        movementRelations.clear();
    }

    private void setObjectA_X(double x) {
        this.objectA_X = x;
    }

    private void setObjectA_Y(double y) {
        this.objectA_Y = y;
    }

    private void setObjectB_X(double x) {
        this.objectB_X = x;
    }

    private void setObjectB_Y(double y) {
        this.objectB_Y = y;
    }

    private double getObjectA_X() {
        return objectA_X;
    }

    private double getObjectA_Y() {
        return objectA_Y;
    }

    private double getObjectB_X() {
        return objectB_X;
    }

    private double getObjectB_Y() {
        return objectB_Y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MovementRelations that = (MovementRelations) o;

        return (objectA.equals(that.objectA) && objectB.equals(that.objectB)) ||
                (objectA.equals(that.objectB) && objectB.equals(that.objectA));
    }

    @Override
    public int hashCode() {
        return objectA.hashCode() + objectB.hashCode(); // symetryczne
    }

}
