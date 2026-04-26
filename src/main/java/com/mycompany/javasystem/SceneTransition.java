package com.mycompany.javasystem;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;

public class SceneTransition {
    public static void slideTo(Stage stage, String fxml, boolean maximize, Class<?> context) {
        try {
            Parent newRoot = FXMLLoader.load(context.getResource(fxml));
            stage.setMaximized(maximize);
            stage.getScene().setRoot(newRoot);
            
            Platform.runLater(() -> {
                // Apply theme to new scene
                ThemeManager.loadThemePreference();
                ThemeManager.applyTheme(stage);
                
                Platform.runLater(() -> {
                    newRoot.requestFocus();
                });
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}