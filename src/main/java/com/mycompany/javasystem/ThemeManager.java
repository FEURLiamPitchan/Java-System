package com.mycompany.javasystem;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.chart.Chart;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

public class ThemeManager {
    
    public static boolean isDarkMode = false;
    private static Stage primaryStage = null;
    
    // Modern dark mode color palette
    private static final String DARK_BG_PRIMARY = "#121212";
    private static final String DARK_BG_SECONDARY = "#1e1e1e";
    private static final String DARK_BG_TERTIARY = "#2a2a2a";
    private static final String DARK_BG_CHART = "#1a1a1a";
    private static final String DARK_TEXT = "#e8e8e8";
    private static final String DARK_TEXT_SECONDARY = "#b0b0b0";
    private static final String DARK_BORDER = "#404040";
    
    // Store original styles to restore them
    private static final Map<Node, String> originalStyles = new HashMap<>();

    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    public static void loadThemePreference() {
        String email = SessionManager.getEmail();
        System.out.println("[ThemeManager] loadThemePreference() - email: " + email);
        if (email == null) {
            isDarkMode = false;
            return;
        }
        try {
            Connection conn = DatabaseConnection.getConnection();
            ResultSet rs = conn.prepareStatement(
                "SELECT dark_mode FROM settings WHERE user_email = '" + email + "'"
            ).executeQuery();
            if (rs.next()) {
                String dm = rs.getString("dark_mode");
                isDarkMode = "true".equalsIgnoreCase(dm);
                System.out.println("[ThemeManager] Loaded from DB - dark_mode: " + dm);
            } else {
                isDarkMode = false;
            }
            rs.close();
            conn.close();
        } catch (Exception e) {
            System.out.println("[ThemeManager] Error: " + e.getMessage());
            isDarkMode = false;
        }
    }

