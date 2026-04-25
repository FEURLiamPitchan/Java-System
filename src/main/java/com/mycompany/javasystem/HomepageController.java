package com.mycompany.javasystem;

import javafx.fxml.FXML;
import javafx.stage.Stage;
import javafx.scene.control.ScrollPane;

public class HomepageController {

    @FXML private ScrollPane scrollPane;

    @FXML
    private void openLogin() {
        Stage stage = (Stage) scrollPane.getScene().getWindow();
        SceneTransition.slideTo(stage, "login.fxml", false, getClass());
    }

    @FXML
    private void openRegister() {
        Stage stage = (Stage) scrollPane.getScene().getWindow();
        SceneTransition.slideTo(stage, "register.fxml", false, getClass());
    }
}
