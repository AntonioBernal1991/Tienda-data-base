package org.example.ui;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class MainController {

    @FXML
    private Button btnEntrar;

    @FXML
    private void onEntrar() {
        abrirVista("/org/example/ui/ProductView.fxml", "Productos", btnEntrar);
    }

    private void abrirVista(String rutaFxml, String titulo, Node origen) {
        try {
            Parent root = FxmlUtil.loadRoot(rutaFxml);
            Scene scene = new Scene(root);

            Stage stage = (Stage) origen.getScene().getWindow();
            WindowUtil.applyWindowSettings(stage, scene, titulo);
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("No se pudo abrir la vista: " + titulo);
            alert.setContentText(FxmlUtil.causaCadena(e));
            alert.showAndWait();
        }
    }
}
