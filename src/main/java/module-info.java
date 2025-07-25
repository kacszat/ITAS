module com.itasoftware.itasoftware {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires java.desktop;
    requires com.github.librepdf.openpdf;
    requires javafx.swing;

    opens com.itasoftware.itasoftware to javafx.fxml;
    exports com.itasoftware.itasoftware;
}