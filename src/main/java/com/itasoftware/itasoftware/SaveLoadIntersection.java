package com.itasoftware.itasoftware;

import javafx.event.ActionEvent;
import javafx.scene.control.MenuItem;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SaveLoadIntersection {

    // Funkcja zapisująca skrzyżowanie
    public static void saveToFile(String filePath, List<String> lines, boolean append) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, append))) {
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Błąd podczas zapisu pliku: " + e.getMessage());
        }
    }

    public static void saveIntersectionLane(String filePath, List<IntersectionLane> IntLane) {
    List<String> lines = new ArrayList<>();
    for (IntersectionLane ila : IntLane) {
        lines.add("ila," + ila.getLocalization() + "," + ila.getType() + "," + ila.getIndex());
    }
    saveToFile(filePath, lines, false);     // nadpisz plik
    }

    public static void saveStopLine(String filePath, List<StopLine> StLine) {
        List<String> stoplines = new ArrayList<>();
        for (StopLine sl : StLine) {
            stoplines.add("sl," + sl.getLocalization() + "," + sl.getType() + "," + sl.getIndex() + "," + sl.getPositionCenterX() + "," + sl.getPositionCenterY());
        }
        saveToFile(filePath, stoplines, true);  // dopisz linie
    }

    public static void saveIntersectionLaneButton(String filePath, List<IntersectionLaneButton> IntLaneBt) {
        List<String> buttons = new ArrayList<>();
        for (IntersectionLaneButton ilb : IntLaneBt) {
            buttons.add("ilb," + ilb.getLocalization() + "," + ilb.getType() + "," + ilb.getIndex() + "," + ilb.getX() + "," + ilb.getY() + "," + ilb.getSize());
        }
        saveToFile(filePath, buttons, true);    // dopisz linie
    }

    public static void saveMovementRelations(String filePath, List<MovementRelations> relations) {
        List<String> movrel = new ArrayList<>();
        for (MovementRelations mr : relations) {
            movrel.add("mr," + mr.getObjectA().getInfo() + "," + mr.getObjectB().getInfo());
        }
        saveToFile(filePath, movrel, true);     // dopisz linie
    }

}
