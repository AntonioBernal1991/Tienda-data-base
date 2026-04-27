package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.ui.WindowUtil;

public class App extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxml = new FXMLLoader(App.class.getResource("/org/example/ui/MainView.fxml"));
        Scene scene = new Scene(fxml.load());
        WindowUtil.applyWindowSettings(stage, scene, "Tienda");
    }

    public static void main(String[] args) {
        launch(args);
    }
}

