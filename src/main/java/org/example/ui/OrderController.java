package org.example.ui;

import dao.ClienteDAO;
import dao.PedidoDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import model.Cliente;
import model.PedidoClienteView;

import java.time.YearMonth;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class OrderController {

    @FXML
    private TableView<PedidoClienteView> tablaPedidos;

    @FXML
    private TableColumn<PedidoClienteView, Integer> colPedidoId;

    @FXML
    private TableColumn<PedidoClienteView, java.sql.Date> colFecha;

    @FXML
    private TableColumn<PedidoClienteView, String> colCliente;

    @FXML
    private TableColumn<PedidoClienteView, String> colCiudad;

    @FXML
    private ComboBox<Cliente> cmbClienteId;

    @FXML
    private ComboBox<Integer> cmbPedidoId;

    @FXML
    private TextField txtBuscar;

    @FXML
    private Label lblKpiTotal;

    @FXML
    private Label lblKpiClientes;

    @FXML
    private Label lblKpiCiudades;

    @FXML
    private Label lblKpiEsteMes;

    private final PedidoDAO pedidoDAO = new PedidoDAO();
    private final ClienteDAO clienteDAO = new ClienteDAO();

    private final ObservableList<PedidoClienteView> todosLosPedidos = FXCollections.observableArrayList();
    private FilteredList<PedidoClienteView> pedidosFiltrados;

    @FXML
    private void initialize() {
        try {
            tablaPedidos.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            colPedidoId.setCellValueFactory(new PropertyValueFactory<>("pedidoId"));
            colFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
            colCliente.setCellValueFactory(new PropertyValueFactory<>("cliente"));
            colCiudad.setCellValueFactory(new PropertyValueFactory<>("ciudad"));

            pedidosFiltrados = new FilteredList<>(todosLosPedidos, p -> true);
            tablaPedidos.setItems(pedidosFiltrados);

            txtBuscar.textProperty().addListener((obs, oldVal, newVal) -> aplicarFiltroBusqueda(newVal));

            configurarVisualizacionCombos();
            cargarPedidos();
            cargarClientesEnCombo();
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("No se pudo inicializar la vista de pedidos");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    private void aplicarFiltroBusqueda(String consulta) {
        String needle = consulta == null ? "" : consulta.trim().toLowerCase();
        pedidosFiltrados.setPredicate(p -> {
            if (needle.isEmpty()) {
                return true;
            }
            if (String.valueOf(p.getPedidoId()).contains(needle)) {
                return true;
            }
            if (contiene(p.getCliente(), needle)) {
                return true;
            }
            if (contiene(p.getCiudad(), needle)) {
                return true;
            }
            if (p.getFecha() != null && p.getFecha().toString().toLowerCase().contains(needle)) {
                return true;
            }
            return false;
        });
    }

    private static boolean contiene(String texto, String needle) {
        return texto != null && texto.toLowerCase().contains(needle);
    }

    private void actualizarKpis() {
        List<PedidoClienteView> datos = List.copyOf(todosLosPedidos);
        lblKpiTotal.setText(String.valueOf(datos.size()));

        Set<String> clientes = new HashSet<>();
        Set<String> ciudades = new HashSet<>();
        YearMonth mesActual = YearMonth.now();
        int esteMes = 0;

        for (PedidoClienteView p : datos) {
            String c = p.getCliente();
            if (c != null && !c.isBlank()) {
                clientes.add(c.trim());
            }
            String ciudad = p.getCiudad();
            if (ciudad != null && !ciudad.isBlank()) {
                ciudades.add(ciudad.trim());
            }
            if (p.getFecha() != null) {
                if (YearMonth.from(p.getFecha().toLocalDate()).equals(mesActual)) {
                    esteMes++;
                }
            }
        }

        lblKpiClientes.setText(String.valueOf(clientes.size()));
        lblKpiCiudades.setText(String.valueOf(ciudades.size()));
        lblKpiEsteMes.setText(String.valueOf(esteMes));
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
    private void onNuevoPedido() {
        limpiarCampos();
        if (cmbClienteId != null) {
            cmbClienteId.requestFocus();
        }
    }

    @FXML
    private void onListarPedidos() {
        cargarPedidos();
    }

    @FXML
    private void onCrearPedido() {
        Cliente clienteSeleccionado = cmbClienteId.getValue();

        if (clienteSeleccionado == null) {
            mostrarAviso("Campos incompletos",
                    "Debes seleccionar un cliente para crear el pedido.");
            return;
        }

        try {
            int pedidoId = pedidoDAO.crearPedido(clienteSeleccionado.getId());
            if (pedidoId <= 0) {
                throw new RuntimeException("No se pudo crear el pedido.");
            }

            cargarPedidos();
            cargarClientesEnCombo();
            limpiarCampos();

            Alert ok = new Alert(Alert.AlertType.INFORMATION);
            ok.setTitle("Pedido creado");
            ok.setHeaderText("Pedido creado correctamente");
            ok.setContentText("Se ha creado el pedido con ID " + pedidoId + ".");
            ok.showAndWait();
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("No se pudo crear el pedido");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void onEliminarPedido() {
        Integer pedidoId = cmbPedidoId.getValue();
        if (pedidoId == null) {
            mostrarAviso("Campo ID pedido vacío",
                    "Debes seleccionar el ID del pedido que quieres eliminar.");
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar eliminación");
        confirmacion.setHeaderText("¿Eliminar el pedido con ID " + pedidoId + "?");
        confirmacion.setContentText("Si tiene productos asociados, también se repondrá el stock.");

        confirmacion.showAndWait().ifPresent(respuesta -> {
            if (respuesta == ButtonType.OK) {
                try {
                    boolean eliminado = pedidoDAO.eliminarPedidoYReponerStock(pedidoId);
                    if (eliminado) {
                        cargarPedidos();
                        limpiarCampos();

                        Alert ok = new Alert(Alert.AlertType.INFORMATION);
                        ok.setTitle("Pedido eliminado");
                        ok.setHeaderText("Pedido eliminado correctamente");
                        ok.setContentText("Se eliminó el pedido " + pedidoId + ".");
                        ok.showAndWait();
                    } else {
                        mostrarAviso("Pedido no encontrado",
                                "No existe ningún pedido con el ID " + pedidoId + ".");
                    }
                } catch (Exception e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText("No se pudo eliminar el pedido");
                    alert.setContentText(e.getMessage());
                    alert.showAndWait();
                }
            }
        });
    }

    @FXML
    private void onVerDetalle() {
        Integer pedidoId = null;
        PedidoClienteView seleccionado = tablaPedidos.getSelectionModel().getSelectedItem();
        if (seleccionado != null) {
            pedidoId = seleccionado.getPedidoId();
        } else if (cmbPedidoId.getValue() != null) {
            pedidoId = cmbPedidoId.getValue();
        }

        if (pedidoId == null) {
            mostrarAviso("Pedido no seleccionado",
                    "Selecciona un pedido de la tabla o en el combo de ID para ver su detalle.");
            return;
        }

        try {
            FXMLLoader loader = FxmlUtil.loadRootAndKeepLoader("/org/example/ui/OrderDetailView.fxml");
            Parent root = loader.getRoot();
            Scene scene = new Scene(root);

            OrderDetailController controller = loader.getController();
            controller.setPedidoId(pedidoId);

            Stage stage = (Stage) tablaPedidos.getScene().getWindow();
            WindowUtil.applyWindowSettings(stage, scene, "Detalle pedido");
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("No se pudo abrir el detalle del pedido");
            alert.setContentText(FxmlUtil.causaCadena(e));
            alert.showAndWait();
        }
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

            Stage stage = (Stage) tablaPedidos.getScene().getWindow();
            WindowUtil.applyWindowSettings(stage, scene, titulo);
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("No se pudo abrir la vista: " + titulo);
            alert.setContentText(FxmlUtil.causaCadena(e));
            alert.showAndWait();
        }
    }

    private void cargarPedidos() {
        try {
            List<PedidoClienteView> pedidos = pedidoDAO.listarPedidosResumen();
            todosLosPedidos.setAll(pedidos);
            actualizarKpis();
            cargarIdsPedidosEnCombo();
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("No se pudieron cargar los pedidos");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    private void configurarVisualizacionCombos() {
        configurarComboCliente();
    }

    private void configurarComboCliente() {
        StringConverter<Cliente> converter = new StringConverter<>() {
            @Override
            public String toString(Cliente cliente) {
                if (cliente == null) {
                    return "";
                }
                return cliente.getId() + " - " + cliente.getNombre();
            }

            @Override
            public Cliente fromString(String string) {
                return null;
            }
        };
        cmbClienteId.setConverter(converter);
        cmbClienteId.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Cliente item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : converter.toString(item));
            }
        });
        cmbClienteId.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Cliente item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : converter.toString(item));
            }
        });
    }

    private void cargarClientesEnCombo() {
        try {
            List<Cliente> clientes = clienteDAO.listarClientes();
            cmbClienteId.setItems(FXCollections.observableArrayList(clientes));
        } catch (Exception e) {
            mostrarError("No se pudieron cargar los clientes", e);
        }
    }

    private void cargarIdsPedidosEnCombo() {
        try {
            List<Integer> ids = pedidoDAO.listarIdsPedidos();
            cmbPedidoId.setItems(FXCollections.observableArrayList(ids));
        } catch (Exception e) {
            mostrarError("No se pudieron cargar los IDs de pedidos", e);
        }
    }

    private void limpiarCampos() {
        cmbClienteId.setValue(null);
        cmbPedidoId.setValue(null);
        tablaPedidos.getSelectionModel().clearSelection();
    }

    private void mostrarAviso(String cabecera, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Aviso");
        alert.setHeaderText(cabecera);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarError(String cabecera, Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(cabecera);
        alert.setContentText(e.getMessage());
        alert.showAndWait();
    }
}
