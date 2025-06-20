package com.itasoftware.itasoftware;

import java.util.ArrayList;
import java.util.List;

public class SinglePhaseButton {

    double rowNumber, colNumber, startX, startY;
    static double rectWidth = 10, rectHeight = 20, rectSpacing = 3;    // Parametry prostokąta
    boolean selected = false, activated = false;
    public static final List<SinglePhaseButton> singlePhaseButtons = new ArrayList<>();
    RowDescriptor desc;
    TrafficLight.Phase phase;

    public SinglePhaseButton(double rowNumber, double colNumber, double startX, double startY, RowDescriptor desc) {
        this.rowNumber = rowNumber;
        this.colNumber = colNumber;
        this.startX = startX;
        this.startY = startY;
        this.desc = desc;
        phase = TrafficLight.Phase.RED;
    }

    public static void addSinglePhaseButton(SinglePhaseButton spb) {
        if (!singlePhaseButtons.contains(spb)) {
            singlePhaseButtons.add(spb);
        }
    }

    public boolean contains(double clickX, double clickY) {
        return clickX >= startX && clickX <= (startX + rectWidth) &&
                clickY >= startY && clickY <= (startY + rectHeight);
    }

    public static SinglePhaseButton getSinglePhaseButton(double row, int column) {
        for (SinglePhaseButton spb : singlePhaseButtons) {
            if (spb.getRowNumber() == row && spb.getColNumber() == column) {
                return spb;
            }
        }
        return null;
    }

    public static void clear() {
        singlePhaseButtons.clear();
    }

    // Sprawdzenie aktywności danych przycisków
    public boolean isActivated() {
        switch (desc.getType()) {
            case LEFT -> {
                Boolean isActive = TrafficLightController.hasDedicatedLeftTurnLane.get(desc.getLocalization()); // Sprawdzenie w mapie, czy ten wlot ma lewoskręt
                activated = isActive != null && isActive;
            }
            case RIGHT -> {
                Boolean isActive = TrafficLightController.hasDedicatedRightTurnLane.get(desc.getLocalization()); // Sprawdzenie w mapie, czy ten wlot ma prawoskręt
                activated = isActive != null && isActive;
            }
            case MAIN -> {
                Boolean isActive = TrafficLightController.hasDedicatedMainLane.get(desc.getLocalization()); // Sprawdzenie w mapie, czy ten wlot prowadzi prosto
                activated = isActive != null && isActive;
            }
        }
        return activated;
    }

    public void changePhase(TrafficLight.Phase newPhase) {
        phase = newPhase;
    }

    public void select() {
        selected = (!selected);
    }

    public boolean isSelected() {
        return selected;
    }

    public double getColNumber() {
        return colNumber;
    }

    public double getRowNumber() {
        return rowNumber;
    }

    public static double getRectWidth() {
        return rectWidth;
    }

    public static double getRectHeight() {
        return rectHeight;
    }

    public static double getRectSpacing() {
        return rectSpacing;
    }

    public TrafficLight.Phase getPhase() {
        return phase;
    }
}
