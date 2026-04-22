package org.example.ui;

import dao.ClienteDAO;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import model.Cliente;

import java.util.List;

public class ClientController {

    @FXML
    private TableView<Cliente> tablaClientes;

    @FXML
    private TableColumn<Cliente, Integer> colId;

    @FXML
    private TableColumn<Cliente, String> colNombre;

    @FXML
    private TableColumn<Cliente, String> colEmail;

    @FXML
    private TextField txtNombre;

    @FXML
    private TextField txtEmail;

    @FXML
    private TextField txtId;

    private final ClienteDAO clienteDAO = new ClienteDAO();

    @FXML
    private void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));

        cargarClientes();
    }

    @FXML
    private void onListarClientes() {
        cargarClientes();
    }

    @FXML
    private void onVolver() {
        try {
            FXMLLoader fxml = new FXMLLoader(
                    getClass().getResource("/org/example/ui/MainView.fxml"));
            Scene scene = new Scene(fxml.load());

            Stage stage = (Stage) tablaClientes.getScene().getWindow();
            stage.setTitle("Tienda");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("No se pudo volver al menú principal");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void onAnadirCliente() {
        String nombre = txtNombre.getText() == null ? "" : txtNombre.getText().trim();
        String email = txtEmail.getText() == null ? "" : txtEmail.getText().trim();

        if (nombre.isEmpty() || email.isEmpty()) {
            mostrarAviso("Campos incompletos",
                    "Debes rellenar nombre y email antes de añadir el cliente.");
            return;
        }

        if (!email.contains("@") || !email.contains(".")) {
            mostrarAviso("Email no válido",
                    "Introduce un email con formato correcto (ej. usuario@dominio.com).");
            return;
        }

        try {
            Cliente nuevo = new Cliente(nombre, email);
            clienteDAO.insertarCliente(nuevo);

            cargarClientes();
            limpiarCampos();
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("No se pudo añadir el cliente");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    private void cargarClientes() {
        try {
            List<Cliente> clientes = clienteDAO.listarClientes();
            tablaClientes.setItems(FXCollections.observableArrayList(clientes));
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("No se pudieron cargar los clientes");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    private void limpiarCampos() {
        txtNombre.clear();
        txtEmail.clear();
        if (txtId != null) {
            txtId.clear();
        }
    }

    private void mostrarAviso(String cabecera, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Aviso");
        alert.setHeaderText(cabecera);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
