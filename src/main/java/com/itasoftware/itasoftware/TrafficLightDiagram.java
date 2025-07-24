package com.itasoftware.itasoftware;

import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class TrafficLightDiagram {

    public static Map<String, List<TrafficLight.Phase>> trafficlightWithLabelMap = new LinkedHashMap<>();

    public static void saveTrafficDiagramAsPng(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Zapisz diagram jako PNG");

        // Filtr rozszerzeń dla plików PNG
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Obraz PNG (*.png)", "*.png")
        );

        // Okno zapisu
        Window window = ((Node) event.getSource()).getScene().getWindow();
        File file = fileChooser.showSaveDialog(window);

        if (file != null) {
            String filePath = file.getAbsolutePath();

            // Dodanie rozszerzenia .png, jeśli użytkownik go nie wpisał
            if (!filePath.toLowerCase().endsWith(".png")) {
                filePath += ".png";
            }

            try {
                prepareDataToTrafficLightsDiagram();
                TrafficLightDiagram.generateDiagram(TrafficLightDiagram.trafficlightWithLabelMap, filePath, true);
                System.out.println("Zapisano diagram: " + filePath);
            } catch (IOException e) {
                System.err.println("Błąd zapisu diagramu: " + e.getMessage());
            }
        } else {
            System.out.println("Zapis anulowany przez użytkownika.");
        }
    }

    public static void getTrafficLightsDiagramWindow() {
        prepareDataToTrafficLightsDiagram();
        BufferedImage diagramImage;
        try {
            diagramImage = generateDiagram(trafficlightWithLabelMap, null, false);
        } catch (IOException e) {
            System.err.println("Błąd tworzenia diagramu: " + e.getMessage());
            return;
        }

        javafx.scene.image.Image fxImage = SwingFXUtils.toFXImage(diagramImage, null);
        ImageView imageView = new ImageView(fxImage);

        ScrollPane scrollPane = new ScrollPane(imageView);
        scrollPane.setPannable(true);

        Stage stage = new Stage();
        stage.setTitle("Podgląd diagramu sygnalizacji świetlnej");
        stage.setScene(new Scene(scrollPane));
        stage.setWidth(1000);
        stage.setHeight(500);
        stage.show();
    }

    // Przygotowanie danych do budowy diagramu faz sygnalizacji świetlnej
    public static void prepareDataToTrafficLightsDiagram() {
        trafficlightWithLabelMap.clear(); // wyczyszczenie przed dodaniem
        List<String> directions = List.of("NORTH", "SOUTH", "WEST", "EAST");
        List<String> types = List.of("MAIN", "LEFT", "RIGHT");

        for (String direction : directions) {   // Sortowanie kolejności kierunków i typów pasów
            for (String type : types) {
                for (RowDescriptor rd : RowDescriptor.rowDescriptors) {
                    if (!rd.getLocalization().toString().equals(direction) || !rd.getType().toString().equals(type)) {
                        continue;
                    }

                    List<SinglePhaseButton> activeButtons = new ArrayList<>();
                    for (SinglePhaseButton spb : SinglePhaseButton.singlePhaseButtons) {
                        if (spb.activated && spb.getDesc() == rd) {
                            activeButtons.add(spb);
                        }
                    }
                    if (activeButtons.isEmpty()) continue;

                    List<TrafficLight.Phase> diagramPhaseList = new ArrayList<>();
                    for (SinglePhaseButton spb : activeButtons) {
                        for (int i = 0; i < CanvasPhase.rectNumber; i++) {
                            if (spb.getColNumber() == i) {
                                diagramPhaseList.add(spb.getPhase());
                            }
                        }
                    }

                    String label = direction + " " + type;
                    trafficlightWithLabelMap.put(label, diagramPhaseList);
                }
            }
        }
    }

    // Funkcja generująca diagram
    public static BufferedImage generateDiagram(Map<String, List<TrafficLight.Phase>> trafficProgram, String outputFilePath, boolean save) throws IOException {
        int programTime = TrafficLightController.completePhase / TrafficLightController.singlePhase;
        int cellWidth = 10;
        int rowHeight = 20;
        int rowSpacing = 10;
        int labelWidth = 100;
        int margin = 20;
        int axisTimeHeight = 15; // Wysokość przestrzeni opisu osi czasu

        int totalRowHeight = rowHeight + rowSpacing;
        int totalRows = trafficProgram.size();

        // Całkowita wysokość i szerokość rysunku diagramu
        int width = margin * 2 + labelWidth + programTime * cellWidth;
        int height = margin * 2 + totalRows * totalRowHeight - rowSpacing + axisTimeHeight;  // odejęcie 1x spacing na końcu (ostatni rząd nie potrzebuje odstępu pod spodem)

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();

        g.setColor(Color.WHITE); // Białe tło
        g.fillRect(0, 0, width, height);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setFont(new Font("Arial", Font.PLAIN, 12));

        // Rysowanie kafelek
        int row = 0;
        for (Map.Entry<String, List<TrafficLight.Phase>> entry : trafficProgram.entrySet()) {
            String direction = entry.getKey();
            List<TrafficLight.Phase> phases = entry.getValue();

            // Rysowanie etykiet rzędów kafelków
            g.setColor(Color.BLACK);
            int y = margin + row * totalRowHeight;
            int labelY = y + rowHeight / 2 + 5;
            g.drawString(direction, margin + 5, labelY);

            for (int sec = 0; sec < phases.size(); sec++) {
                TrafficLight.Phase phase = phases.get(sec);
                Color color = switch (phase) {
                    case RED -> new Color(200, 50, 50);
                    case YELLOW -> new Color(200, 200, 50);
                    case GREEN -> new Color(0, 200, 0);
                    case RED_YELLOW -> new Color(250, 150, 50);
                    case GREEN_ARROW -> new Color(0, 75, 0);
                };

                int x = margin + labelWidth + sec * cellWidth;

                g.setColor(color);
                g.fillRect(x, y, cellWidth, rowHeight); // Wypełnienie kolorem

                g.setColor(Color.BLACK);
                g.drawRect(x, y, cellWidth, rowHeight); // Ramka
            }
            row++;
        }

        // Oś czasu (poniżej wykresów)
        g.setColor(Color.BLACK);
        int topY = margin / 2; // górny rząd diagramu kafelków plus 0.5 marginesu
        int bottomY = margin + totalRows * totalRowHeight - rowSpacing; // ostatni rząd diagramu kafelków
        int axisY = bottomY + rowSpacing; // pozycja linii osi czasu

        g.setColor(Color.BLACK);

        // Rysowanie linii pionowej co 5 sekund
        for (int sec = 0; sec <= programTime; sec += 5) {
            int x = margin + labelWidth + sec * cellWidth;

            // Linia od góry kafelków do osi czasu
            g.drawLine(x, topY, x, axisY - 5);

            // Znaczniki na osi czasu
            g.drawLine(x, axisY - 5, x, axisY);
            g.drawString(String.valueOf(sec * TrafficLightController.singlePhase), x - 5, axisY + 12);
        }

        g.dispose();

        if (save && outputFilePath != null && !outputFilePath.isBlank()) {  // Jeśli zapisujemy jako PNG
            ImageIO.write(image, "png", new File(outputFilePath));
        }

        return image;
    }

}
