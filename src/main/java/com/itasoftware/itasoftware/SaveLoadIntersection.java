package com.itasoftware.itasoftware;

import javafx.event.ActionEvent;
import javafx.scene.control.MenuItem;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class SaveLoadIntersection {

    private GeneratorController genContrl;

    public SaveLoadIntersection(GeneratorController controller) {
        this.genContrl = controller;
    }

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

    // Zapisanie skrzyżowania
    public void saveIntersection(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Zapisz plik");

        // Filtr rozszerzeń
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Plik skrzyżowania ITAS (*.itaint)", "*.itaint")
        );

        // Okno zapisu
        Window window = ((MenuItem) event.getSource()).getParentPopup().getOwnerWindow();
        File file = fileChooser.showSaveDialog(window);

        if (file != null) {
            SaveLoadIntersection.saveIntersectionLane(file.getAbsolutePath(), GeneratorController.intersectionLanes);
            SaveLoadIntersection.saveStopLine(file.getAbsolutePath(), GeneratorController.stopLines);
            SaveLoadIntersection.saveIntersectionLaneButton(file.getAbsolutePath(), GeneratorController.intersectionLaneButtons);
            SaveLoadIntersection.saveMovementRelations(file.getAbsolutePath(), MovementRelations.movementRelations);
        }
    }

    public void loadIntersectionLane(String line) {
        if (line.startsWith("ila,")) {
            String[] tokens = line.split(",");
            if (tokens.length == 4) {
                IntersectionLane.Localization localization = IntersectionLane.Localization.valueOf(tokens[1]);
                IntersectionLane.Type type = IntersectionLane.Type.valueOf(tokens[2]);
                int index = Integer.parseInt(tokens[3]);
                GeneratorController.intersectionLanes.add(new IntersectionLane(localization, type, index));

                // Ustawienie sliderów na bazie danych z wczytanego skrzyżowania
                if (localization == IntersectionLane.Localization.NORTH && type == IntersectionLane.Type.ENTRY) {
                    genContrl.sliderNorthEntry.setValue(index + 1);
                } else if (localization == IntersectionLane.Localization.NORTH && type == IntersectionLane.Type.EXIT) {
                    genContrl.sliderNorthExit.setValue(index + 1);
                } else if (localization == IntersectionLane.Localization.SOUTH && type == IntersectionLane.Type.ENTRY) {
                    genContrl.sliderSouthEntry.setValue(index + 1);
                } else if (localization == IntersectionLane.Localization.SOUTH && type == IntersectionLane.Type.EXIT) {
                    genContrl.sliderSouthExit.setValue(index + 1);
                } else if (localization == IntersectionLane.Localization.EAST && type == IntersectionLane.Type.ENTRY) {
                    genContrl.sliderEastEntry.setValue(index + 1);
                } else if (localization == IntersectionLane.Localization.EAST && type == IntersectionLane.Type.EXIT) {
                    genContrl.sliderEastExit.setValue(index + 1);
                } else if (localization == IntersectionLane.Localization.WEST && type == IntersectionLane.Type.ENTRY) {
                    genContrl.sliderWestEntry.setValue(index + 1);
                } else if (localization == IntersectionLane.Localization.WEST && type == IntersectionLane.Type.EXIT) {
                    genContrl.sliderWestExit.setValue(index + 1);
                }
            }
        }
    }

    public static void loadStopLine(String line) {
        if (line.startsWith("sl,")) {
            String[] tokens = line.split(",");
            if (tokens.length == 6) {
                IntersectionLane.Localization localization = IntersectionLane.Localization.valueOf(tokens[1]);
                IntersectionLane.Type type = IntersectionLane.Type.valueOf(tokens[2]);
                int index = Integer.parseInt(tokens[3]);
                double x = Double.parseDouble(tokens[4]);
                double y = Double.parseDouble(tokens[5]);
                GeneratorController.stopLines.add(new StopLine(localization, type, index, x, y));
            }
        }
    }

    public static void loadIntersectionLaneButton(String line) {
        if (line.startsWith("ilb,")) {
            String[] tokens = line.split(",");
            if (tokens.length == 7) {
                IntersectionLane.Localization localization = IntersectionLane.Localization.valueOf(tokens[1]);
                IntersectionLane.Type type = IntersectionLane.Type.valueOf(tokens[2]);
                int index = Integer.parseInt(tokens[3]);
                double x = Double.parseDouble(tokens[4]);
                double y = Double.parseDouble(tokens[5]);
                double size = Double.parseDouble(tokens[6]);
                GeneratorController.intersectionLaneButtons.add(new IntersectionLaneButton(localization, type, index, x, y, size));
            }
        }
    }

    public static void loadMovementRelations(String line) {
        if (line.startsWith("mr,")) {
            String[] tokens = line.split(",");
            if (tokens.length == 13) {
                // Pierwszy przycisk
                IntersectionLane.Localization locA = IntersectionLane.Localization.valueOf(tokens[1]);
                IntersectionLane.Type typeA = IntersectionLane.Type.valueOf(tokens[2]);
                int indexA = Integer.parseInt(tokens[3]);
                double xA = Double.parseDouble(tokens[4]);
                double yA = Double.parseDouble(tokens[5]);
                double sizeA = Double.parseDouble(tokens[6]);

                // Drugi przycisk
                IntersectionLane.Localization locB = IntersectionLane.Localization.valueOf(tokens[7]);
                IntersectionLane.Type typeB = IntersectionLane.Type.valueOf(tokens[8]);
                int indexB = Integer.parseInt(tokens[9]);
                double xB = Double.parseDouble(tokens[10]);
                double yB = Double.parseDouble(tokens[11]);
                double sizeB = Double.parseDouble(tokens[12]);

                // Szukamy pasujących przycisków z listy
                IntersectionLaneButton a = findIntersectionLaneButton(GeneratorController.intersectionLaneButtons, locA, typeA, indexA, xA, yA, sizeA);
                IntersectionLaneButton b = findIntersectionLaneButton(GeneratorController.intersectionLaneButtons, locB, typeB, indexB, xB, yB, sizeB);

                if (a != null && b != null) {
                    MovementRelations.movementRelations.add(new MovementRelations(a, b));
                }
            }
        }
    }

    // Wczytanie skrzyżowania
    public void loadIntersection(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Wczytaj plik");

        // Filtr rozszerzeń
        FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("Pliki ITAINT (*.itaint)", "*.itaint");
        fileChooser.getExtensionFilters().add(filter);

        // Okno wyboru pliku
        Window window = ((MenuItem) event.getSource()).getParentPopup().getOwnerWindow();
        File file = fileChooser.showOpenDialog(window);

        if (file != null) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                GeneratorController.intersectionLanes.clear();
                GeneratorController.intersectionLaneButtons.clear();
                GeneratorController.stopLines.clear();
                MovementRelations.clearMovementRelations();

                String line;
                while ((line = reader.readLine()) != null) {
                    loadIntersectionLane(line);
                    loadStopLine(line);
                    loadIntersectionLaneButton(line);
                    loadMovementRelations(line);
                }

                genContrl.drawCanvas(genContrl.genCanvas);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Funkcja pomocnicza poszukująca danego intersectionLaneButton
    private static IntersectionLaneButton findIntersectionLaneButton(List<IntersectionLaneButton> buttons,
                                                                     IntersectionLane.Localization loc, IntersectionLane.Type type, int index, double x, double y, double size) {
        for (IntersectionLaneButton button : buttons) {
            if (button.getLocalization() == loc &&
                    button.getType() == type &&
                    button.getIndex() == index &&
                    button.getX() == x &&
                    button.getY() == y &&
                    button.getSize() == size) {
                return button;
            }
        }
        return null;
    }

}
