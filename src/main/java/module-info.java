module com.mycompany.javasystem {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.base;
    requires java.sql;
    requires okhttp3;
    requires org.json;
    requires kotlin.stdlib;
    requires itextpdf;
    requires java.desktop;
    requires javafx.swing;
    opens com.mycompany.javasystem to javafx.fxml;
    exports com.mycompany.javasystem;
}