package com.itasoftware.itasoftware;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

public class SaveReport {

    public void saveReport(ActionEvent event, List<ChartCreator.ChartImage> charts) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Zapisz raport jako PDF");

        // Filtr rozszerzeń dla plików PDF
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Dokument PDF (*.pdf)", "*.pdf")
        );

        // Okno zapisu
        Window window = ((Node) event.getSource()).getScene().getWindow();
        File file = fileChooser.showSaveDialog(window);

        if (file != null) {
            String filePath = file.getAbsolutePath();

            // Dodaj rozszerzenie .pdf, jeśli użytkownik go nie wpisał
            if (!filePath.toLowerCase().endsWith(".pdf")) {
                filePath += ".pdf";
            }

            exportReportToPdf(filePath, DataCollector.reportContent.toString(), charts);
        }
    }

    public static void exportReportToPdf(String filename, String content, List<ChartCreator.ChartImage> charts) {
        try {
            Document document = new Document();

            PdfWriter.getInstance(document, new FileOutputStream(filename));
            document.open();
            document.add(new Paragraph("RAPORT PODSUMOWUJĄCY SYMULACJĘ\n\n"));
            document.add(new Paragraph(content));   // Informacje o płynności ruchu

            // Wykresy - obecnie nie używane
//            for (ChartCreator.ChartImage chart : charts) {
//                document.add(new Paragraph(chart.title));
//                document.add(Chunk.NEWLINE);
//
//                ByteArrayOutputStream baos = new ByteArrayOutputStream();
//                ImageIO.write(chart.image, "png", baos);
//                Image img = Image.getInstance(baos.toByteArray());
//                img.scaleToFit(500, 300);
//                document.add(img);
//                document.add(Chunk.NEWLINE);
//                document.newPage();
//            }

            document.close();
            System.out.println("PDF zapisany jako " + filename);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