    public static void saveThemePreference(boolean darkMode) {
        isDarkMode = darkMode;
        String email = SessionManager.getEmail();
        System.out.println("[ThemeManager] saveThemePreference() - darkMode: " + darkMode);
        if (email == null) return;
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                "UPDATE settings SET dark_mode = ? WHERE user_email = ?");
            stmt.setString(1, darkMode ? "true" : "false");
            stmt.setString(2, email);
            int updated = stmt.executeUpdate();
            System.out.println("[ThemeManager] Saved to DB - rows updated: " + updated);
            stmt.close();
            conn.close();
        } catch (Exception e) {
            System.out.println("[ThemeManager] Error saving: " + e.getMessage());
        }
    }

    public static void applyTheme(Stage stage) {
        System.out.println("\n========== [ThemeManager] applyTheme() START - isDarkMode: " + isDarkMode + " ==========");
        
        if (stage == null || stage.getScene() == null || stage.getScene().getRoot() == null) {
            System.out.println("[ThemeManager] Stage/Scene/Root is null!");
            return;
        }

        Parent root = stage.getScene().getRoot();
        
        if (isDarkMode) {
            // ✅ DARK MODE ON: Apply dark theme
            saveOriginalStyles(root);
            applyDarkThemeToNode(root);
            System.out.println("[ThemeManager] ✓ DARK MODE APPLIED");
        } else {
            // ✅ DARK MODE OFF: Revert to original light styles
            revertToLightMode(root);
            System.out.println("[ThemeManager] ✓ LIGHT MODE (no changes)");
        }
        
        System.out.println("========== [ThemeManager] applyTheme() END ==========\n");
    }

    // ✅ SAVE ORIGINAL LIGHT MODE STYLES BEFORE APPLYING DARK
    private static void saveOriginalStyles(Node node) {
        if (node == null) return;

        if (node instanceof Region || node instanceof Label) {
            String style = ((Region) node).getStyle();
            // Only save if not already saved
            if (!originalStyles.containsKey(node)) {
                originalStyles.put(node, style != null ? style : "");
            }
        }

        if (node instanceof Parent) {
            Parent parent = (Parent) node;
            for (Node child : parent.getChildrenUnmodifiable()) {
                saveOriginalStyles(child);
            }
        }
    }

    // ✅ REVERT TO ORIGINAL LIGHT MODE STYLES
    private static void revertToLightMode(Node node) {
        if (node == null) return;

        if (node instanceof Region || node instanceof Label) {
            String original = originalStyles.get(node);
            if (original != null) {
                // Restore to ORIGINAL saved style
                ((Region) node).setStyle(original);
            }
        }

        if (node instanceof Parent) {
            Parent parent = (Parent) node;
            for (Node child : parent.getChildrenUnmodifiable()) {
                revertToLightMode(child);
            }
        }
    }

    // ✅ APPLY DARK MODE TO ALL NODES
    private static void applyDarkThemeToNode(Node node) {
        if (node == null) return;

        // Handle charts - FORCE dark background
        if (node instanceof Chart) {
            Chart chart = (Chart) node;
            chart.setStyle(
                "-fx-background-color: " + DARK_BG_CHART + ";" +
                "-fx-text-fill: " + DARK_TEXT + ";");
        }

        // Handle table views - FORCE dark background
        else if (node instanceof TableView) {
            TableView<?> table = (TableView<?>) node;
            table.setStyle(
                "-fx-background-color: " + DARK_BG_SECONDARY + ";" +
                "-fx-control-inner-background: " + DARK_BG_SECONDARY + ";" +
                "-fx-text-fill: " + DARK_TEXT + ";");
        }

        // Apply theme to all Region types
        else if (node instanceof Region) {
            Region region = (Region) node;
            String style = region.getStyle();
            if (style != null && !style.isEmpty()) {
                style = convertToDarkMode(style);
                region.setStyle(style);
            }
        }

        // Apply text colors to labels
        else if (node instanceof Label) {
            Label label = (Label) node;
            String style = label.getStyle();
            if (style != null && !style.isEmpty()) {
                style = convertToDarkMode(style);
                label.setStyle(style);
            }
        }

        // Recursively apply to children
        if (node instanceof Parent) {
            Parent parent = (Parent) node;
            for (Node child : parent.getChildrenUnmodifiable()) {
                applyDarkThemeToNode(child);
            }
        }
    }

    // ✅ CONVERT LIGHT COLORS TO DARK
    private static String convertToDarkMode(String style) {
        if (style == null || style.isEmpty()) return style;

        // Backgrounds - convert ALL whites to dark
        style = style.replaceAll("(?i)#ffffff\\b", DARK_BG_SECONDARY);
        style = style.replaceAll("(?i)#f0f2f5\\b", DARK_BG_PRIMARY);
        style = style.replaceAll("(?i)#f8f9fa\\b", DARK_BG_TERTIARY);
        style = style.replaceAll("(?i)#f4f4f4\\b", DARK_BG_SECONDARY);
        style = style.replaceAll("(?i)#fafafa\\b", DARK_BG_CHART);
        style = style.replaceAll("(?i)#fafbfc\\b", DARK_BG_SECONDARY);
        style = style.replaceAll("(?i)#eeeeee\\b", DARK_BG_TERTIARY);
        style = style.replaceAll("(?i)#fafbff\\b", DARK_BG_SECONDARY);
        
        // Text - convert ALL dark text to light
        style = style.replaceAll("(?i)#333333\\b", DARK_TEXT);
        style = style.replaceAll("(?i)#1a1a1a\\b", DARK_TEXT);
        style = style.replaceAll("(?i)#000000\\b", DARK_TEXT);
        style = style.replaceAll("(?i)#222222\\b", DARK_TEXT);
        style = style.replaceAll("(?i)#111111\\b", DARK_TEXT);
        style = style.replaceAll("(?i)#555555\\b", DARK_TEXT_SECONDARY);
        style = style.replaceAll("(?i)#666666\\b", DARK_TEXT_SECONDARY);
        style = style.replaceAll("(?i)#777777\\b", DARK_TEXT_SECONDARY);
        style = style.replaceAll("(?i)#999999\\b", DARK_TEXT_SECONDARY);
        style = style.replaceAll("(?i)#aaaaaa\\b", DARK_TEXT_SECONDARY);
        style = style.replaceAll("(?i)#444444\\b", DARK_TEXT_SECONDARY);
        style = style.replaceAll("(?i)#bbbbbb\\b", DARK_TEXT_SECONDARY);
        style = style.replaceAll("(?i)#cccccc\\b", DARK_TEXT_SECONDARY);
        
        // Borders - convert light borders to dark
        style = style.replaceAll("(?i)#e8e8e8\\b", DARK_BORDER);
        style = style.replaceAll("(?i)#eeeeee\\b", DARK_BORDER);
        style = style.replaceAll("(?i)#f0f0f0\\b", DARK_BORDER);
        style = style.replaceAll("(?i)#e0e0e0\\b", DARK_BORDER);
        style = style.replaceAll("(?i)#dddddd\\b", DARK_BORDER);
        style = style.replaceAll("(?i)#ebebeb\\b", DARK_BORDER);
        
        return style;
    }

    public static void toggleTheme(Stage stage) {
        isDarkMode = !isDarkMode;
        saveThemePreference(isDarkMode);
        applyTheme(stage);
    }
}