package org.example.ui;

import dao.ClienteDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
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
    private TableColumn<Cliente, String> colCiudad;

    @FXML
    private TextField txtNombre;

    @FXML
    private TextField txtEmail;

    @FXML
    private TextField txtCiudad;

    @FXML
    private TextField txtId;

    @FXML
    private TextField txtBuscar;

    @FXML
    private Label lblKpiTotal;

    @FXML
    private Label lblKpiConCiudad;

    @FXML
    private Label lblKpiSinCiudad;

    @FXML
    private Label lblKpiCiudades;

    private final ClienteDAO clienteDAO = new ClienteDAO();

    private final ObservableList<Cliente> todosLosClientes = FXCollections.observableArrayList();
    private FilteredList<Cliente> clientesFiltrados;

    @FXML
    private void initialize() {
        tablaClientes.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colCiudad.setCellValueFactory(new PropertyValueFactory<>("ciudad"));

        clientesFiltrados = new FilteredList<>(todosLosClientes, p -> true);
        tablaClientes.setItems(clientesFiltrados);

        txtBuscar.textProperty().addListener((obs, oldVal, newVal) -> aplicarFiltroBusqueda(newVal));

        cargarClientes();
    }

    private void aplicarFiltroBusqueda(String consulta) {
        String needle = consulta == null ? "" : consulta.trim().toLowerCase();
        clientesFiltrados.setPredicate(c -> {
            if (needle.isEmpty()) {
                return true;
            }
            if (Integer.toString(c.getId()).contains(needle)) {
                return true;
            }
            if (contiene(c.getNombre(), needle)) {
                return true;
            }
            if (contiene(c.getEmail(), needle)) {
                return true;
            }
            return contiene(c.getCiudad(), needle);
        });
    }

    private static boolean contiene(String texto, String needle) {
        return texto != null && texto.toLowerCase().contains(needle);
    }

    @FXML
    private void onExportar() {
        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle("Exportar");
        info.setHeaderText(null);
        info.setContentText("Exportación a archivo no está implementada en esta versión de demo.");
        info.showAndWait();
    }

    @FXML
    private void onNuevoCliente() {
        limpiarCampos();
        txtNombre.requestFocus();
    }

    @FXML
    private void onListarClientes() {
        cargarClientes();
    }

    @FXML
    private void onVolver() {
        onIrInicio();
    }

    @FXML
    private void onIrInicio() {
        abrirVista("/org/example/ui/MainView.fxml", "Tienda");
    }

    @FXML
    private void onIrProductos() {
        abrirVista("/org/example/ui/ProductView.fxml", "Productos");
    }

    @FXML
    private void onIrClientes() {
        abrirVista("/org/example/ui/ClientView.fxml", "Clientes");
    }

    @FXML
    private void onIrPedidos() {
        abrirVista("/org/example/ui/OrderView.fxml", "Pedidos");
    }

    private void abrirVista(String rutaFxml, String titulo) {
        try {
            Parent root = FxmlUtil.loadRoot(rutaFxml);
            Scene scene = new Scene(root);

            Stage stage = (Stage) tablaClientes.getScene().getWindow();
            WindowUtil.applyWindowSettings(stage, scene, titulo);
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("No se pudo abrir la vista: " + titulo);
            alert.setContentText(FxmlUtil.causaCadena(e));
            alert.showAndWait();
        }
    }

    @FXML
    private void onAnadirCliente() {
        String nombre = txtNombre.getText() == null ? "" : txtNombre.getText().trim();
        String email = txtEmail.getText() == null ? "" : txtEmail.getText().trim();
        String ciudad = txtCiudad.getText() == null ? "" : txtCiudad.getText().trim();

        if (nombre.isEmpty() || email.isEmpty() || ciudad.isEmpty()) {
            mostrarAviso("Campos incompletos",
                    "Debes rellenar nombre, email y ciudad antes de añadir el cliente.");
            return;
        }

        if (!email.contains("@") || !email.contains(".")) {
            mostrarAviso("Email no válido",
                    "Introduce un email con formato correcto (ej. usuario@dominio.com).");
            return;
        }

        try {
            Cliente nuevo = new Cliente(nombre, email, ciudad);
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

    @FXML
    private void onActualizarCliente() {
        String idTexto = txtId.getText() == null ? "" : txtId.getText().trim();
        if (idTexto.isEmpty()) {
            mostrarAviso("Campo ID vacío",
                    "Debes introducir el ID del cliente que quieres actualizar.");
            return;
        }

        int id;
        try {
            id = Integer.parseInt(idTexto);
        } catch (NumberFormatException e) {
            mostrarAviso("ID no válido", "El ID debe ser un número entero.");
            return;
        }

        Cliente existente;
        try {
            existente = clienteDAO.buscarPorId(id);
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("No se pudo buscar el cliente");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
            return;
        }

        if (existente == null) {
            mostrarAviso("Cliente no encontrado",
                    "No existe ningún cliente con el ID " + id + ".");
            return;
        }

        String nombreTexto = txtNombre.getText() == null ? "" : txtNombre.getText().trim();
        String emailTexto = txtEmail.getText() == null ? "" : txtEmail.getText().trim();
        String ciudadTexto = txtCiudad.getText() == null ? "" : txtCiudad.getText().trim();

        if (nombreTexto.isEmpty() && emailTexto.isEmpty() && ciudadTexto.isEmpty()) {
            mostrarAviso("Sin cambios",
                    "Rellena al menos nombre, email o ciudad para actualizar.");
            return;
        }

        if (!emailTexto.isEmpty() && (!emailTexto.contains("@") || !emailTexto.contains("."))) {
            mostrarAviso("Email no válido",
                    "Introduce un email con formato correcto (ej. usuario@dominio.com).");
            return;
        }

        String nuevoNombre = nombreTexto.isEmpty() ? existente.getNombre() : nombreTexto;
        String nuevoEmail = emailTexto.isEmpty() ? existente.getEmail() : emailTexto;
        String nuevaCiudad = ciudadTexto.isEmpty() ? existente.getCiudad() : ciudadTexto;

        try {
            Cliente actualizado = new Cliente(id, nuevoNombre, nuevoEmail, nuevaCiudad);
            boolean ok = clienteDAO.actualizarCliente(actualizado);
            if (ok) {
                cargarClientes();
                limpiarCampos();
            } else {
                mostrarAviso("Sin cambios",
                        "No se ha podido actualizar el cliente con ID " + id + ".");
            }
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("No se pudo actualizar el cliente");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    private void cargarClientes() {
        try {
            List<Cliente> clientes = clienteDAO.listarClientes();
            todosLosClientes.setAll(clientes);
            actualizarKpis(clientes);
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("No se pudieron cargar los clientes");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    private void actualizarKpis(List<Cliente> list) {
        lblKpiTotal.setText(String.valueOf(list.size()));

        long conCiudad = list.stream().filter(ClientController::tieneCiudadInformada).count();
        lblKpiConCiudad.setText(String.valueOf(conCiudad));

        long sinCiudad = list.stream().filter(c -> !tieneCiudadInformada(c)).count();
        lblKpiSinCiudad.setText(String.valueOf(sinCiudad));

        long ciudadesDistintas = list.stream()
                .filter(ClientController::tieneCiudadInformada)
                .map(c -> c.getCiudad().trim().toLowerCase())
                .distinct()
                .count();
        lblKpiCiudades.setText(String.valueOf(ciudadesDistintas));
    }

    private static boolean tieneCiudadInformada(Cliente c) {
        String ci = c.getCiudad();
        return ci != null && !ci.trim().isEmpty();
    }

    private void limpiarCampos() {
        txtNombre.clear();
        txtEmail.clear();
        txtCiudad.clear();
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
