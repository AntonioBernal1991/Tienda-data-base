package org.example.ui;

import dao.ClienteDAO;
import dao.PedidoDAO;
import dao.ProductoDAO;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Parent;
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
import model.Cliente;
import model.PedidoClienteView;
import model.PedidoDetalleView;
import model.Producto;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class OrderDetailController {

    private static final double ENVIO_ESTANDAR = 120.0;
    private static final Locale ES = new Locale("es", "ES");

    @FXML
    private Label lblTituloPedido;

    @FXML
    private Label lblBadgeEstado;

    @FXML
    private Label lblSubtituloFecha;

    @FXML
    private Label lblCardClienteNombre;

    @FXML
    private Label lblCardClienteEmail;

    @FXML
    private Label lblCardClienteTel;

    @FXML
    private Label lblMetodoPago;

    @FXML
    private Label lblMetodoPagoDetalle;

    @FXML
    private Label lblMetodoEnvio;

    @FXML
    private Label lblMetodoEnvioDetalle;

    @FXML
    private Label lblProductosHeading;

    @FXML
    private TableView<PedidoDetalleView> tablaDetalle;

    @FXML
    private TableColumn<PedidoDetalleView, String> colProducto;

    @FXML
    private TableColumn<PedidoDetalleView, Double> colPrecio;

    @FXML
    private TableColumn<PedidoDetalleView, Integer> colCantidad;

    @FXML
    private TableColumn<PedidoDetalleView, Double> colTotal;

    @FXML
    private Label lblHist1;

    @FXML
    private Label lblHist2;

    @FXML
    private Label lblHist3;

    @FXML
    private Label lblHist4;

    @FXML
    private Label lblHist4Check;

    @FXML
    private Label lblHist4Title;

    @FXML
    private Label lblSubtotal;

    @FXML
    private Label lblEnvio;

    @FXML
    private Label lblDescuento;

    @FXML
    private Label lblTotalGeneral;

    @FXML
    private Label lblResumenEstado;

    @FXML
    private Label lblResumenFecha;

    @FXML
    private Label lblDirEnvio;

    @FXML
    private Label lblDirFacturacion;

    @FXML
    private ComboBox<Producto> cmbProducto;

    @FXML
    private TextField txtCantidad;

    private final PedidoDAO pedidoDAO = new PedidoDAO();
    private final ClienteDAO clienteDAO = new ClienteDAO();
    private final ProductoDAO productoDAO = new ProductoDAO();

    private final NumberFormat moneda = NumberFormat.getCurrencyInstance(Locale.US);

    private int pedidoId;
    private PedidoClienteView cabeceraCache;

    @FXML
    private void initialize() {
        tablaDetalle.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        colProducto.setCellValueFactory(new PropertyValueFactory<>("producto"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        configurarComboProducto();

        lblMetodoPago.setText("Tarjeta de crédito");
        lblMetodoPagoDetalle.setText("Visa · ****4242");
        lblMetodoEnvio.setText("Envío estándar");
        lblMetodoEnvioDetalle.setText("Entrega estimada según zona.");
        lblCardClienteTel.setText("Teléfono no registrado en base de datos.");
    }

    public void setPedidoId(int pedidoId) {
        this.pedidoId = pedidoId;
        cargarCabeceraPedido();
        cargarDetallePedido();
        cargarProductosDisponibles();
    }

    @FXML
    private void onImprimir() {
        mostrarDemo("Imprimir", "La vista previa de impresión no está implementada en esta versión.");
    }

    @FXML
    private void onDescargar() {
        mostrarDemo("Descargar", "La exportación PDF no está implementada en esta versión.");
    }

    @FXML
    private void onMasAcciones() {
        mostrarDemo("Más acciones", "Opciones adicionales (duplicar pedido, enviar por correo, etc.) no están implementadas.");
    }

    @FXML
    private void onVerCliente() {
        Cliente c = cargarClienteCompleto();
        String nombre = c != null ? c.getNombre() : lblCardClienteNombre.getText();
        String email = c != null ? nullToDash(c.getEmail()) : lblCardClienteEmail.getText();
        String ciudad = cabeceraCache != null ? nullToDash(cabeceraCache.getCiudad()) : "—";
        mostrarDemo("Cliente",
                nombre + "\n" + email + "\nCiudad: " + ciudad);
    }

    @FXML
    private void onVerPago() {
        mostrarDemo("Pago", "Detalle de pasarela no disponible: método simulado para demo de interfaz.");
    }

    @FXML
    private void onVerEnvio() {
        mostrarDemo("Envío", lblMetodoEnvio.getText() + "\n" + lblMetodoEnvioDetalle.getText());
    }

    @FXML
    private void onEditarNotas() {
        mostrarDemo("Notas", "Campo de notas internas no está conectado a base de datos en esta versión.");
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
            Parent root = FxmlUtil.loadRoot(rutaFxml);
            Scene scene = new Scene(root);

            Stage stage = (Stage) tablaDetalle.getScene().getWindow();
            WindowUtil.applyWindowSettings(stage, scene, titulo);
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("No se pudo abrir la vista: " + titulo);
            alert.setContentText(FxmlUtil.causaCadena(e));
            alert.showAndWait();
        }
    }

    private void cargarCabeceraPedido() {
        try {
            PedidoClienteView cabecera = pedidoDAO.obtenerPedidoResumenPorId(pedidoId);
            if (cabecera == null) {
                throw new RuntimeException("No existe pedido con ID " + pedidoId + ".");
            }
            this.cabeceraCache = cabecera;

            lblTituloPedido.setText("Pedido ORD-" + String.format("%05d", pedidoId));

            DateTimeFormatter fechaLarga = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(ES);
            String fechaTxt = "";
            if (cabecera.getFecha() != null) {
                fechaTxt = cabecera.getFecha().toLocalDate().format(fechaLarga) + " · 10:30";
            }
            lblSubtituloFecha.setText(fechaTxt.isEmpty() ? "—" : fechaTxt);

            lblCardClienteNombre.setText(nullToDash(cabecera.getCliente()));

            Cliente cliente = cargarClienteCompleto();
            if (cliente != null && cliente.getEmail() != null && !cliente.getEmail().isBlank()) {
                lblCardClienteEmail.setText(cliente.getEmail());
            } else {
                lblCardClienteEmail.setText("Email no disponible");
            }

            actualizarResumenLaterales(cabecera);
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("No se pudo cargar la cabecera del pedido");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    private Cliente cargarClienteCompleto() {
        Integer clienteId = pedidoDAO.obtenerClienteIdPorPedido(pedidoId);
        if (clienteId == null) {
            return null;
        }
        try {
            return clienteDAO.buscarPorId(clienteId);
        } catch (Exception e) {
            return null;
        }
    }

    private void actualizarResumenLaterales(PedidoClienteView cabecera) {
        String ciudad = cabecera.getCiudad() != null ? cabecera.getCiudad().trim() : "";
        String nombre = cabecera.getCliente() != null ? cabecera.getCliente() : "Cliente";
        String bloqueDir = nombre + "\n"
                + (ciudad.isEmpty() ? "Ciudad no indicada" : ciudad + " (España)")
                + "\n\nLa dirección postal completa no está almacenada en la aplicación.";
        lblDirEnvio.setText(bloqueDir);
        lblDirFacturacion.setText(bloqueDir);

        lblResumenFecha.setText(
                cabecera.getFecha() != null
                        ? "Fecha del pedido: " + cabecera.getFecha()
                        : "—");
    }

    private void cargarDetallePedido() {
        try {
            List<PedidoDetalleView> detalle = pedidoDAO.listarDetallePorPedido(pedidoId);
            tablaDetalle.setItems(FXCollections.observableArrayList(detalle));
            actualizarTotales(detalle);
            actualizarEstadoEhistorial(detalle);
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("No se pudo cargar el detalle del pedido");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    private void actualizarTotales(List<PedidoDetalleView> detalle) {
        double subtotal = 0.0;
        for (PedidoDetalleView linea : detalle) {
            subtotal += linea.getTotal();
        }

        int n = detalle.size();
        lblProductosHeading.setText("Productos (" + n + ")");

        boolean tieneLineas = n > 0;
        double envio = tieneLineas ? ENVIO_ESTANDAR : 0.0;
        double descuento = 0.0;
        double total = subtotal + envio - descuento;

        lblSubtotal.setText(moneda.format(subtotal));
        lblEnvio.setText(moneda.format(envio));
        lblDescuento.setText("-" + moneda.format(descuento));
        lblTotalGeneral.setText(moneda.format(total));
    }

    private void actualizarEstadoEhistorial(List<PedidoDetalleView> detalle) {
        boolean tieneLineas = !detalle.isEmpty();

        if (tieneLineas) {
            lblBadgeEstado.setText("Completado");
            lblBadgeEstado.getStyleClass().setAll("status-badge");
            lblResumenEstado.setText("Estado: completado · listo para facturación.");
            lblHist4Title.setText("Pedido completado");
            lblHist4Check.setText("*");
            lblHist4Check.getStyleClass().setAll("timeline-check");
        } else {
            lblBadgeEstado.setText("Pendiente");
            lblBadgeEstado.getStyleClass().setAll("status-badge", "status-badge-pending");
            lblResumenEstado.setText("Estado: pendiente · sin líneas en el pedido.");
            lblHist4Title.setText("Completar pedido");
            lblHist4Check.setText("…");
            lblHist4Check.getStyleClass().setAll("detail-muted");
        }

        String base = lblSubtituloFecha.getText();
        if ("—".equals(base)) {
            base = "Horario referencial";
        }
        lblHist1.setText(base);
        lblHist2.setText(base);
        lblHist3.setText(base);
        lblHist4.setText(base);
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

    private static String nullToDash(String s) {
        return s == null || s.isBlank() ? "—" : s;
    }

    private void mostrarDemo(String titulo, String mensaje) {
        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle(titulo);
        info.setHeaderText(null);
        info.setContentText(mensaje);
        info.showAndWait();
    }

    private void mostrarAviso(String cabecera, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Aviso");
        alert.setHeaderText(cabecera);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
