module com.mycompany.javasystem {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.mycompany.javasystem to javafx.fxml;
    exports com.mycompany.javasystem;
}
