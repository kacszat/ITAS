package com.itasoftware.itasoftware;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class AlertPopUp {

    static boolean answer; // Odpowiedź z PopUp Alert
    private Stage window; // Stage dla okna PopUp Alert

    @FXML
    private Label alertLabel, alertLabel2;

    @FXML
    private Button alertButtonYes, alertButtonNo;

    public static boolean showAlertPopUp(String type) {
        try {
            FXMLLoader loader = new FXMLLoader(AlertPopUp.class.getResource("AlertPopUp.fxml"));
            Parent root = loader.load();

            AlertPopUp controller = loader.getController(); // Pobranie kontrolera

            // Ustawienie tekstu na podstawie typu alertu
            switch (type) {
                case "Save" -> controller.alertLabel.setText("Czy na pewno chcesz zamknąć program?");
                case "Load" -> controller.alertLabel.setText("Czy na pewno chcesz wczytać nowy plik?");
                case "Quit Generator" -> controller.alertLabel.setText("Czy na pewno chcesz wyjść z generatora skrzyżowań?");
                case "New Generator" -> controller.alertLabel.setText("Czy na pewno chcesz utworzyć nowe skrzyżowanie?");
                case "Quit Simulation" -> controller.alertLabel.setText("Czy na pewno chcesz wyjść z symulacji ruchu?");
                case "New Simulation" -> controller.alertLabel.setText("Czy na pewno chcesz utworzyć nową symulację?");
                case "Lack Relations" -> {controller.alertLabel.setText("Czy na pewno chcesz przejść do okna symulacji ruchu?");
                                            controller.alertLabel2.setText("Na skrzyżowaniu nie ma wyznaczonych żadnych relacji ruchu.");}
                case "Delete Relations" -> {controller.alertLabel.setText("Czy na pewno chcesz zmienić liczbę pasów ruchu?");
                                            controller.alertLabel2.setText("Istniejące relacje ruchu zostaną usunięte.");}
                case "New TL" -> controller.alertLabel.setText("Czy na pewno chcesz utworzyć nowy program faz?");
                case "Bad VehicleNumber" -> {controller.alertLabel.setText("Wprowadzono niewłaściwe dane dotyczące liczby pojazdów.");
                                            controller.alertLabel2.setText("Całkowita liczba pojazdów musi być większa niż zero.");
                                            controller.alertButtonYes.setVisible(false);
                                            controller.alertButtonYes.setManaged(false);
                                            controller.alertButtonNo.setText("OK");}
                case "Bad SimTime" -> {controller.alertLabel.setText("Wprowadzono niewłaściwą wartość czasu trwania symulacji.");
                                            controller.alertLabel2.setText("Minimalny czas trwania symulacji to jedna minuta.");
                                            controller.alertButtonYes.setVisible(false);
                                            controller.alertButtonYes.setManaged(false);
                                            controller.alertButtonNo.setText("OK");}
                case "Can't Save Sim" -> {controller.alertLabel.setText("Obecnie nie można zapisać symulacji.");
                                            controller.alertLabel2.setText("Nie wczytano skrzyżowania, bądź na skrzyżowaniu nie ma relacji ruchu.");
                                            controller.alertButtonYes.setVisible(false);
                                            controller.alertButtonYes.setManaged(false);
                                            controller.alertButtonNo.setText("OK");}
                case "Can't Show TL" -> {controller.alertLabel.setText("Obecnie nie można włączyć sygnalizacji świetlnej.");
                                            controller.alertLabel2.setText("Nie utworzono/wczytano żadnego programu faz sygnalizacji świetlnej.");
                                            controller.alertButtonYes.setVisible(false);
                                            controller.alertButtonYes.setManaged(false);
                                            controller.alertButtonNo.setText("OK");}
                case "Can't Save TL" -> {controller.alertLabel.setText("Obecnie nie można zapisać programu faz sygnalizacji świetlnej.");
                                            controller.alertLabel2.setText("Nie wczytano skrzyżowania, bądź na skrzyżowaniu nie ma relacji ruchu.");
                                            controller.alertButtonYes.setVisible(false);
                                            controller.alertButtonYes.setManaged(false);
                                            controller.alertButtonNo.setText("OK");}
                default -> controller.alertLabel.setText("Czy na pewno chcesz wykonać tę akcję?");
            }

            Stage window = new Stage();
            controller.window = window; // Stage w kontrolerze

            window.initModality(Modality.APPLICATION_MODAL);
            window.setTitle("Ostrzeżenie");
            window.setMinWidth(300);
            window.setMinHeight(150);

            Scene scene = new Scene(root);
            window.setScene(scene);
            window.showAndWait(); // Zatrzymuje wykonywanie kodu do momentu zamknięcia PopUp Alertu

            return answer;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @FXML
    private void clickYes() {
        answer = true;
        window.close();
    }

    @FXML
    private void clickNo() {
        answer = false;
        window.close();
    }

}
