package com.mycompany.javasystem;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

public class Kagawad_SettingsController {

    @FXML private Button logoutButton;
    @FXML private Button alertsButton;
    @FXML private Label alertBadge;
    @FXML private HBox avatarBox;
    @FXML private Circle avatarCircle;
    @FXML private ImageView profileImageView;
    @FXML private Label avatarInitialLabel;
    @FXML private Label topBarNameLabel;
    @FXML private Label topBarRoleLabel;
    @FXML private StackPane tabContent;
    @FXML private ScrollPane mainScrollPane;
    @FXML private Button tabGeneralBtn;
    @FXML private Button tabNotifBtn;
    @FXML private Button tabAppearanceBtn;
    @FXML private Button tabDataBtn;

    private SettingsController baseController;

    @FXML
    public void initialize() {
        baseController = new SettingsController();
        ensureRequiredControls();
        copyFXMLFields();
        try {
            baseController.initialize();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        Platform.runLater(() -> {
            hideRestrictedElements();
            showNotifTab();
        });
    }

    private void ensureRequiredControls() {
        // SettingsController expects these tab buttons to exist during setup.
        // Kagawad settings hides these tabs, so provide placeholders to avoid null crashes.
        if (tabGeneralBtn == null) {
            tabGeneralBtn = new Button();
        }
        if (tabDataBtn == null) {
            tabDataBtn = new Button();
        }
    }

    private void copyFXMLFields() {
        java.lang.reflect.Field[] fields = SettingsController.class.getDeclaredFields();
        for (java.lang.reflect.Field field : fields) {
            if (field.isAnnotationPresent(FXML.class)) {
                try {
                    field.setAccessible(true);
                    java.lang.reflect.Field thisField = this.getClass().getDeclaredField(field.getName());
                    thisField.setAccessible(true);
                    field.set(baseController, thisField.get(this));
                } catch (NoSuchFieldException ignored) {
                    // Ignore FXML fields that are not part of kagawad layout.
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void hideRestrictedElements() {
        if (tabDataBtn != null) {
            tabDataBtn.setVisible(false);
            tabDataBtn.setManaged(false);
        }
        
        if (tabGeneralBtn != null) {
            tabGeneralBtn.setVisible(false);
            tabGeneralBtn.setManaged(false);
        }
    }

    @FXML private void showNotifTab() {
        if (baseController != null) {
            try {
                java.lang.reflect.Method method = SettingsController.class.getDeclaredMethod("showNotifTab");
                method.setAccessible(true);
                method.invoke(baseController);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @FXML private void showAppearanceTab() {
        if (baseController != null) {
            try {
                java.lang.reflect.Method method = SettingsController.class.getDeclaredMethod("showAppearanceTab");
                method.setAccessible(true);
                method.invoke(baseController);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @FXML private void handleAlertsClick() {
        if (baseController != null) {
            try {
                java.lang.reflect.Method method = SettingsController.class.getDeclaredMethod("handleAlertsClick");
                method.setAccessible(true);
                method.invoke(baseController);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @FXML private void handleLogout() {
        SessionManager.logout();
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        SceneTransition.slideTo(stage, "login.fxml", false, getClass());
    }

    @FXML private void goToDashboard() {
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        SceneTransition.slideTo(stage, "KagawadDashboard.fxml", true, getClass());
    }

    @FXML private void goToResidents() {
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        SceneTransition.slideTo(stage, "KagawadResidents.fxml", true, getClass());
    }

    @FXML private void goToDocuments() {
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        SceneTransition.slideTo(stage, "KagawadDocuments.fxml", true, getClass());
    }

    @FXML private void goToPayments() {
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        SceneTransition.slideTo(stage, "KagawadDocuments.fxml", true, getClass());
    }

    @FXML private void goToArchive() {
        // RBAC: Payments — Read-Only for Kagawad; archive management not permitted
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Access Denied");
        alert.setHeaderText(null);
        alert.setContentText("Payment archive management is not available for your role.");
        alert.showAndWait();
    }

    @FXML private void goToComplaints() {
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        SceneTransition.slideTo(stage, "KagawadComplaints.fxml", true, getClass());
    }

    @FXML private void goToAnnouncements() {
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        SceneTransition.slideTo(stage, "KagawadAnnouncements.fxml", true, getClass());
    }

    @FXML private void goToFinances() {
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        SceneTransition.slideTo(stage, "KagawadFinances.fxml", true, getClass());
    }

    @FXML private void handleAvatarClick() {
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        SceneTransition.slideTo(stage, "KagawadProfile.fxml", true, getClass());
    }
}
