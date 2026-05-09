package org.example;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.example.ui.FxmlUtil;
import org.example.ui.WindowUtil;

import java.util.Objects;

public class App extends Application {

    @Override
    public void init() throws Exception {
        try (var regular = App.class.getResourceAsStream("/fonts/Inter-Regular.ttf")) {
            Objects.requireNonNull(regular, "fonts/Inter-Regular.ttf");
            if (Font.loadFont(regular, 12) == null) {
                throw new IllegalStateException("Font.loadFont falló para Inter-Regular.ttf");
            }
        }
        try (var bold = App.class.getResourceAsStream("/fonts/Inter-Bold.ttf")) {
            Objects.requireNonNull(bold, "fonts/Inter-Bold.ttf");
            if (Font.loadFont(bold, 12) == null) {
                throw new IllegalStateException("Font.loadFont falló para Inter-Bold.ttf");
            }
        }
    }

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FxmlUtil.loadRoot("/org/example/ui/MainView.fxml");
        Scene scene = new Scene(root);
        WindowUtil.applyWindowSettings(stage, scene, "Tienda");
    }

    public static void main(String[] args) {
        launch(args);
    }
}

