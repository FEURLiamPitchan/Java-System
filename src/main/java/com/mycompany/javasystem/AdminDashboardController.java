package com.mycompany.javasystem;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.util.Duration;

public class AdminDashboardController {
    @FXML
    private Button logoutButton;

    private void switchScene(String fxml, boolean maximize) {
        try {
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            Parent currentRoot = stage.getScene().getRoot();

            FadeTransition fadeOut = new FadeTransition(Duration.millis(200), currentRoot);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(e -> {
                try {
                    Parent newRoot = FXMLLoader.load(getClass().getResource(fxml));
                    newRoot.setOpacity(0.0);
                    stage.setMaximized(maximize);
                    stage.getScene().setRoot(newRoot);

                    FadeTransition fadeIn = new FadeTransition(Duration.millis(200), newRoot);
                    fadeIn.setFromValue(0.0);
                    fadeIn.setToValue(1.0);
                    fadeIn.play();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
            fadeOut.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout() {
        switchScene("login.fxml", false);
    }

    @FXML
    private void goToResidents() {
        switchScene("Residents.fxml", true);
    }
}