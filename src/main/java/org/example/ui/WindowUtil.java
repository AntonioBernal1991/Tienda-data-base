package org.example.ui;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.net.URL;

public final class WindowUtil {
    private WindowUtil() {
    }

    public static void applyWindowSettings(Stage stage, Scene scene, String title) {
        mergeGlobalStylesheet(scene);

        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();

        Scene finalScene = wrapWithUniformScale(scene, bounds.getWidth(), bounds.getHeight());

        if (finalScene != scene) {
            finalScene.getStylesheets().setAll(scene.getStylesheets());
        }

        stage.setTitle(title);
        stage.setScene(finalScene);

        if (!stage.isShowing()) {
            stage.setX(bounds.getMinX());
            stage.setY(bounds.getMinY());
            stage.setWidth(bounds.getWidth());
            stage.setHeight(bounds.getHeight());
        }

        if (!stage.isMaximized()) {
            stage.setMaximized(true);
        }

        stage.show();
    }

    /** Inter + fallback; debe aplicarse a cada {@link Scene} antes de mostrarla. */
    private static void mergeGlobalStylesheet(Scene scene) {
        URL css = WindowUtil.class.getResource("/org/example/ui/styles/app.css");
        if (css != null) {
            String url = css.toExternalForm();
            if (!scene.getStylesheets().contains(url)) {
                scene.getStylesheets().add(0, url);
            }
        }
    }

    private static Scene wrapWithUniformScale(Scene scene, double sceneWidth, double sceneHeight) {
        Parent originalRoot = scene.getRoot();
        if (!(originalRoot instanceof Region region)) {
            return scene;
        }

        if (originalRoot instanceof BorderPane bp && bp.getLeft() == null) {
            return scene;
        }

        double designW = region.getPrefWidth();
        double designH = region.getPrefHeight();
        if (designW <= 0 || designH <= 0) {
            return scene;
        }

        scene.setRoot(new Group());

        region.setMinSize(designW, designH);
        region.setPrefSize(designW, designH);
        region.setMaxSize(designW, designH);

        Group scalable = new Group(region);

        StackPane wrapper = new StackPane(scalable);
        wrapper.setAlignment(Pos.CENTER_LEFT);
        String rootStyle = region.getStyle();
        if (rootStyle != null && !rootStyle.isBlank()) {
            wrapper.setStyle(rootStyle);
        }

        DoubleBinding scaleBinding = Bindings.createDoubleBinding(
                () -> {
                    double w = wrapper.getWidth();
                    double h = wrapper.getHeight();
                    if (w <= 0 || h <= 0) {
                        return 1.0;
                    }
                    return Math.min(w / designW, h / designH);
                },
                wrapper.widthProperty(), wrapper.heightProperty()
        );
        region.scaleXProperty().bind(scaleBinding);
        region.scaleYProperty().bind(scaleBinding);

        return new Scene(wrapper, sceneWidth, sceneHeight);
    }
}
