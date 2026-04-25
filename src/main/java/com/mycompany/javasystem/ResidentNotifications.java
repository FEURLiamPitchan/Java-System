package com.mycompany.javasystem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ResidentNotifications {
    
    public static void syncNotifications(String userEmail) {
        // This method would sync notifications from the server
        // For demo purposes, we'll create some sample notifications if none exist
        try {
            Connection conn = DatabaseConnection.getConnection();
            if (conn == null) return;
            
            // Check if user has any notifications
            PreparedStatement checkStmt = conn.prepareStatement(
                "SELECT COUNT(*) FROM notifications WHERE user_email = ?");
            checkStmt.setString(1, userEmail);
            ResultSet rs = checkStmt.executeQuery();
            
            if (rs.next() && rs.getInt(1) == 0) {
                // Create sample notifications for demo
                createSampleNotifications(conn, userEmail);
            }
            
            rs.close();
            checkStmt.close();
            conn.close();
            
        } catch (Exception e) {
            System.err.println("Error syncing notifications: " + e.getMessage());
        }
    }
    
    private static void createSampleNotifications(Connection conn, String userEmail) {
        try {
            String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            
            PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO notifications (type, message, reference_id, is_read, created_at, user_email) VALUES (?,?,?,?,?,?)");
            
            // Sample document notification
            stmt.setString(1, "document");
            stmt.setString(2, "Your Barangay Clearance request has been approved and is ready for pickup.");
            stmt.setString(3, "REQ-2024-001");
            stmt.setString(4, "false");
            stmt.setString(5, currentTime);
            stmt.setString(6, userEmail);
            stmt.executeUpdate();
            
            // Sample announcement notification
            stmt.setString(1, "announcement");
            stmt.setString(2, "New community health program starting next week. Register now!");
            stmt.setString(3, "ANN-2024-001");
            stmt.setString(4, "false");
            stmt.setString(5, currentTime);
            stmt.setString(6, userEmail);
            stmt.executeUpdate();
            
            stmt.close();
            
        } catch (Exception e) {
            System.err.println("Error creating sample notifications: " + e.getMessage());
        }
    }
    
    public static int getUnreadCount(String userEmail) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            if (conn == null) return 0;
            
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT COUNT(*) FROM notifications WHERE user_email = ? AND is_read = 'false'");
            stmt.setString(1, userEmail);
            ResultSet rs = stmt.executeQuery();
            
            int count = 0;
            if (rs.next()) {
                count = rs.getInt(1);
            }
            
            rs.close();
            stmt.close();
            conn.close();
            
            return count;
            
        } catch (Exception e) {
            System.err.println("Error getting unread count: " + e.getMessage());
            return 0;
        }
    }
    
    public static void markAsRead(int notificationId) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            if (conn == null) return;
            
            PreparedStatement stmt = conn.prepareStatement(
                "UPDATE notifications SET is_read = 'true' WHERE notif_id = ?");
            stmt.setInt(1, notificationId);
            stmt.executeUpdate();
            stmt.close();
            conn.close();
            
        } catch (Exception e) {
            System.err.println("Error marking notification as read: " + e.getMessage());
        }
    }
    
    public static void createNotification(String userEmail, String type, String message, String referenceId) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            if (conn == null) return;
            
            PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO notifications (type, message, reference_id, is_read, created_at, user_email) VALUES (?,?,?,?,?,?)");
            stmt.setString(1, type);
            stmt.setString(2, message);
            stmt.setString(3, referenceId);
            stmt.setString(4, "false");
            stmt.setString(5, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            stmt.setString(6, userEmail);
            stmt.executeUpdate();
            stmt.close();
            conn.close();
            
        } catch (Exception e) {
            System.err.println("Error creating notification: " + e.getMessage());
        }
    }
}