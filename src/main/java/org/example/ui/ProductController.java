package org.example.ui;

import dao.ProductoDAO;
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
import model.Producto;

import java.util.List;

public class ProductController {
    @FXML
    private TableView<Producto> tablaProductos;

    @FXML
    private TableColumn<Producto, Integer> colId;

    @FXML
    private TableColumn<Producto, String> colNombre;

    @FXML
    private TableColumn<Producto, Double> colPrecio;

    @FXML
    private TableColumn<Producto, Integer> colStock;

    @FXML
    private TextField txtNombre;

    @FXML
    private TextField txtPrecio;

    @FXML
    private TextField txtStock;

    @FXML
    private TextField txtId;

    private final ProductoDAO productoDAO = new ProductoDAO();

    @FXML
    private void initialize() {
        tablaProductos.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));

        cargarProductos();
    }

    @FXML
    private void onListarProductos() {
        cargarProductos();
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
            FXMLLoader fxml = new FXMLLoader(getClass().getResource(rutaFxml));
            Scene scene = new Scene(fxml.load());

            Stage stage = (Stage) tablaProductos.getScene().getWindow();
            WindowUtil.applyWindowSettings(stage, scene, titulo);
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("No se pudo abrir la vista: " + titulo);
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void onAnadirProducto() {
        String nombre = txtNombre.getText() == null ? "" : txtNombre.getText().trim();
        String precioTexto = txtPrecio.getText() == null ? "" : txtPrecio.getText().trim();
        String stockTexto = txtStock.getText() == null ? "" : txtStock.getText().trim();

        if (nombre.isEmpty() || precioTexto.isEmpty() || stockTexto.isEmpty()) {
            mostrarAviso("Campos incompletos",
                    "Debes rellenar nombre, precio y stock antes de añadir el producto.");
            return;
        }

        double precio;
        int stock;
        try {
            precio = Double.parseDouble(precioTexto.replace(',', '.'));
            stock = Integer.parseInt(stockTexto);
        } catch (NumberFormatException e) {
            mostrarAviso("Datos no válidos",
                    "El precio debe ser un número (ej. 9.99) y el stock un entero.");
            return;
        }

        if (precio < 0 || stock < 0) {
            mostrarAviso("Datos no válidos",
                    "El precio y el stock no pueden ser negativos.");
            return;
        }

        try {
            Producto nuevo = new Producto(nombre, precio, stock);
            productoDAO.insertarProducto(nuevo);

            cargarProductos();
            limpiarCampos();
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("No se pudo añadir el producto");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void onActualizarProducto() {
        String idTexto = txtId.getText() == null ? "" : txtId.getText().trim();

        if (idTexto.isEmpty()) {
            mostrarAviso("Campo ID vacío",
                    "Debes introducir el ID del producto que quieres actualizar.");
            return;
        }

        int id;
        try {
            id = Integer.parseInt(idTexto);
        } catch (NumberFormatException e) {
            mostrarAviso("ID no válido",
                    "El ID debe ser un número entero.");
            return;
        }

        Producto existente;
        try {
            existente = productoDAO.buscarPorId(id);
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("No se pudo buscar el producto");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
            return;
        }

        if (existente == null) {
            mostrarAviso("Producto no encontrado",
                    "No existe ningún producto con el ID " + id + ".");
            return;
        }

        String nombreTexto = txtNombre.getText() == null ? "" : txtNombre.getText().trim();
        String precioTexto = txtPrecio.getText() == null ? "" : txtPrecio.getText().trim();
        String stockTexto = txtStock.getText() == null ? "" : txtStock.getText().trim();

        if (nombreTexto.isEmpty() && precioTexto.isEmpty() && stockTexto.isEmpty()) {
            mostrarAviso("Sin cambios",
                    "Rellena al menos un campo (nombre, precio o stock) para actualizar.");
            return;
        }

        String nuevoNombre = existente.getNombre();
        double nuevoPrecio = existente.getPrecio();
        int nuevoStock = existente.getStock();

        if (!nombreTexto.isEmpty()) {
            nuevoNombre = nombreTexto;
        }

        if (!precioTexto.isEmpty()) {
            try {
                nuevoPrecio = Double.parseDouble(precioTexto.replace(',', '.'));
            } catch (NumberFormatException e) {
                mostrarAviso("Precio no válido",
                        "El precio debe ser un número (ej. 9.99).");
                return;
            }
            if (nuevoPrecio < 0) {
                mostrarAviso("Precio no válido", "El precio no puede ser negativo.");
                return;
            }
        }

        if (!stockTexto.isEmpty()) {
            try {
                nuevoStock = Integer.parseInt(stockTexto);
            } catch (NumberFormatException e) {
                mostrarAviso("Stock no válido",
                        "El stock debe ser un número entero.");
                return;
            }
            if (nuevoStock < 0) {
                mostrarAviso("Stock no válido", "El stock no puede ser negativo.");
                return;
            }
        }

        try {
            Producto actualizado = new Producto(id, nuevoNombre, nuevoPrecio, nuevoStock);
            boolean ok = productoDAO.actualizarProducto(actualizado);
            if (ok) {
                cargarProductos();
                limpiarCampos();
            } else {
                mostrarAviso("Sin cambios",
                        "No se ha podido actualizar el producto con ID " + id + ".");
            }
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("No se pudo actualizar el producto");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void onEliminarProducto() {
        String idTexto = txtId.getText() == null ? "" : txtId.getText().trim();

        if (idTexto.isEmpty()) {
            mostrarAviso("Campo ID vacío",
                    "Debes introducir el ID del producto que quieres eliminar.");
            return;
        }

        int id;
        try {
            id = Integer.parseInt(idTexto);
        } catch (NumberFormatException e) {
            mostrarAviso("ID no válido",
                    "El ID debe ser un número entero.");
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar eliminación");
        confirmacion.setHeaderText("¿Eliminar el producto con ID " + id + "?");
        confirmacion.setContentText("Esta acción no se puede deshacer.");

        confirmacion.showAndWait().ifPresent(respuesta -> {
            if (respuesta == javafx.scene.control.ButtonType.OK) {
                try {
                    boolean eliminado = productoDAO.eliminarProducto(id);
                    if (eliminado) {
                        cargarProductos();
                        limpiarCampos();
                    } else {
                        mostrarAviso("Sin cambios",
                                "No existe ningún producto con el ID " + id + ".");
                    }
                } catch (Exception e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText("No se pudo eliminar el producto");
                    alert.setContentText(e.getMessage());
                    alert.showAndWait();
                }
            }
        });
    }

    private void cargarProductos() {
        try {
            List<Producto> productos = productoDAO.listarProductos();
            tablaProductos.setItems(FXCollections.observableArrayList(productos));
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("No se pudieron cargar los productos");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    private void limpiarCampos() {
        txtNombre.clear();
        txtPrecio.clear();
        txtStock.clear();
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
