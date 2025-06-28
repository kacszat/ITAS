package com.itasoftware.itasoftware;

import javafx.event.ActionEvent;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SaveLoadSimulation {

    private SimulationController simContrl;

    public SaveLoadSimulation(SimulationController simControl) {
        this.simContrl = simControl;
    }

    public static void saveTextFieldVehicleNumber(String filePath, List<TextFieldVehicleNumber> tfVehNumList) {
        List<String> lines = new ArrayList<>();
        for (TextFieldVehicleNumber tfVehNum : tfVehNumList) {
            lines.add("tfvn," + tfVehNum.getLocalization() + "," + tfVehNum.getDestination() + "," + tfVehNum.getVehiclesNumber());
        }
        SaveLoadIntersection.saveToFile(filePath, lines, true);  // dopisz linie
    }

    public static void saveTimeSettings(String filePath) {
        List<String> lines = new ArrayList<>();
        lines.add("simtime," + SimulationController.simTimeLength);
        lines.add("simspeed," + SimulationController.simSpeed);
        SaveLoadIntersection.saveToFile(filePath, lines, true);  // dopisz linie
    }

    // Zapisanie skrzyżowania
    public void saveSimulation(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Zapisz plik");

        // Filtr rozszerzeń
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Plik symulacji ITAS (*.itasim)", "*.itasim")
        );

        // Okno zapisu
        Window window = ((MenuItem) event.getSource()).getParentPopup().getOwnerWindow();
        File file = fileChooser.showSaveDialog(window);

        if (file != null) {
            SaveLoadIntersection.saveIntersectionLane(file.getAbsolutePath(), GeneratorController.intersectionLanes);
            SaveLoadIntersection.saveStopLine(file.getAbsolutePath(), GeneratorController.stopLines);
            SaveLoadIntersection.saveIntersectionLaneButton(file.getAbsolutePath(), GeneratorController.intersectionLaneButtons);
            SaveLoadIntersection.saveMovementRelations(file.getAbsolutePath(), MovementRelations.movementRelations);
            saveTextFieldVehicleNumber(file.getAbsolutePath(), simContrl.tfVehNumInputs);
            saveTimeSettings(file.getAbsolutePath());
            SaveLoadTrafficLight.saveTrafficLightSettings(file.getAbsolutePath(), true);
            SaveLoadTrafficLight.saveSinglePhaseButtonState(file.getAbsolutePath());
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
            }
        }
    }

    public void loadTextFieldVehicleNumber(String line) {
        if (line.startsWith("tfvn,")) {
            String[] tokens = line.split(",");
            if (tokens.length == 4) {
                IntersectionLane.Localization localization = IntersectionLane.Localization.valueOf(tokens[1]);
                IntersectionLane.Localization destination = IntersectionLane.Localization.valueOf(tokens[2]);
                double vehiclesNumber = Double.parseDouble(tokens[3]);

                for (Map.Entry<TextField, TextFieldVehicleNumber> entry : simContrl.textfieldMap.entrySet()) {
                    TextField tf = entry.getKey();
                    TextFieldVehicleNumber tfVehNum = entry.getValue();

                    boolean textFieldAndRelationMatch = false;
                    for (MovementRelations relation : MovementRelations.movementRelations) {
                        if (relation.getObjectA().getLocalization().equals(tfVehNum.getLocalization()) &&
                                relation.getObjectA().getType().equals(tfVehNum.getType()) &&
                                relation.getObjectB().getLocalization().equals(tfVehNum.getDestination())) {

                            textFieldAndRelationMatch = true;
                            break;
                        }
                    }
                    tf.setDisable(!textFieldAndRelationMatch);

                    if (tfVehNum.getLocalization() == localization && tfVehNum.getType() == IntersectionLane.Type.ENTRY && tfVehNum.getDestination() == destination) {
                        tf.setText(String.valueOf((int) vehiclesNumber));
                        tfVehNum.setVehiclesNumber(vehiclesNumber);
                    }

                }
            }
        }
    }

    public void loadTimeSettings(String line) {
        if (line.startsWith("simtime,")) {
            String[] tokens = line.split(",");
            if (tokens.length == 2) {
                int totalTime = Integer.parseInt(tokens[1]);
                SimulationController.simTimeLength = totalTime;
                simContrl.spinnerTimeHours.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory((totalTime / 60), 23, 0));
                simContrl.spinnerTimeMinutes.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory((totalTime % 60), 59, 0));
            }
        }
        if (line.startsWith("simspeed,")) {
            String[] tokens = line.split(",");
            if (tokens.length == 2) {
                double speed = Double.parseDouble(tokens[1]);
                SimulationController.simSpeed = speed;
                simContrl.sliderTimeSpeed.setValue(speed);
            }
        }
    }

    // Wczytanie skrzyżowania
    public void loadSimulation(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Wczytaj plik");

        // Filtr rozszerzeń
        FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("Pliki ITASIM (*.itasim)", "*.itasim");
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
                simContrl.resetSimulation();
                simContrl.resetSimTime();
                simContrl.clearVehicleNumbers();

                String line;
                while ((line = reader.readLine()) != null) {
                    loadIntersectionLane(line);
                    SaveLoadIntersection.loadStopLine(line);
                    SaveLoadIntersection.loadIntersectionLaneButton(line);
                    SaveLoadIntersection.loadMovementRelations(line);
                    loadTextFieldVehicleNumber(line);
                    loadTimeSettings(line);
//                    SaveLoadTrafficLight.loadTrafficLightSettings(line);
//                    SaveLoadTrafficLight.loadSinglePhaseButtonState(line);
                }

                simContrl.loadCanvasOrInfo(simContrl.canvasDrawer);
                copyToTempFile(String.valueOf(file));

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Funkcje pomocnicze służące do zapisania określonych danych w pliku tymczasowym
    public void saveToTempFile() {
        String tempPath = "temp.itasim";
        saveTextFieldVehicleNumber(tempPath, simContrl.tfVehNumInputs);
        saveTimeSettings(tempPath);
    }

    public void restoreFromTempFile() {
        File file = new File("temp.itasim");
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    loadTextFieldVehicleNumber(line);
                    loadTimeSettings(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Skopiowanie pliku wczytanego do tymczasowego - rozwiązanie niedocelowe
    public static void copyToTempFile(String original) {
        String temp = "temp.itasim";
        Path originalFile = Path.of(original);      // oryginalny plik
        Path tempFile = Path.of(temp);          // nowy plik z tą samą zawartością

        try {
            Files.copy(originalFile, tempFile, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

}
