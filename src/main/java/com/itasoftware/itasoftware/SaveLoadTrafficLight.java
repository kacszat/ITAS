package com.itasoftware.itasoftware;

import javafx.event.ActionEvent;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SpinnerValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SaveLoadTrafficLight {

    private static TrafficLightController traffLightContrl;

    public SaveLoadTrafficLight(TrafficLightController traffLightContrl) {
        this.traffLightContrl = traffLightContrl;
    }

    // Zapis ustawień SŚ
    public static void saveTrafficLightSettings(String filePath, Boolean saveWithSim) {
        List<String> lines = new ArrayList<>();
        lines.add("tlactive," + SimulationController.isBackFromTLView);
        lines.add("singlephasetime," + TrafficLightController.singlePhase);
        lines.add("phasetime," + CanvasPhase.rectNumber);
        SaveLoadIntersection.saveToFile(filePath, lines, saveWithSim);  // nadpisz linie
    }

    // Zapis stanu poszczególnych SPB
    public static void saveSinglePhaseButtonState(String filePath) {
        List<String> lines = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            StringBuilder line = new StringBuilder("spb," + i);
            for (int j = 0; j < CanvasPhase.rectNumber; j++) {
                SinglePhaseButton spb = SinglePhaseButton.getSinglePhaseButton(i, j);
                assert spb != null;
                line.append(",").append(spb.getPhase());
            }
            lines.add(line.toString());
        }
        SaveLoadIntersection.saveToFile(filePath, lines, true);  // dopisz linie
    }

    // Zapisanie programu faz sygnalizacji świetlnej
    public void saveTrafficLightPhaseProgram(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Zapisz plik");

        // Filtr rozszerzeń
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Plik programu faz sygnalizacji świetlnej ITAS (*.itapha)", "*.itapha")
        );

        // Okno zapisu
        Window window = ((MenuItem) event.getSource()).getParentPopup().getOwnerWindow();
        File file = fileChooser.showSaveDialog(window);

        if (file != null) {
            saveTrafficLightSettings(file.getAbsolutePath(), false);
            saveSinglePhaseButtonState(file.getAbsolutePath());
        }
    }

    // Wczytanie ustawień SŚ
    public static void loadTrafficLightSettings(String line) {
        if (line.startsWith("tlactive,")) { // Obecnie zbędny zapis (zostawiony potencjalnie)
            String[] tokens = line.split(",");
            if (tokens.length == 2) {
                String active = tokens[1];
                if (Objects.equals(active, "true")) { SimulationController.isBackFromTLView = true; }
            }
        }
        if (line.startsWith("singlephasetime,")) {
            String[] tokens = line.split(",");
            if (tokens.length == 2) {
                int singlePhase = Integer.parseInt(tokens[1]);
                TrafficLightController.singlePhase = singlePhase;
                traffLightContrl.spinnerSinglePhase.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 5, singlePhase));
                traffLightContrl.handleSpinnerSinglePhaseClick();
            }
        }
        if (line.startsWith("phasetime,")) {
            String[] tokens = line.split(",");
            if (tokens.length == 2) {
                int phaseTime = Integer.parseInt(tokens[1]);
                CanvasPhase.rectNumber = phaseTime;
                traffLightContrl.handleSpinnerSinglePhaseClick();
            }
        }
    }

    // Wczytabie stanu poszczególnych SPB
    public static void loadSinglePhaseButtonState(String line) {
        if (line.startsWith("spb,")) {
            String[] tokens = line.split(",");
            int row = Integer.parseInt(tokens[1]);
            for (int i = 0; i < (tokens.length - 2); i++) {
                String phase = tokens[i+2];
                SinglePhaseButton spb = SinglePhaseButton.getSinglePhaseButton(row, i);
                if (spb != null && spb.isActivated()) {
                    spb.changePhase(TrafficLight.Phase.valueOf(phase));
                }
            }
        }
    }

    // Wczytanie programu faz sygnalizacji świetlnej
    public void loadTrafficLightPhaseProgram(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Wczytaj plik");

        // Filtr rozszerzeń
        FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("Pliki ITAPHA (*.itapha)", "*.itapha");
        fileChooser.getExtensionFilters().add(filter);

        // Okno wyboru pliku
        Window window = ((MenuItem) event.getSource()).getParentPopup().getOwnerWindow();
        File file = fileChooser.showOpenDialog(window);

        justLoad(file);
    }

    // Wczytanie danych z pliku tymczasowego - przydatne przy wczytywaniu programu faz z zapisu symulacji
    public void loadFromTempFile() {
        File file = new File("temp.itasim");
        justLoad(file);
    }

    private void justLoad(File file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            traffLightContrl.clearButtons();

            String line;
            while ((line = reader.readLine()) != null) {
                loadTrafficLightSettings(line);
                traffLightContrl.loadSpinnersButton();
                loadSinglePhaseButtonState(line);
            }
            traffLightContrl.loadSpinnersButton();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
