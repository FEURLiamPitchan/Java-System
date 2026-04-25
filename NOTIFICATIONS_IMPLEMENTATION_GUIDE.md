# Notifications Implementation Guide for Remaining Controllers

## Controllers to Update:
1. Complaints_ResidentController.java
2. RequestDocumentController.java  
3. ResidentAnnouncementsController.java
4. MyProfileController.java
5. PaymentsController.java

## Step 1: Add Imports (at top of each controller)
```java
import javafx.scene.Scene;
import javafx.scene.text.Text;
import javafx.stage.Modality;
```

## Step 2: Add FXML Fields (after existing @FXML fields)
```java
@FXML
private Button alertsButton;
@FXML
private Label alertBadge;
```

## Step 3: Update initialize() Method
Add these two lines at the END of the initialize() method:
```java
ResidentNotifications.syncNotifications(UserSession.getCurrentUserEmail());
refreshAlertBadge();
```

## Step 4: Add These 4 Methods (copy entire block to end of class, before closing brace)

```java
private void refreshAlertBadge() {
    int count = ResidentNotifications.getUnreadCount(UserSession.getCurrentUserEmail());
    if (count > 0) {
        alertBadge.setText(count > 99 ? "99+" : String.valueOf(count));
        alertBadge.setVisible(true);
    } else {
        alertBadge.setVisible(false);
    }
}

@FXML
private void handleAlertsClick() {
    try {
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.initOwner(logoutButton.getScene().getWindow());
        popup.setTitle("Notifications");
        
        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: #f8f9fa;");
        
        VBox header = new VBox(4);
        header.setStyle("-fx-background-color: #2d2d2d; -fx-padding: 20;");
        Label titleLabel = new Label("Notifications");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");
        Label subtitleLabel = new Label("Stay updated with your requests and announcements");
        subtitleLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #aaaaaa;");
        header.getChildren().addAll(titleLabel, subtitleLabel);
        
        HBox filterBox = new HBox(8);
        filterBox.setStyle("-fx-padding: 12 20; -fx-background-color: #ffffff; -fx-border-color: #e8e8e8; -fx-border-width: 0 0 1 0;");
        Button unreadBtn = new Button("Unread");
        unreadBtn.setStyle("-fx-background-color: #2d2d2d; -fx-text-fill: #ffffff; -fx-font-size: 11px; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 6 12; -fx-cursor: hand;");
        Button allBtn = new Button("Past Notifications");
        allBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #555555; -fx-font-size: 11px; -fx-background-radius: 6; -fx-padding: 6 12; -fx-cursor: hand;");
        filterBox.getChildren().addAll(unreadBtn, allBtn);
        
        VBox notifList = new VBox(0);
        ScrollPane scrollPane = new ScrollPane(notifList);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: #f8f9fa; -fx-background-color: #f8f9fa;");
        scrollPane.setPrefHeight(400);
        
        loadNotifications(notifList, false);
        
        unreadBtn.setOnAction(e -> {
            unreadBtn.setStyle("-fx-background-color: #2d2d2d; -fx-text-fill: #ffffff; -fx-font-size: 11px; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 6 12; -fx-cursor: hand;");
            allBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #555555; -fx-font-size: 11px; -fx-background-radius: 6; -fx-padding: 6 12; -fx-cursor: hand;");
            loadNotifications(notifList, false);
        });
        
        allBtn.setOnAction(e -> {
            allBtn.setStyle("-fx-background-color: #2d2d2d; -fx-text-fill: #ffffff; -fx-font-size: 11px; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 6 12; -fx-cursor: hand;");
            unreadBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #555555; -fx-font-size: 11px; -fx-background-radius: 6; -fx-padding: 6 12; -fx-cursor: hand;");
            loadNotifications(notifList, true);
        });
        
        HBox footer = new HBox();
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setStyle("-fx-padding: 12 20; -fx-background-color: #ffffff; -fx-border-color: #e8e8e8; -fx-border-width: 1 0 0 0;");
        Button closeBtn = new Button("Close");
        closeBtn.setStyle("-fx-background-color: #2d2d2d; -fx-text-fill: #ffffff; -fx-font-size: 12px; -fx-background-radius: 6; -fx-padding: 8 16; -fx-cursor: hand;");
        closeBtn.setOnAction(e -> {
            popup.close();
            refreshAlertBadge();
        });
        footer.getChildren().add(closeBtn);
        
        root.getChildren().addAll(header, filterBox, scrollPane, footer);
        
        Scene scene = new Scene(root, 480, 550);
        popup.setScene(scene);
        popup.setResizable(false);
        popup.showAndWait();
        
    } catch (Exception e) {
        e.printStackTrace();
    }
}

private void loadNotifications(VBox container, boolean showAll) {
    container.getChildren().clear();
    
    try {
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return;
        
        String sql = "SELECT notif_id, type, message, reference_id, is_read, created_at FROM notifications WHERE user_email = ?";
        if (!showAll) {
            sql += " AND is_read = 'false'";
        }
        sql += " ORDER BY notif_id DESC";
        
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, UserSession.getCurrentUserEmail());
        ResultSet rs = stmt.executeQuery();
        
        boolean hasData = false;
        while (rs.next()) {
            hasData = true;
            int notifId = rs.getInt("notif_id");
            String type = rs.getString("type");
            String message = rs.getString("message");
            String refId = rs.getString("reference_id");
            boolean isRead = rs.getString("is_read").equals("true");
            String createdAt = rs.getString("created_at");
            
            HBox item = buildNotifItem(notifId, type, message, refId, isRead, createdAt);
            container.getChildren().add(item);
        }
        
        if (!hasData) {
            Label emptyLabel = new Label(showAll ? "No notifications yet" : "No unread notifications");
            emptyLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #888888; -fx-padding: 40;");
            VBox emptyBox = new VBox(emptyLabel);
            emptyBox.setAlignment(Pos.CENTER);
            container.getChildren().add(emptyBox);
        }
        
        rs.close();
        stmt.close();
        conn.close();
        
    } catch (Exception e) {
        e.printStackTrace();
    }
}

private HBox buildNotifItem(int notifId, String type, String message, String refId, boolean isRead, String createdAt) {
    HBox item = new HBox(12);
    item.setAlignment(Pos.CENTER_LEFT);
    item.setStyle("-fx-padding: 16 20; -fx-background-color: " + (isRead ? "#ffffff" : "#fafbff") + "; -fx-border-color: #e8e8e8; -fx-border-width: 0 0 1 0; -fx-cursor: hand;");
    
    String icon = "";
    String iconBg = "";
    if (type.equals("document")) {
        icon = "📄";
        iconBg = "#e3f2fd";
    } else if (type.equals("complaint")) {
        icon = "📢";
        iconBg = "#ffebee";
    } else if (type.equals("announcement")) {
        icon = "📣";
        iconBg = "#fff8e1";
    }
    
    Label iconLabel = new Label(icon);
    iconLabel.setStyle("-fx-font-size: 20px; -fx-background-color: " + iconBg + "; -fx-background-radius: 8; -fx-padding: 8; -fx-min-width: 40; -fx-min-height: 40; -fx-alignment: center;");
    
    VBox content = new VBox(4);
    content.setAlignment(Pos.CENTER_LEFT);
    HBox.setHgrow(content, Priority.ALWAYS);
    
    Label messageLabel = new Label(message);
    messageLabel.setWrapText(true);
    messageLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #1a1a1a;" + (isRead ? "" : " -fx-font-weight: bold;"));
    
    Label timeLabel = new Label(createdAt);
    timeLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #888888;");
    
    content.getChildren().addAll(messageLabel, timeLabel);
    
    VBox indicator = new VBox();
    indicator.setAlignment(Pos.CENTER);
    if (!isRead) {
        Label dot = new Label("•");
        dot.setStyle("-fx-font-size: 20px; -fx-text-fill: #2196f3;");
        indicator.getChildren().add(dot);
    } else {
        Label readBadge = new Label("Read");
        readBadge.setStyle("-fx-font-size: 9px; -fx-text-fill: #888888; -fx-background-color: #f0f0f0; -fx-background-radius: 4; -fx-padding: 2 6;");
        indicator.getChildren().add(readBadge);
    }
    
    item.getChildren().addAll(iconLabel, content, indicator);
    
    item.setOnMouseClicked(e -> showNotifDetail(notifId, type, message, refId, isRead));
    
    return item;
}

private void showNotifDetail(int notifId, String type, String message, String refId, boolean isRead) {
    try {
        Stage detailPopup = new Stage();
        detailPopup.initModality(Modality.APPLICATION_MODAL);
        detailPopup.initOwner(logoutButton.getScene().getWindow());
        detailPopup.setTitle("Notification Detail");
        
        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: #f8f9fa;");
        
        VBox header = new VBox(4);
        header.setStyle("-fx-background-color: #2d2d2d; -fx-padding: 20;");
        String headerTitle = type.equals("document") ? "Document Update" : type.equals("complaint") ? "Complaint Update" : "Announcement";
        Label titleLabel = new Label(headerTitle);
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");
        header.getChildren().add(titleLabel);
        
        VBox content = new VBox(16);
        content.setStyle("-fx-padding: 24; -fx-background-color: #ffffff;");
        
        String icon = type.equals("document") ? "📄" : type.equals("complaint") ? "📢" : "📣";
        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 32px;");
        
        Text messageText = new Text(message);
        messageText.setWrappingWidth(360);
        messageText.setStyle("-fx-font-size: 13px; -fx-fill: #333333;");
        
        Button goToBtn = new Button("→ Go to " + (type.equals("document") ? "My Documents" : type.equals("complaint") ? "My Complaints" : "Announcements"));
        goToBtn.setStyle("-fx-background-color: #2d2d2d; -fx-text-fill: #ffffff; -fx-font-size: 12px; -fx-background-radius: 6; -fx-padding: 10 16; -fx-cursor: hand;");
        goToBtn.setOnAction(e -> {
            ResidentNotifications.markAsRead(notifId);
            detailPopup.close();
            if (type.equals("document")) {
                goToMyDocuments();
            } else if (type.equals("complaint")) {
                goToComplaints();
            } else if (type.equals("announcement")) {
                goToAnnouncements();
            }
        });
        
        content.getChildren().addAll(iconLabel, messageText, goToBtn);
        
        HBox footer = new HBox(8);
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setStyle("-fx-padding: 12 20; -fx-background-color: #ffffff; -fx-border-color: #e8e8e8; -fx-border-width: 1 0 0 0;");
        
        Button closeBtn = new Button("Close");
        closeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #555555; -fx-font-size: 12px; -fx-background-radius: 6; -fx-border-color: #e0e0e0; -fx-border-width: 1; -fx-padding: 8 16; -fx-cursor: hand;");
        closeBtn.setOnAction(e -> detailPopup.close());
        
        footer.getChildren().add(closeBtn);
        
        if (!isRead) {
            Button markReadBtn = new Button("Mark as Read");
            markReadBtn.setStyle("-fx-background-color: #2d2d2d; -fx-text-fill: #ffffff; -fx-font-size: 12px; -fx-background-radius: 6; -fx-padding: 8 16; -fx-cursor: hand;");
            markReadBtn.setOnAction(e -> {
                ResidentNotifications.markAsRead(notifId);
                detailPopup.close();
                refreshAlertBadge();
            });
            footer.getChildren().add(markReadBtn);
        }
        
        root.getChildren().addAll(header, content, footer);
        
        Scene scene = new Scene(root, 420, 320);
        detailPopup.setScene(scene);
        detailPopup.setResizable(false);
        detailPopup.showAndWait();
        
    } catch (Exception e) {
        e.printStackTrace();
    }
}
```

