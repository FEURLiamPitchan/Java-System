module com.mycompany.javasystem {
    requires javafx.controls;
    requires javafx.fxml;
<<<<<<< Updated upstream
=======
    requires java.sql;
    requires java.desktop;
>>>>>>> Stashed changes

    opens com.mycompany.javasystem to javafx.fxml;
    exports com.mycompany.javasystem;
}
