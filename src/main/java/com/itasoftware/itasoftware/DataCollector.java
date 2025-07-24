package com.itasoftware.itasoftware;

import javafx.scene.control.TextField;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataCollector {

    private static VehicleManager vehMan;
    public DataCollector(VehicleManager vehMan) { DataCollector.vehMan = vehMan; }

    public static StringBuilder reportContent = new StringBuilder(); // Scalony tekst raportu

    // Przechowanie danych do wykresów (klucz = nazwa pasa, np. "Wlot: NORTH 0")
    public static Map<String, List<ChartDataPoint>> chartData = new HashMap<>();

    public static void generateData() { // Funkcja agregująca dane
        describeSimulationTime();
        collectAllDataAndCalculateAverageValues();
        describePossibleRelations();
        describeVehiclesNumber();
        if (SimulationController.areTrafficLightsActive) {
            if (SettingsController.saveReportWithTextPhaseProgram) { describeTrafficLightsProgram(); }
            if (SettingsController.saveReportWithPhaseDiagram) { describeTrafficLightsDiagram(); }
        }
    }

    private static void describeSimulationTime() {
        long time = (vehMan.currentTime) / 1000;  // Czas w sekundach
        long timePlanned = SimulationController.simTimeLength;  // Czas w minutach

        long hours = time / 3600;
        long minutes = (time % 3600) / 60;
        long seconds = time % 60;
        long hoursPlanned = timePlanned / 60;
        long minutesPlanned = timePlanned % 60;

        reportContent.append("Zaplanowany czas trwania symulacji: ").append("\n").append("  Godzin: ").append(hoursPlanned).append("\n").
                append("  Minut: ").append(minutesPlanned).append("\n");
        reportContent.append("Wykonany czas trwania symulacji: ").append("\n").append("  Godzin: ").append(hours).append("\n").
                append("  Minut: ").append(minutes).append("\n").append("  Sekund: ").append(seconds).append("\n\n");
    }

    private static void collectAllDataAndCalculateAverageValues() {
        for (IntersectionLane il : GeneratorController.intersectionLanes) {
            double averageSpeed, averageStopTime, averageStopCounts;
            double averageSpeedSum = 0, averageStopTimeSum = 0, averageStopCountsSum = 0;
            double averageSpeedMin = Double.MAX_VALUE, averageStopTimeMax = 0, averageStopCountsMax = 0;
            int count = 0;

            for (StopLine sl : GeneratorController.stopLines) {
                if (il.getLocalization() == sl.getLocalization() && il.getIndex() == sl.getIndex() && il.getType() == IntersectionLane.Type.ENTRY
                && sl.getType() == IntersectionLane.Type.ENTRY) {
                    for (Vehicle v : VehicleManager.finishedVehiclesList) {
                        if (v.getAssignedStopLine() == sl) {
                            averageSpeedSum = averageSpeedSum + v.getVehicleAverageSpeed();
                            averageStopTimeSum = averageStopTimeSum + v.getVehicleStopTimeTotal();
                            averageStopCountsSum = averageStopCountsSum + v.getVehicleStopsCount();

                            if (v.getVehicleAverageSpeed() < averageSpeedMin) { averageSpeedMin = v.getVehicleAverageSpeed(); }
                            if (v.getVehicleStopTimeTotal() > averageStopTimeMax) { averageStopTimeMax = v.getVehicleStopTimeTotal(); }
                            if (v.getVehicleStopsCount() > averageStopCountsMax) { averageStopCountsMax = v.getVehicleStopsCount(); }

                            count++;
                        }
                    }
                    if (count == 0) continue;

                    averageSpeed = averageSpeedSum / count;
                    averageStopTime = averageStopTimeSum / count;
                    averageStopCounts = averageStopCountsSum / count;

                    String laneInfo = "Wlot: " + il.getLocalization() + " " + il.getIndex();
                    reportContent.append(laneInfo).append(":\n");
                    reportContent.append("  Średnia prędkość pojazdów na dojeździe do skrzyżowania: ").append(String.format("%.2f", averageSpeed)).append(" km/h\n");
                    reportContent.append("  Minimalna średnia prędkość pojazdu na dojeździe do skrzyżowania: ").append(String.format("%.2f", averageSpeedMin)).append(" km/h\n");
                    reportContent.append("  Średni czas oczekiwania pojazdów przed skrzyżowaniem: ").append(String.format("%.2f", averageStopTime)).append(" sekund\n");
                    reportContent.append("  Maksymalny czas oczekiwania pojazdu przed skrzyżowaniem: ").append(String.format("%.2f", averageStopTimeMax)).append(" sekund\n");
                    reportContent.append("  Średnia liczba zatrzymań pojazdów przed skrzyżowaniem: ").append(String.format("%.2f", averageStopCounts)).append("\n");
                    reportContent.append("  Maksymalna liczba zatrzymań pojazdu przed skrzyżowaniem: ").append(String.format("%.2f", averageStopCountsMax)).append("\n\n");

                }
            }
        }
    }

    // Opis tekstowy dozwolonych na skrzyżowaniu relacji ruchu
    private static void describePossibleRelations() {
        String relationsInfo = "Dopuszczone relacje ruchu na analizowanym skrzyżowaniu: ";
        reportContent.append(relationsInfo).append("\n");
        for (MovementRelations mr : MovementRelations.movementRelations) {
            if (mr.getObjectA().getType() == IntersectionLane.Type.ENTRY) {
                reportContent.append("  Relacja z pasa: ").append(mr.getObjectA().getType()).append(" ").append(mr.getObjectA().getLocalization()).append(" ").
                        append(mr.getObjectA().getIndex()).append(" do pasa: ").append(mr.getObjectB().getType()).append(" ").
                        append(mr.getObjectB().getLocalization()).append(" ").append(mr.getObjectB().getIndex()).append("\n");
            }
        }
        reportContent.append("\n");
    }

    // Opis liczby pojazdów na danych relacjach
    private static void describeVehiclesNumber() {
        String relationsInfo = "Liczba pojazdów zliczonych w stosunku do liczby pojazdów zaplanowanych na następujących relacjach wynosi: ";
        reportContent.append(relationsInfo).append("\n");
        for (Map.Entry<TextField, TextFieldVehicleNumber> entry : SimulationController.textfieldMap.entrySet()) {
            TextField tf = entry.getKey();
            TextFieldVehicleNumber tfVehNum = entry.getValue();
            int finishedVehiclesCount = 0;

            if (tfVehNum.getVehiclesNumber() != 0) {
                for (Vehicle v : VehicleManager.finishedVehiclesList) {
                    if (v.getVehicleOrigin() == tfVehNum.getLocalization() && v.getVehicleDestination() == tfVehNum.getDestination()) {
                        finishedVehiclesCount++;    // Ilość pojazdów z danej relacji, które ukończyły swoje trajektorie, w chwili wygenerowania raportu
                    }
                }
                reportContent.append("  Z kierunku: ").append(tfVehNum.getLocalization()).append(" do kierunku: ").append(tfVehNum.getDestination()).
                        append(" występuje liczba pojazdów równa: ").append(finishedVehiclesCount).append("/").
                        append(String.format("%.0f", tfVehNum.getVehiclesNumber())).append("\n");
            }
        }
        reportContent.append("\n");
    }

    // Opis tekstowy programu faz sygnalizacji świetlnej
    public static void describeTrafficLightsProgram() {
        reportContent.append("Zapis tekstowy programu faz sygnalizacji świetlnej należy interpretować w wyznaczony sposób.").append("\n").
                append("Przykładowo, zapis 4_GREEN, 2_RED, 2_GREEN oznacza ośmio sekundowy program faz, gdzie pierwsze 4 sekundy to " +
                        "faza zielona, następnie po nich występują 2 sekundy fazy czerwonej, a potem znów 2 sekundy fazy zielonej.").append("\n\n");
        reportContent.append("Całkowita długość programu faz sygnalizacji świetlnej wynosi: ").append(TrafficLightController.completePhase).append(" sekund").append("\n\n");
        reportContent.append("Programy faz sygnalizacji świetlnej dla wskazanych lokalizacji i kierunków wyglądają następująco:").append("\n");

        for (RowDescriptor rd : RowDescriptor.rowDescriptors) {

            List<SinglePhaseButton> activeButtons = new ArrayList<>();
            for (SinglePhaseButton spb : SinglePhaseButton.singlePhaseButtons) {
                if (spb.activated && spb.getDesc() == rd) {
                    activeButtons.add(spb);
                }
            }
            // Jeśli nie ma żadnych aktywnych przycisków pomijamy wypisywanie poniższego append
            if (activeButtons.isEmpty()) { continue; }
            reportContent.append("  Układ faz dla pasa/pasów ruchu typu: ").append(rd.getLocalization()).append(" ").append(rd.getType()).append(" to: ");

            TrafficLight.Phase previousPhase = null;
            int samePhaseCount = 1;

            for (SinglePhaseButton spb : SinglePhaseButton.singlePhaseButtons) {
                if (!spb.activated) { continue; } // Jeśli nie programu faz dla danego pasa/pasów, to pomijamy

                if (spb.getDesc() == rd) {
                    for (int i = 0; i < CanvasPhase.rectNumber; i++) {  // Wypisanie programu faz sygnalizacji świetlnej
                        if (spb.getColNumber() == i) {
                            if (spb.getPhase() == previousPhase) {
                                samePhaseCount++;
                                if (i == (CanvasPhase.rectNumber - 1)) {
                                    reportContent.append(samePhaseCount).append("_").append(previousPhase).append(", ");
                                    continue;
                                }
                            } else if (previousPhase != null) {
                                reportContent.append(samePhaseCount).append("_").append(previousPhase).append(", ");
                                samePhaseCount = 1;
                                if (i == (CanvasPhase.rectNumber - 1)) {
                                    reportContent.append(samePhaseCount).append("_").append(spb.getPhase()).append(", ");
                                    continue;
                                }
                            }
                            previousPhase = spb.getPhase();
                        }
                    }
                }
            }

            reportContent.append("\n");
        }
        reportContent.append("\n");
    }

    public static void describeTrafficLightsDiagram() {
        reportContent.append("Poniżej umieszczony jest diagram programu faz sygnalizacji świetlnej. Dolna oś reprezentuje czas trwania programu. " +
                        "Poszczególne kolory reprezentują następujące fazy: ").append("\n").append("Kolor czerwony - faza światła czerwonego ").append("\n").
                        append("Kolor żółty - faza światła żółtego ").append("\n").append("Kolor zielony - faza światła zielonego ").append("\n").
                        append("Kolor pomarańczowy - faza światła czerwono-żółtego ").append("\n").
                        append("Kolor ciemno-zielony - faza światła zielonej strzałki ").append("\n\n");

        reportContent.append("Całkowita długość programu faz sygnalizacji świetlnej wynosi: ").append(TrafficLightController.completePhase).append(" sekund").append("\n");
    }

    public static void clearReportContent() {  // Wyczyszczenie zawartości StringBuildera
        reportContent.setLength(0);
    }



    public void collectDataForCharts(long elapsedTime) {
        for (IntersectionLane il : GeneratorController.intersectionLanes) {
            double averageSpeed, averageStopTime;
            double averageSpeedSum = 0, averageStopTimeSum = 0;
            int count = 0;

            for (StopLine sl : GeneratorController.stopLines) {
                if (il.getLocalization() == sl.getLocalization() && il.getIndex() == sl.getIndex() && il.getType() == IntersectionLane.Type.ENTRY
                        && sl.getType() == IntersectionLane.Type.ENTRY) {
                    for (Vehicle v : vehMan.vehiclesList) {
                        if (v.getAssignedStopLine() == sl) {
                            averageSpeedSum = averageSpeedSum + v.getVehicleAverageSpeed();
                            averageStopTimeSum = averageStopTimeSum + v.getVehicleStopTimeTotal();

                            count++;
                        }
                    }
                    if (count == 0) continue;

                    averageSpeed = averageSpeedSum / count;
                    averageStopTime = averageStopTimeSum / count;

                    String key = "Wlot: " + il.getLocalization() + " " + il.getIndex();

                    chartData.computeIfAbsent(key, k -> new ArrayList<>()).add(new ChartDataPoint(elapsedTime, averageSpeed, averageStopTime));
                }
            }
        }
    }

    // klasa pomocnicza do przechowywania pojedyńczych danych
    public static class ChartDataPoint {
        public final long time;
        public final double speed;
        public final double stopTime;

        public ChartDataPoint(long time, double speed, double stopTime) {
            this.time = time;
            this.speed = speed;
            this.stopTime = stopTime;
        }
    }

}