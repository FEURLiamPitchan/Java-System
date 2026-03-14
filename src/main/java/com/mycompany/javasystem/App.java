package com.mycompany.javasystem;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
public class App extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        System.setProperty("prism.text", "t2k");
        Application.setUserAgentStylesheet(Application.STYLESHEET_CASPIAN);
        Parent root = FXMLLoader.load(getClass().getResource("login.fxml"));
        Scene scene = new Scene(root, 1366, 768);
        stage.setTitle("Barangay System");
        stage.setScene(scene);
        stage.setResizable(true);
        stage.show();
    }
    public static void main(String[] args) {
        System.setProperty("prism.text", "t2k");
        launch(args);
    }
}