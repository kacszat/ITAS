package com.itasoftware.itasoftware;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.VBox;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class ChartCreator {

    public static class ChartImage {
        public final String title;
        public final BufferedImage image;

        public ChartImage(String title, BufferedImage image) {
            this.title = title;
            this.image = image;
        }
    }

    public List<ChartImage> createChartsAsImages() {
        List<ChartImage> images = new ArrayList<>();

        for (Map.Entry<String, List<DataCollector.ChartDataPoint>> entry : DataCollector.chartData.entrySet()) {
            String laneName = entry.getKey();
            List<DataCollector.ChartDataPoint> dataPoints = entry.getValue();

            // Speed chart
            LineChart<Number, Number> speedChart = createChart("Średnia prędkość - " + laneName, "Czas (s)", "Prędkość (km/h)", dataPoints, true);
            images.add(new ChartImage("Średnia prędkość - " + laneName, chartToImage(speedChart)));

            // Stop time chart
            LineChart<Number, Number> stopTimeChart = createChart("Średni czas zatrzymania - " + laneName, "Czas (s)", "Czas zatrzymania (s)", dataPoints, false);
            images.add(new ChartImage("Średni czas zatrzymania - " + laneName, chartToImage(stopTimeChart)));
        }

        return images;
    }

    private LineChart<Number, Number> createChart(String title, String xLabel, String yLabel, List<DataCollector.ChartDataPoint> dataPoints, boolean speed) {
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel(xLabel);
        yAxis.setLabel(yLabel);

        LineChart<Number, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setCreateSymbols(false);
        chart.setTitle(title);

        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName(title);

        for (DataCollector.ChartDataPoint dp : dataPoints) {
            double value = speed ? dp.speed : dp.stopTime;
            series.getData().add(new XYChart.Data<>(dp.time, value));
        }

        chart.getData().add(series);
        chart.setAnimated(false);
        chart.setLegendVisible(false);

        chart.setPrefSize(800, 400);
        return chart;
    }

    private BufferedImage chartToImage(LineChart<Number, Number> chart) {
        VBox container = new VBox(chart);
        Scene scene = new Scene(container);
        WritableImage fxImage = scene.snapshot(null);
        return SwingFXUtils.fromFXImage(fxImage, null);
    }
}
