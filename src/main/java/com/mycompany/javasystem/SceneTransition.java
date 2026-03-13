package com.mycompany.javasystem;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.animation.Interpolator;

public class SceneTransition {

    public static void slideTo(Stage stage, String fxml, boolean maximize, Class<?> context) {
        try {
            Parent oldRoot = stage.getScene().getRoot();
            Parent newRoot = FXMLLoader.load(context.getResource(fxml));

            // Stack new root on top of old root
            StackPane container = new StackPane(oldRoot, newRoot);
            newRoot.setOpacity(0.0);
            stage.getScene().setRoot(container);

            Timeline timeline = new Timeline(
                new KeyFrame(Duration.millis(400),
                    new KeyValue(newRoot.opacityProperty(), 1.0, Interpolator.EASE_BOTH),
                    new KeyValue(oldRoot.opacityProperty(), 0.0, Interpolator.EASE_BOTH)
                )
            );

            timeline.setOnFinished(e -> {
                stage.setMaximized(maximize);
                stage.getScene().setRoot(newRoot);
                newRoot.setOpacity(1.0);
            });

            timeline.play();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}