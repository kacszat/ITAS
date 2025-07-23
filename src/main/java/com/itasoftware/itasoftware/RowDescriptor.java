package com.itasoftware.itasoftware;

import java.util.ArrayList;
import java.util.List;

public class RowDescriptor {

    private final IntersectionLane.Localization loc;
    private final TrafficLight.LaneType type;
    public int row;

    public static final List<RowDescriptor> rowDescriptors = new ArrayList<>();

    // Klasa pomocnicza, określająca typ lokalizację, do jakich odnosi się dany rząd przycisków faz
    public RowDescriptor(IntersectionLane.Localization loc, TrafficLight.LaneType type, int row) {
        this.loc = loc;
        this.type = type;
        this.row = row;
    }

    public static RowDescriptor getRowDescriptor(int rowIndex) {
        IntersectionLane.Localization[] locs = {
                IntersectionLane.Localization.NORTH,
                IntersectionLane.Localization.SOUTH,
                IntersectionLane.Localization.WEST,
                IntersectionLane.Localization.EAST
        };

            TrafficLight.LaneType[] types = {
                TrafficLight.LaneType.MAIN,
                TrafficLight.LaneType.LEFT,
                TrafficLight.LaneType.RIGHT
        };

        int locIndex = rowIndex / 3;
        int typeIndex = rowIndex % 3;

        // Pomijanie duplikatu
        IntersectionLane.Localization loc = locs[locIndex];
        TrafficLight.LaneType type = types[typeIndex];
        for (RowDescriptor existing : rowDescriptors) {
            if (existing.getLocalization() == loc && existing.getType() == type) {
                return existing;
            }
        }

        RowDescriptor rd = new RowDescriptor(locs[locIndex], types[typeIndex], rowIndex);
        rowDescriptors.add(rd);
        return rd;
    }

    public TrafficLight.LaneType getType() {
        return type;
    }

    public IntersectionLane.Localization getLocalization() {
        return loc;
    }

    public int getRow() {
        return row;
    }

    // Pobranie numeru wiersza dla zadanych właściwości
    public static int getRowNumber(IntersectionLane.Localization loc, TrafficLight.LaneType type) {
        for (RowDescriptor rd : RowDescriptor.rowDescriptors) {
            if (rd.getLocalization() == loc && rd.getType() == type) {
                return rd.getRow();
            }
        }
        throw new IllegalArgumentException("Brak wiersza dla lokalizacji " + loc + " i typu " + type);
    }

}
