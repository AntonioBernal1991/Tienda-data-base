package org.example.ui;

import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;

public final class WindowUtil {
    private WindowUtil() {
    }

    public static void applyWindowSettings(Stage stage, Scene scene, String title) {
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                stage.close();
            }
        });

        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();

        stage.setTitle(title);
        stage.setScene(scene);
        stage.setMaximized(false);
        stage.setX(bounds.getMinX());
        stage.setY(bounds.getMinY());
        stage.setWidth(bounds.getWidth());
        stage.setHeight(bounds.getHeight());
        stage.show();
    }
}
