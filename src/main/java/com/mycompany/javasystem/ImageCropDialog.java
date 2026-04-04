package com.mycompany.javasystem;

import javafx.geometry.Bounds;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.ByteArrayInputStream;
import java.util.Base64;

public class ImageCropDialog {

    private Image originalImage;
    private double cropX = 50;
    private double cropY = 50;
    private double cropSize = 200;
    private Canvas canvas;
    private String resultBase64 = null;
    
    // Canvas offset (for centered image)
    private double offsetX = 0;
    private double offsetY = 0;
    private double displayW = 0;
    private double displayH = 0;
    private double scale = 1.0;
    
    // Mouse dragging
    private boolean isDragging = false;
    private double dragStartX = 0;
    private double dragStartY = 0;
    private double dragOffsetX = 0;
    private double dragOffsetY = 0;
    private int dragHandle = -1; 
    // -1 = move center
    // 0 = top-left, 1 = top-center, 2 = top-right
    // 3 = middle-left, 4 = middle-right
    // 5 = bottom-left, 6 = bottom-center, 7 = bottom-right

    public String showCropDialog(Stage owner, String imageBase64) {
        resultBase64 = null;

        try {
            byte[] bytes = Base64.getDecoder().decode(imageBase64);
            originalImage = new Image(new ByteArrayInputStream(bytes));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(owner);
        dialog.setTitle("Crop Profile Picture");
        dialog.setResizable(false);
        dialog.setWidth(620);
        dialog.setHeight(580);

        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: #ffffff;");

        // Header
        VBox header = new VBox(6);
        header.setStyle("-fx-background-color: #1a1a1a; -fx-padding: 20 24;");
        Label titleLbl = new Label("Crop Your Profile Picture");
        titleLbl.setStyle(
            "-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");
        Label subLbl = new Label("Drag the crop box to adjust • Drag edges or corners to resize");
        subLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #aaaaaa;");
        header.getChildren().addAll(titleLbl, subLbl);

        // Canvas
        canvas = new Canvas(560, 320);
        canvas.setStyle("-fx-border-color: #e0e0e0; -fx-border-width: 1;");
        
        canvas.setOnMousePressed(this::handleMousePressed);
        canvas.setOnMouseDragged(this::handleMouseDragged);
        canvas.setOnMouseReleased(this::handleMouseReleased);
        canvas.setOnMouseMoved(this::handleMouseMoved);

        // Canvas container
        VBox canvasContainer = new VBox();
        canvasContainer.setStyle(
            "-fx-alignment: CENTER; -fx-padding: 20 24; -fx-background-color: #f8f9fa;");
        canvasContainer.getChildren().add(canvas);

        // Info label
        Label infoLabel = new Label("💡 Drag center to move • Drag edges/corners to resize • Scroll to zoom");
        infoLabel.setStyle(
            "-fx-font-size: 11px; -fx-text-fill: #777777; -fx-padding: 12 24 0 24;");

        // Footer
        HBox footer = new HBox(16);
        footer.setStyle(
            "-fx-padding: 20 24 24 24; -fx-alignment: CENTER_RIGHT;" +
            "-fx-border-color: #f0f0f0; -fx-border-width: 1 0 0 0;");

        Button resetBtn = new Button("Reset");
        resetBtn.setStyle(
            "-fx-background-color: #f4f4f4; -fx-text-fill: #555555;" +
            "-fx-font-size: 12px; -fx-font-weight: bold;" +
            "-fx-background-radius: 8; -fx-border-color: #e0e0e0;" +
            "-fx-border-width: 1; -fx-padding: 10 20; -fx-cursor: hand;");
        resetBtn.setOnAction(e -> {
            cropX = 50;
            cropY = 50;
            cropSize = 200;
            redrawCanvas();
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setStyle(
            "-fx-background-color: #ffffff; -fx-text-fill: #555555;" +
            "-fx-font-size: 12px; -fx-font-weight: bold;" +
            "-fx-background-radius: 8; -fx-border-color: #e0e0e0;" +
            "-fx-border-width: 1; -fx-padding: 10 20; -fx-cursor: hand;");
        cancelBtn.setOnAction(e -> {
            resultBase64 = null;
            dialog.close();
        });

        Button saveBtn = new Button("Crop & Save");
        saveBtn.setStyle(
            "-fx-background-color: #1a1a1a; -fx-text-fill: #ffffff;" +
            "-fx-font-size: 12px; -fx-font-weight: bold;" +
            "-fx-background-radius: 8; -fx-padding: 10 24; -fx-cursor: hand;");
        saveBtn.setOnAction(e -> {
            System.out.println("[ImageCropDialog] Crop & Save clicked");
            System.out.println("[ImageCropDialog] cropX: " + cropX + ", cropY: " + cropY + ", cropSize: " + cropSize);
            cropImage();
            System.out.println("[ImageCropDialog] resultBase64: " + (resultBase64 != null && !resultBase64.isEmpty() ? "✓ Generated" : "✗ Failed"));
            dialog.close();
        });

        footer.getChildren().addAll(resetBtn, spacer, cancelBtn, saveBtn);

        root.getChildren().addAll(
            header,
            new Separator(),
            canvasContainer,
            infoLabel,
            footer
        );

        redrawCanvas();

        dialog.setScene(new Scene(root));
        dialog.showAndWait();

        System.out.println("[ImageCropDialog] Dialog closed, returning: " + (resultBase64 != null ? "base64" : "null"));
        return resultBase64;
    }

    private void redrawCanvas() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        double canvasW = canvas.getWidth();
        double canvasH = canvas.getHeight();

        // Background
        gc.setFill(Color.web("#f8f9fa"));
        gc.fillRect(0, 0, canvasW, canvasH);

        if (originalImage == null) return;

        // Calculate scaling to fit image in canvas
        double imgW = originalImage.getWidth();
        double imgH = originalImage.getHeight();
        double scaleX = canvasW / imgW;
        double scaleY = canvasH / imgH;
        scale = Math.min(scaleX, scaleY);

        displayW = imgW * scale;
        displayH = imgH * scale;
        offsetX = (canvasW - displayW) / 2;
        offsetY = (canvasH - displayH) / 2;

        // Draw image centered
        gc.drawImage(originalImage, offsetX, offsetY, displayW, displayH);

        // Constrain crop box
        double minX = offsetX;
        double maxX = offsetX + displayW;
        double minY = offsetY;
        double maxY = offsetY + displayH;
        
        cropX = Math.max(minX, Math.min(cropX, maxX - cropSize));
        cropY = Math.max(minY, Math.min(cropY, maxY - cropSize));
        cropSize = Math.min(cropSize, Math.min(displayW, displayH));

        // Draw dark overlay
        gc.setFill(Color.color(0, 0, 0, 0.5));
        gc.fillRect(0, 0, canvasW, cropY);
        gc.fillRect(0, cropY + cropSize, canvasW, canvasH - (cropY + cropSize));
        gc.fillRect(0, cropY, cropX, cropSize);
        gc.fillRect(cropX + cropSize, cropY, canvasW - (cropX + cropSize), cropSize);

        // Draw crop box border
        gc.setStroke(Color.web("#1565c0"));
        gc.setLineWidth(2);
        gc.strokeRect(cropX, cropY, cropSize, cropSize);

        // Draw 8 handles (corners + sides)
        double handleSize = 8;
        
        // Top-left, Top-center, Top-right
        drawHandle(gc, cropX, cropY, handleSize);
        drawHandle(gc, cropX + cropSize / 2, cropY, handleSize);
        drawHandle(gc, cropX + cropSize, cropY, handleSize);
        
        // Middle-left, Middle-right
        drawHandle(gc, cropX, cropY + cropSize / 2, handleSize);
        drawHandle(gc, cropX + cropSize, cropY + cropSize / 2, handleSize);
        
        // Bottom-left, Bottom-center, Bottom-right
        drawHandle(gc, cropX, cropY + cropSize, handleSize);
        drawHandle(gc, cropX + cropSize / 2, cropY + cropSize, handleSize);
        drawHandle(gc, cropX + cropSize, cropY + cropSize, handleSize);
    }

    private void drawHandle(GraphicsContext gc, double x, double y, double size) {
        gc.setFill(Color.web("#1565c0"));
        gc.fillRect(x - size / 2, y - size / 2, size, size);
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(2);
        gc.strokeRect(x - size / 2, y - size / 2, size, size);
    }

    private void handleMousePressed(MouseEvent e) {
        double x = e.getX();
        double y = e.getY();
        dragStartX = x;
        dragStartY = y;
        
        double tolerance = 12;

        // Check corners and edges
        if (isNear(x, y, cropX, cropY, tolerance)) {
            dragHandle = 0; // Top-left
            isDragging = true;
        } else if (isNear(x, y, cropX + cropSize / 2, cropY, tolerance)) {
            dragHandle = 1; // Top-center
            isDragging = true;
        } else if (isNear(x, y, cropX + cropSize, cropY, tolerance)) {
            dragHandle = 2; // Top-right
            isDragging = true;
        } else if (isNear(x, y, cropX, cropY + cropSize / 2, tolerance)) {
            dragHandle = 3; // Middle-left
            isDragging = true;
        } else if (isNear(x, y, cropX + cropSize, cropY + cropSize / 2, tolerance)) {
            dragHandle = 4; // Middle-right
            isDragging = true;
        } else if (isNear(x, y, cropX, cropY + cropSize, tolerance)) {
            dragHandle = 5; // Bottom-left
            isDragging = true;
        } else if (isNear(x, y, cropX + cropSize / 2, cropY + cropSize, tolerance)) {
            dragHandle = 6; // Bottom-center
            isDragging = true;
        } else if (isNear(x, y, cropX + cropSize, cropY + cropSize, tolerance)) {
            dragHandle = 7; // Bottom-right
            isDragging = true;
        } else if (x >= cropX && x <= cropX + cropSize &&
                   y >= cropY && y <= cropY + cropSize) {
            // Inside crop box - move
            dragHandle = -1;
            dragOffsetX = x - cropX;
            dragOffsetY = y - cropY;
            isDragging = true;
            canvas.setCursor(javafx.scene.Cursor.CLOSED_HAND);
        }
    }

    private void handleMouseDragged(MouseEvent e) {
        if (!isDragging) return;

        double x = e.getX();
        double y = e.getY();
        double deltaX = x - dragStartX;
        double deltaY = y - dragStartY;

        double minX = offsetX;
        double maxX = offsetX + displayW;
        double minY = offsetY;
        double maxY = offsetY + displayH;
        double minSize = 50;

        if (dragHandle == -1) {
            // Move entire box
            cropX = Math.max(minX, Math.min(x - dragOffsetX, maxX - cropSize));
            cropY = Math.max(minY, Math.min(y - dragOffsetY, maxY - cropSize));
        } else if (dragHandle == 0) {
            // Top-left corner
            double newX = cropX + deltaX;
            double newY = cropY + deltaY;
            double newSize = Math.min(cropX + cropSize - newX, cropY + cropSize - newY);
            newSize = Math.max(minSize, newSize);
            cropX = Math.max(minX, cropX + cropSize - newSize);
            cropY = Math.max(minY, cropY + cropSize - newSize);
            cropSize = newSize;
        } else if (dragHandle == 1) {
            // Top-center (only Y)
            double newY = cropY + deltaY;
            double newSize = cropY + cropSize - newY;
            newSize = Math.max(minSize, newSize);
            cropY = Math.max(minY, cropY + cropSize - newSize);
            cropSize = newSize;
        } else if (dragHandle == 2) {
            // Top-right corner
            double newY = cropY + deltaY;
            double newX = x;
            double newSize = Math.min(newX - cropX, cropY + cropSize - newY);
            newSize = Math.max(minSize, newSize);
            cropY = Math.max(minY, cropY + cropSize - newSize);
            cropSize = newSize;
        } else if (dragHandle == 3) {
            // Middle-left (only X)
            double newX = cropX + deltaX;
            double newSize = cropX + cropSize - newX;
            newSize = Math.max(minSize, newSize);
            cropX = Math.max(minX, cropX + cropSize - newSize);
            cropSize = newSize;
        } else if (dragHandle == 4) {
            // Middle-right (only X)
            cropSize = Math.max(minSize, Math.min(x - cropX, maxX - cropX));
        } else if (dragHandle == 5) {
            // Bottom-left corner
            double newX = cropX + deltaX;
            double newSize = Math.min(cropX + cropSize - newX, y - cropY);
            newSize = Math.max(minSize, newSize);
            cropX = Math.max(minX, cropX + cropSize - newSize);
            cropSize = newSize;
        } else if (dragHandle == 6) {
            // Bottom-center (only Y)
            cropSize = Math.max(minSize, Math.min(y - cropY, maxY - cropY));
        } else if (dragHandle == 7) {
            // Bottom-right corner
            double newSize = Math.min(x - cropX, y - cropY);
            cropSize = Math.max(minSize, newSize);
        }

        dragStartX = x;
        dragStartY = y;
        redrawCanvas();
    }

    private void handleMouseReleased(MouseEvent e) {
        isDragging = false;
        dragHandle = -1;
        canvas.setCursor(javafx.scene.Cursor.DEFAULT);
    }

    private void handleMouseMoved(MouseEvent e) {
        double x = e.getX();
        double y = e.getY();
        double tolerance = 12;

        // Check for handle hover
        if ((isNear(x, y, cropX, cropY, tolerance) || isNear(x, y, cropX + cropSize, cropY + cropSize, tolerance)) ||
            (isNear(x, y, cropX + cropSize, cropY, tolerance) || isNear(x, y, cropX, cropY + cropSize, tolerance))) {
            canvas.setCursor(javafx.scene.Cursor.HAND);
        } else if (isNear(x, y, cropX + cropSize / 2, cropY, tolerance) ||
                   isNear(x, y, cropX + cropSize / 2, cropY + cropSize, tolerance)) {
            canvas.setCursor(javafx.scene.Cursor.V_RESIZE);
        } else if (isNear(x, y, cropX, cropY + cropSize / 2, tolerance) ||
                   isNear(x, y, cropX + cropSize, cropY + cropSize / 2, tolerance)) {
            canvas.setCursor(javafx.scene.Cursor.H_RESIZE);
        } else if (x >= cropX && x <= cropX + cropSize &&
                   y >= cropY && y <= cropY + cropSize) {
            canvas.setCursor(javafx.scene.Cursor.MOVE);
        } else {
            canvas.setCursor(javafx.scene.Cursor.DEFAULT);
        }
    }

    private boolean isNear(double x, double y, double targetX, double targetY, double tolerance) {
        return Math.abs(x - targetX) <= tolerance && Math.abs(y - targetY) <= tolerance;
    }

    private void cropImage() {
        try {
            System.out.println("[ImageCropDialog] Starting crop image process");
            
            // Convert canvas coordinates to original image coordinates
            double imgW = originalImage.getWidth();
            double imgH = originalImage.getHeight();
            
            // Calculate source coordinates in the original image
            int srcX = (int)((cropX - offsetX) / scale);
            int srcY = (int)((cropY - offsetY) / scale);
            int srcSize = (int)(cropSize / scale);
            
            System.out.println("[ImageCropDialog] Source: X=" + srcX + " Y=" + srcY + " Size=" + srcSize);
            System.out.println("[ImageCropDialog] Original image size: " + imgW + "x" + imgH);
            
            // Clamp to image boundaries
            srcX = Math.max(0, Math.min(srcX, (int)imgW - 1));
            srcY = Math.max(0, Math.min(srcY, (int)imgH - 1));
            srcSize = Math.min(srcSize, (int)Math.min(imgW - srcX, imgH - srcY));
            
            System.out.println("[ImageCropDialog] Clamped source: X=" + srcX + " Y=" + srcY + " Size=" + srcSize);

            javafx.scene.image.PixelReader reader = originalImage.getPixelReader();
            javafx.scene.image.WritableImage croppedImage =
                new javafx.scene.image.WritableImage(srcSize, srcSize);

            for (int y = 0; y < srcSize; y++) {
                for (int x = 0; x < srcSize; x++) {
                    int readX = srcX + x;
                    int readY = srcY + y;
                    if (readX >= 0 && readX < imgW && readY >= 0 && readY < imgH) {
                        croppedImage.getPixelWriter().setArgb(x, y,
                            reader.getArgb(readX, readY));
                    }
                }
            }

            java.awt.image.BufferedImage awtImage =
                javafx.embed.swing.SwingFXUtils.fromFXImage(croppedImage, null);
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            javax.imageio.ImageIO.write(awtImage, "png", baos);
            byte[] bytes = baos.toByteArray();
            resultBase64 = Base64.getEncoder().encodeToString(bytes);
            
            System.out.println("[ImageCropDialog] Crop successful - base64 length: " + resultBase64.length());

        } catch (Exception e) {
            System.out.println("[ImageCropDialog] Error during crop: " + e.getMessage());
            e.printStackTrace();
        }
    }
}