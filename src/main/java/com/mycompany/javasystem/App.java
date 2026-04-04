package com.mycompany.javasystem;

import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        System.setProperty("prism.text", "t2k");
        
        Parent root = FXMLLoader.load(getClass().getResource("login.fxml"));
        Scene scene = new Scene(root, 1366, 768);
        
        // Load CSS stylesheet
        String css = getClass().getResource("style.css").toExternalForm();
        scene.getStylesheets().add(css);
        
        stage.setTitle("Barangay System");
        stage.setScene(scene);
        stage.setResizable(true);
        
        // Set primary stage for theme manager
        ThemeManager.setPrimaryStage(stage);
        
        // DON'T load theme here - user is not logged in yet!
        // It will be loaded when they navigate to Settings or any page after login
        
        stage.show();
    }

    public static void main(String[] args) {
        System.setProperty("prism.text", "t2k");
        launch(args);
    }
}