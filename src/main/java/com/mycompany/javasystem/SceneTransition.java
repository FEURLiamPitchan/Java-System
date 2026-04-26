package com.mycompany.javasystem;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import java.util.Set;

public class SceneTransition {
    private static final Set<String> SECRETARY_ALLOWED_SIDEBAR_TABS = Set.of(
        "dashboard", "residents", "documents", "payments", "complaints", "finance",
        "announcements", "settings"
    );

    public static void slideTo(Stage stage, String fxml, boolean maximize, Class<?> context) {
        try {
            String routedFxml = routeForRole(fxml);
            Parent newRoot = FXMLLoader.load(context.getResource(routedFxml));
            applyRoleLabels(newRoot);
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

    private static String routeForRole(String fxml) {
        if (!SessionManager.isSecretary()) return fxml;
        switch (fxml) {
            case "AdminDashboard.fxml": return "SecretaryDashboard.fxml";
            case "Admin.fxml": return "SecretaryDashboard.fxml";
            case "Residents.fxml": return "SecretaryResidents.fxml";
            case "Documents.fxml": return "SecretaryDocuments.fxml";
            case "Payments.fxml": return "SecretaryPayments.fxml";
            case "PaymentArchive.fxml": return "SecretaryPaymentArchive.fxml";
            case "Complaints.fxml": return "SecretaryComplaints.fxml";
            case "Announcements.fxml": return "SecretaryAnnouncements.fxml";
            case "Finances.fxml": return "Secretaryfinances.fxml";
            default: return fxml;
        }
    }

    private static void applyRoleLabels(Parent root) {
        if (!SessionManager.isSecretary()) return;
        for (javafx.scene.Node node : root.lookupAll(".label")) {
            if (!(node instanceof Label)) continue;
            Label label = (Label) node;
            String text = label.getText();
            if ("Management System".equals(text)) {
                label.setText("Secretary Portal");
            } else if ("Admin".equals(text)) {
                label.setText("Secretary");
            }
        }
        for (javafx.scene.Node node : root.lookupAll(".button")) {
            if (!(node instanceof Button)) continue;
            Button button = (Button) node;
            String text = button.getText();
            if (text == null) continue;

            String menuName = normalizeMenuName(text);
            if (!menuName.isEmpty()) {
                boolean allowed = SECRETARY_ALLOWED_SIDEBAR_TABS.contains(menuName);
                button.setManaged(allowed);
                button.setVisible(allowed);
            }
        }
    }

    private static String normalizeMenuName(String text) {
        String normalized = text
            .toLowerCase()
            .replaceAll("[^a-z ]", " ")
            .trim()
            .replaceAll("\\s+", " ");

        if (normalized.contains("dashboard")) return "dashboard";
        if (normalized.equals("residents")) return "residents";
        if (normalized.equals("documents")) return "documents";
        if (normalized.equals("payments")) return "payments";
        if (normalized.contains("archive")) return "archive";
        if (normalized.contains("complaints")) return "complaints";
        if (normalized.contains("announcements")) return "announcements";
        if (normalized.contains("finance")) return "finance";
        if (normalized.contains("admin")) return "admin";
        if (normalized.contains("settings")) return "settings";
        return "";
    }
}