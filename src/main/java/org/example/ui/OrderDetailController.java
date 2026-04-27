package org.example.ui;

import dao.PedidoDAO;
import dao.ProductoDAO;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import model.PedidoClienteView;
import model.PedidoDetalleView;
import model.Producto;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class OrderDetailController {
    @FXML
    private Label lblPedidoId;

    @FXML
    private Label lblFecha;

    @FXML
    private Label lblCliente;

    @FXML
    private Label lblCiudad;

    @FXML
    private TableView<PedidoDetalleView> tablaDetalle;

    @FXML
    private TableColumn<PedidoDetalleView, Integer> colProductoId;

    @FXML
    private TableColumn<PedidoDetalleView, String> colProducto;

    @FXML
    private TableColumn<PedidoDetalleView, Integer> colCantidad;

    @FXML
    private TableColumn<PedidoDetalleView, Double> colPrecio;

    @FXML
    private TableColumn<PedidoDetalleView, Double> colTotal;

    @FXML
    private ComboBox<Producto> cmbProducto;

    @FXML
    private TextField txtCantidad;

    @FXML
    private Label lblTotalCantidad;

    @FXML
    private Label lblTotalPedido;

    private final PedidoDAO pedidoDAO = new PedidoDAO();
    private final ProductoDAO productoDAO = new ProductoDAO();
    private int pedidoId;

    @FXML
    private void initialize() {
        tablaDetalle.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        colProductoId.setCellValueFactory(new PropertyValueFactory<>("productoId"));
        colProducto.setCellValueFactory(new PropertyValueFactory<>("producto"));
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        configurarComboProducto();
    }

    public void setPedidoId(int pedidoId) {
        this.pedidoId = pedidoId;
        cargarCabeceraPedido();
        cargarDetallePedido();
        cargarProductosDisponibles();
    }

    @FXML
    private void onAnadirProducto() {
        Producto producto = cmbProducto.getValue();
        String cantidadTexto = txtCantidad.getText() == null ? "" : txtCantidad.getText().trim();

        if (producto == null || cantidadTexto.isEmpty()) {
            mostrarAviso("Campos incompletos", "Debes seleccionar producto y cantidad.");
            return;
        }

        int cantidad;
        try {
            cantidad = Integer.parseInt(cantidadTexto);
        } catch (NumberFormatException e) {
            mostrarAviso("Cantidad no válida", "La cantidad debe ser un número entero.");
            return;
        }

        if (cantidad <= 0) {
            mostrarAviso("Cantidad no válida", "La cantidad debe ser mayor que 0.");
            return;
        }

        if (cantidad > producto.getStock()) {
            mostrarAviso("Stock insuficiente",
                    "El producto seleccionado tiene stock " + producto.getStock() + ".");
            return;
        }

        try {
            pedidoDAO.agregarProductoAPedido(pedidoId, producto.getId(), cantidad);
            cargarDetallePedido();
            cargarProductosDisponibles();
            txtCantidad.clear();
            cmbProducto.setValue(null);
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("No se pudo añadir el producto al pedido");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void onEliminarProducto() {
        Producto producto = cmbProducto.getValue();
        String cantidadTexto = txtCantidad.getText() == null ? "" : txtCantidad.getText().trim();

        if (producto == null || cantidadTexto.isEmpty()) {
            mostrarAviso("Campos incompletos", "Debes seleccionar producto y cantidad.");
            return;
        }

        int cantidad;
        try {
            cantidad = Integer.parseInt(cantidadTexto);
        } catch (NumberFormatException e) {
            mostrarAviso("Cantidad no válida", "La cantidad debe ser un número entero.");
            return;
        }

        if (cantidad <= 0) {
            mostrarAviso("Cantidad no válida", "La cantidad debe ser mayor que 0.");
            return;
        }

        try {
            pedidoDAO.eliminarProductoDePedido(pedidoId, producto.getId(), cantidad);
            cargarDetallePedido();
            cargarProductosDisponibles();
            txtCantidad.clear();
            cmbProducto.setValue(null);
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("No se pudo eliminar el producto del pedido");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void onVolverPedidos() {
        onIrPedidos();
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
            FXMLLoader fxml = new FXMLLoader(getClass().getResource(rutaFxml));
            Scene scene = new Scene(fxml.load());

            Stage stage = (Stage) tablaDetalle.getScene().getWindow();
            WindowUtil.applyWindowSettings(stage, scene, titulo);
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("No se pudo abrir la vista: " + titulo);
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    private void cargarCabeceraPedido() {
        try {
            PedidoClienteView cabecera = pedidoDAO.obtenerPedidoResumenPorId(pedidoId);
            if (cabecera == null) {
                throw new RuntimeException("No existe pedido con ID " + pedidoId + ".");
            }
            lblPedidoId.setText(String.valueOf(cabecera.getPedidoId()));
            lblFecha.setText(String.valueOf(cabecera.getFecha()));
            lblCliente.setText(cabecera.getCliente());
            lblCiudad.setText(cabecera.getCiudad());
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("No se pudo cargar la cabecera del pedido");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    private void cargarDetallePedido() {
        try {
            List<PedidoDetalleView> detalle = pedidoDAO.listarDetallePorPedido(pedidoId);
            tablaDetalle.setItems(FXCollections.observableArrayList(detalle));
            actualizarTotales(detalle);
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("No se pudo cargar el detalle del pedido");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    private void actualizarTotales(List<PedidoDetalleView> detalle) {
        int totalCantidad = 0;
        double totalPedido = 0.0;
        for (PedidoDetalleView linea : detalle) {
            totalCantidad += linea.getCantidad();
            totalPedido += linea.getTotal();
        }

        lblTotalCantidad.setText(String.valueOf(totalCantidad));
        lblTotalPedido.setText(String.format(Locale.US, "%.2f", totalPedido));
    }

    private void cargarProductosDisponibles() {
        try {
            List<Producto> productos = new ArrayList<>();
            for (Producto producto : productoDAO.listarProductos()) {
                if (producto.getStock() > 0) {
                    productos.add(producto);
                }
            }
            cmbProducto.setItems(FXCollections.observableArrayList(productos));
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("No se pudieron cargar los productos");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    private void configurarComboProducto() {
        StringConverter<Producto> converter = new StringConverter<>() {
            @Override
            public String toString(Producto producto) {
                if (producto == null) {
                    return "";
                }
                return producto.getId() + " - " + producto.getNombre() + " (stock: " + producto.getStock() + ")";
            }

            @Override
            public Producto fromString(String string) {
                return null;
            }
        };
        cmbProducto.setConverter(converter);
        cmbProducto.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Producto item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : converter.toString(item));
            }
        });
        cmbProducto.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Producto item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : converter.toString(item));
            }
        });
    }

    private void mostrarAviso(String cabecera, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Aviso");
        alert.setHeaderText(cabecera);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
