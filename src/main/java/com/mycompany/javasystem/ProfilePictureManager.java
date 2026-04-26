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

public class ProfilePictureManager {

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
        profileImageView.setVisible(false);
        if (avatarCircle != null) avatarCircle.setVisible(true);
        if (initialsLabel != null) {
            initialsLabel.setVisible(true);
            String initial = (name != null && !name.isEmpty())
                ? String.valueOf(name.charAt(0)).toUpperCase()
                : "A";
            initialsLabel.setText(initial);
        }
    }
}