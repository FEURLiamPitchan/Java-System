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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThemeManager {
    
    public static boolean isDarkMode = false;
    private static Stage primaryStage = null;
    private static Stage settingsStage = null;
    
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
    // Track which nodes have been styled to avoid duplication
    private static final Map<Node, Boolean> styledNodes = new HashMap<>();
    
    // Background executor for DB operations
    private static final ExecutorService executorService = Executors.newFixedThreadPool(1);

    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
        System.out.println("[ThemeManager] setPrimaryStage() - stage: " + stage);
    }

    public static void setSettingsStage(Stage stage) {
        settingsStage = stage;
        System.out.println("[ThemeManager] setSettingsStage() - stage: " + stage);
    }

    // ✅ LOAD THEME FROM DATABASE
    public static void loadThemePreference() {
        String email = SessionManager.getEmail();
        System.out.println("[ThemeManager] loadThemePreference() - email: " + email);
        if (email == null) {
            isDarkMode = false;
            System.out.println("[ThemeManager] ⚠️  Email is null, setting isDarkMode = false");
            return;
        }
        try {
            Connection conn = DatabaseConnection.getConnection();
            
            // First, get user ID from email
            PreparedStatement userStmt = conn.prepareStatement("SELECT id FROM users WHERE email = ?");
            userStmt.setString(1, email);
            ResultSet userRs = userStmt.executeQuery();
            int userId = -1;
            if (userRs.next()) userId = userRs.getInt("id");
            userRs.close();
            userStmt.close();
            
            if (userId == -1) {
                System.out.println("[ThemeManager] ⚠️  User not found for email: " + email);
                isDarkMode = false;
                conn.close();
                return;
            }
            
            // Now get the settings for this user
            PreparedStatement settingsStmt = conn.prepareStatement(
                "SELECT dark_mode FROM settings WHERE user_id = ?");
            settingsStmt.setInt(1, userId);
            ResultSet rs = settingsStmt.executeQuery();
            if (rs.next()) {
                String dm = rs.getString("dark_mode");
                isDarkMode = "true".equalsIgnoreCase(dm);
                System.out.println("[ThemeManager] ✅ Loaded from DB - dark_mode: " + dm + " -> isDarkMode: " + isDarkMode);
            } else {
                isDarkMode = false;
                System.out.println("[ThemeManager] ⚠️  No settings found for user ID: " + userId);
            }
            rs.close();
            settingsStmt.close();
            conn.close();
        } catch (Exception e) {
            System.out.println("[ThemeManager] Error loading theme: " + e.getMessage());
            e.printStackTrace();
            isDarkMode = false;
        }
    }

    // ✅ SAVE THEME TO DATABASE
    public static void saveThemePreference(boolean darkMode) {
        isDarkMode = darkMode;
        String email = SessionManager.getEmail();
        System.out.println("[ThemeManager] saveThemePreference() - darkMode: " + darkMode + ", email: " + email);
        
        if (email == null) {
            System.out.println("[ThemeManager] ⚠️  Email is null, cannot save");
            return;
        }
        
        // Save in background to avoid UI freeze
        executorService.execute(() -> {
            try {
                Connection conn = DatabaseConnection.getConnection();
                
                // Get user ID
                PreparedStatement userStmt = conn.prepareStatement("SELECT id FROM users WHERE email = ?");
                userStmt.setString(1, email);
                ResultSet userRs = userStmt.executeQuery();
                int userId = -1;
                if (userRs.next()) userId = userRs.getInt("id");
                userRs.close();
                userStmt.close();
                
                if (userId == -1) {
                    System.out.println("[ThemeManager] ⚠️  User not found");
                    conn.close();
                    return;
                }
                
                // Update settings
                PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE settings SET dark_mode = ? WHERE user_id = ?");
                stmt.setString(1, darkMode ? "true" : "false");
                stmt.setInt(2, userId);
                int updated = stmt.executeUpdate();
                System.out.println("[ThemeManager] ✅ Saved to DB - rows updated: " + updated);
                stmt.close();
                
                // Verify it was saved
                PreparedStatement verifyStmt = conn.prepareStatement(
                    "SELECT dark_mode FROM settings WHERE user_id = ?");
                verifyStmt.setInt(1, userId);
                ResultSet verifyRs = verifyStmt.executeQuery();
                if (verifyRs.next()) {
                    String savedValue = verifyRs.getString("dark_mode");
                    System.out.println("[ThemeManager] ✅ Verified saved value: " + savedValue);
                }
                verifyRs.close();
                verifyStmt.close();
                
                conn.close();
            } catch (Exception e) {
                System.out.println("[ThemeManager] Error saving theme: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    // ✅ MAIN METHOD - Apply theme to all stages
    public static void applyTheme(Stage stage) {
        System.out.println("\n========== [ThemeManager] applyTheme() - isDarkMode: " + isDarkMode + " ==========");
        
        if (stage == null || stage.getScene() == null || stage.getScene().getRoot() == null) {
            System.out.println("[ThemeManager] ⚠️  Stage/Scene/Root is null!");
            return;
        }

        Parent root = stage.getScene().getRoot();
        
        if (isDarkMode) {
            System.out.println("[ThemeManager] 🌙 Applying DARK MODE");
            saveOriginalStyles(root);
            applyDarkThemeToNode(root);
        } else {
            System.out.println("[ThemeManager] ☀️  Reverting to LIGHT MODE");
            revertToLightMode(root);
            clearStyledNodeCache();
        }
        
        System.out.println("========== [ThemeManager] applyTheme() END ==========\n");
    }

    // ✅ Apply to PRIMARY STAGE only
    public static void applyThemeToPrimaryStage() {
        if (primaryStage != null && primaryStage.isShowing()) {
            System.out.println("[ThemeManager] Applying theme to PRIMARY stage");
            applyTheme(primaryStage);
        }
    }

    // ✅ Apply to SETTINGS STAGE only
    public static void applyThemeToSettingsStage() {
        if (settingsStage != null && settingsStage.isShowing()) {
            System.out.println("[ThemeManager] Applying theme to SETTINGS stage");
            applyTheme(settingsStage);
        }
    }

    // ✅ Apply to ALL STAGES (main + settings)
    public static void applyThemeToAllStages() {
        System.out.println("[ThemeManager] Applying theme to ALL stages");
        applyThemeToPrimaryStage();
        applyThemeToSettingsStage();
    }

    // ✅ SAVE ORIGINAL LIGHT MODE STYLES BEFORE APPLYING DARK
    private static void saveOriginalStyles(Node node) {
        if (node == null) return;

        try {
            if (node instanceof Region || node instanceof Label || node instanceof TableView || node instanceof Chart) {
                // Only save once per node
                if (!originalStyles.containsKey(node)) {
                    String style = "";
                    if (node instanceof Region) {
                        style = ((Region) node).getStyle();
                    } else if (node instanceof Label) {
                        style = ((Label) node).getStyle();
                    }
                    originalStyles.put(node, style != null ? style : "");
                }
            }

            if (node instanceof Parent) {
                Parent parent = (Parent) node;
                for (Node child : parent.getChildrenUnmodifiable()) {
                    saveOriginalStyles(child);
                }
            }
        } catch (Exception e) {
            System.out.println("[ThemeManager] Error saving original styles: " + e.getMessage());
        }
    }

    // ✅ REVERT TO ORIGINAL LIGHT MODE STYLES
    private static void revertToLightMode(Node node) {
        if (node == null) return;

        try {
            if (node instanceof Region || node instanceof Label || node instanceof TableView || node instanceof Chart) {
                String original = originalStyles.get(node);
                if (original != null) {
                    if (node instanceof Region) {
                        ((Region) node).setStyle(original);
                    } else if (node instanceof Label) {
                        ((Label) node).setStyle(original);
                    }
                    styledNodes.put(node, false);
                }
            }

            if (node instanceof Parent) {
                Parent parent = (Parent) node;
                for (Node child : parent.getChildrenUnmodifiable()) {
                    revertToLightMode(child);
                }
            }
        } catch (Exception e) {
            System.out.println("[ThemeManager] Error reverting to light mode: " + e.getMessage());
        }
    }

    // ✅ APPLY DARK MODE TO ALL NODES
    private static void applyDarkThemeToNode(Node node) {
        if (node == null) return;

        try {
            // Skip if already styled in dark mode
            if (styledNodes.getOrDefault(node, false)) {
                return;
            }

            // Handle charts - FORCE dark background
            if (node instanceof Chart) {
                Chart chart = (Chart) node;
                chart.setStyle(
                    "-fx-background-color: " + DARK_BG_CHART + ";" +
                    "-fx-text-fill: " + DARK_TEXT + ";");
                styledNodes.put(node, true);
            }

            // Handle table views - FORCE dark background
            else if (node instanceof TableView) {
                TableView<?> table = (TableView<?>) node;
                table.setStyle(
                    "-fx-background-color: " + DARK_BG_SECONDARY + ";" +
                    "-fx-control-inner-background: " + DARK_BG_SECONDARY + ";" +
                    "-fx-text-fill: " + DARK_TEXT + ";");
                styledNodes.put(node, true);
            }

            // Apply theme to all Region types
            else if (node instanceof Region) {
                Region region = (Region) node;
                String style = region.getStyle();
                if (style != null && !style.isEmpty()) {
                    style = convertToDarkMode(style);
                    region.setStyle(style);
                    styledNodes.put(node, true);
                }
            }

            // Apply text colors to labels
            else if (node instanceof Label) {
                Label label = (Label) node;
                String style = label.getStyle();
                if (style != null && !style.isEmpty()) {
                    style = convertToDarkMode(style);
                    label.setStyle(style);
                    styledNodes.put(node, true);
                }
            }
        } catch (Exception e) {
            System.out.println("[ThemeManager] Error styling node: " + e.getMessage());
        }

        // Recursively apply to children
        if (node instanceof Parent) {
            Parent parent = (Parent) node;
            try {
                for (Node child : parent.getChildrenUnmodifiable()) {
                    applyDarkThemeToNode(child);
                }
            } catch (Exception e) {
                System.out.println("[ThemeManager] Error applying theme to children: " + e.getMessage());
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
        style = style.replaceAll("(?i)#f8f8f8\\b", DARK_BG_TERTIARY);
        
        // Text - convert ALL dark text to light
        style = style.replaceAll("(?i)#333333\\b", DARK_TEXT);
        style = style.replaceAll("(?i)#1a1a1a\\b", DARK_TEXT);
        style = style.replaceAll("(?i)#000000\\b", DARK_TEXT);
        style = style.replaceAll("(?i)#222222\\b", DARK_TEXT);
        style = style.replaceAll("(?i)#111111\\b", DARK_TEXT);
        style = style.replaceAll("(?i)#2d2d2d\\b", DARK_TEXT);
        style = style.replaceAll("(?i)#0d0d0d\\b", DARK_TEXT);
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

    // ✅ CLEAR CACHE when switching modes
    private static void clearStyledNodeCache() {
        styledNodes.clear();
    }

    // ✅ TOGGLE THEME - handles both stages
    public static void toggleTheme(Stage currentStage) {
        isDarkMode = !isDarkMode;
        System.out.println("\n[ThemeManager] ========== TOGGLING THEME ==========");
        System.out.println("[ThemeManager] isDarkMode now: " + isDarkMode);
        
        // Save preference to DB
        saveThemePreference(isDarkMode);
        
        // Apply theme to all stages
        applyThemeToAllStages();
        
        System.out.println("[ThemeManager] ========== THEME TOGGLE COMPLETE ==========\n");
    }

    // ✅ SYNC DARK MODE FROM SETTINGS TO ALL PAGES
    public static void syncThemeFromSettings(boolean newDarkModeState) {
        System.out.println("\n[ThemeManager] ========== SYNCING THEME FROM SETTINGS ==========");
        System.out.println("[ThemeManager] newDarkModeState: " + newDarkModeState);
        
        isDarkMode = newDarkModeState;
        
        // Save to DB
        saveThemePreference(isDarkMode);
        
        // Apply to all stages
        applyThemeToAllStages();
        
        System.out.println("[ThemeManager] ========== THEME SYNC COMPLETE ==========\n");
    }
}