## Step 5: Update FXML Files
Add this to the top bar in each FXML (replace existing Alerts button):
```xml
<StackPane>
    <Button fx:id="alertsButton" text="🔔  Alerts" onAction="#handleAlertsClick"
            style="-fx-background-color: #f4f4f4;
                   -fx-text-fill: #333333;
                   -fx-font-size: 12px;
                   -fx-background-radius: 20;
                   -fx-border-radius: 20;
                   -fx-border-color: #e0e0e0;
                   -fx-border-width: 1;
                   -fx-padding: 8 16;
                   -fx-cursor: hand;"/>
    <Label fx:id="alertBadge" text="0"
           visible="false"
           style="-fx-background-color: #e53935;
                  -fx-text-fill: #ffffff;
                  -fx-font-size: 9px;
                  -fx-font-weight: bold;
                  -fx-background-radius: 10;
                  -fx-padding: 2 5;
                  -fx-min-width: 16;
                  -fx-min-height: 16;"
           StackPane.alignment="TOP_RIGHT"/>
</StackPane>
```

## Controllers Already Updated:
✅ ResidentDashboardController.java
✅ MyDocumentsController.java

## Controllers Still Need Update:
- Complaints_ResidentController.java
- RequestDocumentController.java
- ResidentAnnouncementsController.java
- MyProfileController.java
- PaymentsController.java
