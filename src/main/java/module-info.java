module com.mycompany.javasystem {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens com.mycompany.javasystem to javafx.fxml;
    exports com.mycompany.javasystem;
}