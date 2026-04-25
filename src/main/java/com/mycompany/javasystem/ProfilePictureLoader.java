package com.mycompany.javasystem;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Circle;
import javafx.scene.control.Label;

import java.io.ByteArrayInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Base64;

public class ProfilePictureLoader {

    /**
     * Load and display user's profile picture in avatar
     * @param email User email
     * @param avatarContainer The container with Circle and ImageView
     * @param avatarCircle The Circle background
     * @param profileImageView The ImageView to display picture
     * @param initialsLabel The label to show initials if no picture
     */
    public static void loadAvatarPicture(String email, 
                                          HBox avatarContainer,
                                          Circle avatarCircle,
                                          ImageView profileImageView,
                                          Label initialsLabel) {
        if (email == null || email.isEmpty()) return;

        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT full_name, profile_picture FROM users WHERE email = ?");
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String name = rs.getString("full_name");
                String pic = rs.getString("profile_picture");

                if (pic != null && !pic.isEmpty()) {
                    // Display profile picture
                    displayProfileImage(pic, avatarCircle, profileImageView, initialsLabel);
                } else {
                    // Show initials
                    showInitials(name, avatarCircle, profileImageView, initialsLabel);
                }
            }
            rs.close();
            stmt.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Display profile image in avatar
     */
    private static void displayProfileImage(String base64,
                                             Circle avatarCircle,
                                             ImageView profileImageView,
                                             Label initialsLabel) {
        try {
            byte[] bytes = Base64.getDecoder().decode(base64);
            Image img = new Image(new ByteArrayInputStream(bytes));
            
            // Create circular clip
            Circle clip = new Circle(14, 14, 14);
            profileImageView.setClip(clip);
            profileImageView.setImage(img);
            profileImageView.setVisible(true);
            
            // Hide initials
            if (avatarCircle != null) avatarCircle.setVisible(false);
            if (initialsLabel != null) initialsLabel.setVisible(false);
        } catch (Exception e) {
            e.printStackTrace();
            showInitials(null, avatarCircle, profileImageView, initialsLabel);
        }
    }

    /**
     * Show initials when no profile picture
     */
    private static void showInitials(String name,
                                      Circle avatarCircle,
                                      ImageView profileImageView,
                                      Label initialsLabel) {
        if (profileImageView != null) {
            profileImageView.setVisible(false);
        }
        if (avatarCircle != null) {
            avatarCircle.setVisible(true);
            // Reset to solid color fill
            avatarCircle.setFill(javafx.scene.paint.Color.web("#2d2d2d"));
        }
        if (initialsLabel != null) {
            initialsLabel.setVisible(true);
            String initial = "R";
            if (name != null && !name.isEmpty()) {
                String[] parts = name.trim().split(" ");
                if (parts.length >= 2) {
                    // First and last name initials
                    initial = String.valueOf(parts[0].charAt(0)).toUpperCase() + 
                             String.valueOf(parts[parts.length - 1].charAt(0)).toUpperCase();
                } else {
                    // Just first letter
                    initial = String.valueOf(name.charAt(0)).toUpperCase();
                }
            }
            initialsLabel.setText(initial);
        }
    }
    
    /**
     * Legacy method for backward compatibility with Circle and Label only
     * Now properly loads and displays profile pictures
     */
    public static void loadProfilePicture(Circle avatarCircle, Label initialsLabel, String userEmail) {
        if (userEmail == null || userEmail.isEmpty()) {
            showInitials("R", avatarCircle, null, initialsLabel);
            return;
        }
        
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT full_name, profile_picture FROM users WHERE email = ?");
            stmt.setString(1, userEmail);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                String name = rs.getString("full_name");
                String profilePicture = rs.getString("profile_picture");
                
                // If profile picture exists, display it
                if (profilePicture != null && !profilePicture.isEmpty()) {
                    try {
                        byte[] imageBytes = Base64.getDecoder().decode(profilePicture);
                        Image image = new Image(new ByteArrayInputStream(imageBytes));
                        
                        // Set image as fill pattern for the circle
                        javafx.scene.paint.ImagePattern imagePattern = new javafx.scene.paint.ImagePattern(image);
                        avatarCircle.setFill(imagePattern);
                        
                        // Hide initials label
                        if (initialsLabel != null) {
                            initialsLabel.setVisible(false);
                        }
                    } catch (Exception e) {
                        System.err.println("Error loading profile picture: " + e.getMessage());
                        // Fall back to initials if image loading fails
                        showInitials(name, avatarCircle, null, initialsLabel);
                    }
                } else {
                    // No profile picture, show initials
                    showInitials(name, avatarCircle, null, initialsLabel);
                }
            } else {
                showInitials("R", avatarCircle, null, initialsLabel);
            }
            
            rs.close();
            stmt.close();
            conn.close();
        } catch (Exception e) {
            System.err.println("Error in loadProfilePicture: " + e.getMessage());
            showInitials("R", avatarCircle, null, initialsLabel);
        }
    }
}
