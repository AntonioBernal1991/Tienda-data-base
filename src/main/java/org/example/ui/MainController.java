package org.example.ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class MainController {

    @FXML
    private Button btnProductos;

    @FXML
    private Button btnClientes;

    @FXML
    private Button btnPedidos;

    @FXML
    private void onAbrirProductos() {
        abrirVista("/org/example/ui/ProductView.fxml", "Productos", btnProductos);
    }

    @FXML
    private void onAbrirClientes() {
        abrirVista("/org/example/ui/ClientView.fxml", "Clientes", btnClientes);
    }

    @FXML
    private void onAbrirPedidos() {
        abrirVista("/org/example/ui/OrderView.fxml", "Pedidos", btnPedidos);
    }

    private void abrirVista(String rutaFxml, String titulo, Node origen) {
        try {
            FXMLLoader fxml = new FXMLLoader(getClass().getResource(rutaFxml));
            Scene scene = new Scene(fxml.load());

            Stage stage = (Stage) origen.getScene().getWindow();
            WindowUtil.applyWindowSettings(stage, scene, titulo);
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("No se pudo abrir la vista: " + titulo);
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }
}
