package dao;

import model.PedidoDetalleView;
import model.PedidoClienteView;

import java.util.ArrayList;
import java.util.List;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class PedidoDAO {

    public int crearPedido(int clienteId) {
        String sql = "INSERT INTO pedidos (cliente_id, fecha) VALUES (?, ?)";

        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, clienteId);
            ps.setDate(2, new Date(System.currentTimeMillis()));

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int nuevoId = keys.getInt(1);
                    System.out.println("Pedido creado correctamente con ID: " + nuevoId);
                    return nuevoId;
                }
            }

            System.out.println("Pedido creado, pero no se pudo recuperar el ID.");
            return -1;

        } catch (SQLException e) {
            System.out.println("Error al crear pedido: " + e.getMessage());
            return -1;
        }
    }

    public void listarPedidosConCliente() {
        String sql = """
                SELECT p.id, p.fecha, c.nombre
                FROM pedidos p
                JOIN clientes c ON p.cliente_id = c.id
                ORDER BY p.id
                """;

        try (Connection con = ConexionBD.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            System.out.println("\n--- LISTA DE PEDIDOS ---");

            while (rs.next()) {
                System.out.println("Pedido ID: " + rs.getInt("id")
                        + " | Fecha: " + rs.getDate("fecha")
                        + " | Cliente: " + rs.getString("nombre"));
            }

        } catch (SQLException e) {
            System.out.println("Error al listar pedidos: " + e.getMessage());
        }
    }

    public int crearPedidoConDetalle(int clienteId, int productoId, int cantidad) {
        String sqlPedido = "INSERT INTO pedidos (cliente_id, fecha) VALUES (?, ?)";
        String sqlDetalle = "INSERT INTO detalle_pedido (pedido_id, producto_id, cantidad) VALUES (?, ?, ?)";
        String sqlStockProducto = "SELECT stock FROM productos WHERE id = ?";
        String sqlActualizarStock = "UPDATE productos SET stock = stock - ? WHERE id = ? AND stock >= ?";

        try (Connection con = ConexionBD.getConnection()) {
            con.setAutoCommit(false);

            try {
                int pedidoId;

                try (PreparedStatement psPedido = con.prepareStatement(sqlPedido, Statement.RETURN_GENERATED_KEYS)) {
                    psPedido.setInt(1, clienteId);
                    psPedido.setDate(2, new Date(System.currentTimeMillis()));
                    psPedido.executeUpdate();

                    try (ResultSet keys = psPedido.getGeneratedKeys()) {
                        if (!keys.next()) {
                            con.rollback();
                            throw new SQLException("No se pudo recuperar el ID del pedido generado.");
                        }
                        pedidoId = keys.getInt(1);
                    }
                }

                try (PreparedStatement psDetalle = con.prepareStatement(sqlDetalle)) {
                    psDetalle.setInt(1, pedidoId);
                    psDetalle.setInt(2, productoId);
                    psDetalle.setInt(3, cantidad);
                    psDetalle.executeUpdate();
                }

                int stockActual;
                try (PreparedStatement psStock = con.prepareStatement(sqlStockProducto)) {
                    psStock.setInt(1, productoId);
                    try (ResultSet rsStock = psStock.executeQuery()) {
                        if (!rsStock.next()) {
                            con.rollback();
                            throw new SQLException("No existe producto con ID " + productoId + ".");
                        }
                        stockActual = rsStock.getInt("stock");
                    }
                }

                if (stockActual < cantidad) {
                    con.rollback();
                    throw new SQLException("Stock insuficiente para el producto " + productoId
                            + ". Stock actual: " + stockActual + ", solicitado: " + cantidad + ".");
                }

                try (PreparedStatement psActualizarStock = con.prepareStatement(sqlActualizarStock)) {
                    psActualizarStock.setInt(1, cantidad);
                    psActualizarStock.setInt(2, productoId);
                    psActualizarStock.setInt(3, cantidad);
                    int filasAfectadas = psActualizarStock.executeUpdate();
                    if (filasAfectadas == 0) {
                        con.rollback();
                        throw new SQLException("No se pudo actualizar el stock del producto " + productoId + ".");
                    }
                }

                con.commit();
                System.out.println("Pedido creado con detalle. ID: " + pedidoId);
                return pedidoId;
            } catch (SQLException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.out.println("Error al crear pedido con detalle: " + e.getMessage());
            throw new RuntimeException("Error al crear pedido con detalle: " + e.getMessage(), e);
        }
    }

    public List<PedidoDetalleView> listarPedidosDetallados() {
        List<PedidoDetalleView> lista = new ArrayList<>();
        String sql = """
                SELECT p.id AS pedido_id, pr.id AS producto_id, p.fecha, c.nombre AS cliente, pr.nombre AS producto,
                       dp.cantidad, pr.precio, (dp.cantidad * pr.precio) AS total
                FROM detalle_pedido dp
                JOIN pedidos p ON dp.pedido_id = p.id
                JOIN clientes c ON p.cliente_id = c.id
                JOIN productos pr ON dp.producto_id = pr.id
                ORDER BY p.id
                """;

        try (Connection con = ConexionBD.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                PedidoDetalleView fila = new PedidoDetalleView(
                        rs.getInt("pedido_id"),
                        rs.getInt("producto_id"),
                        rs.getDate("fecha"),
                        rs.getString("cliente"),
                        rs.getString("producto"),
                        rs.getInt("cantidad"),
                        rs.getDouble("precio"),
                        rs.getDouble("total")
                );
                lista.add(fila);
            }

        } catch (SQLException e) {
            System.out.println("Error al listar pedidos detallados: " + e.getMessage());
            throw new RuntimeException("Error al listar pedidos detallados: " + e.getMessage(), e);
        }

        return lista;
    }

    public List<PedidoClienteView> listarPedidosResumen() {
        List<PedidoClienteView> lista = new ArrayList<>();
        String sql = """
                SELECT p.id AS pedido_id, p.fecha, c.nombre AS cliente, c.ciudad
                FROM pedidos p
                JOIN clientes c ON p.cliente_id = c.id
                ORDER BY p.id
                """;

        try (Connection con = ConexionBD.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                PedidoClienteView fila = new PedidoClienteView(
                        rs.getInt("pedido_id"),
                        rs.getDate("fecha"),
                        rs.getString("cliente"),
                        rs.getString("ciudad")
                );
                lista.add(fila);
            }
        } catch (SQLException e) {
            System.out.println("Error al listar pedidos resumen: " + e.getMessage());
            throw new RuntimeException("Error al listar pedidos resumen: " + e.getMessage(), e);
        }

        return lista;
    }

    public PedidoClienteView obtenerPedidoResumenPorId(int pedidoId) {
        String sql = """
                SELECT p.id AS pedido_id, p.fecha, c.nombre AS cliente, c.ciudad
                FROM pedidos p
                JOIN clientes c ON p.cliente_id = c.id
                WHERE p.id = ?
                """;

        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, pedidoId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new PedidoClienteView(
                            rs.getInt("pedido_id"),
                            rs.getDate("fecha"),
                            rs.getString("cliente"),
                            rs.getString("ciudad")
                    );
                }
            }
            return null;
        } catch (SQLException e) {
            System.out.println("Error al obtener resumen del pedido: " + e.getMessage());
            throw new RuntimeException("Error al obtener resumen del pedido: " + e.getMessage(), e);
        }
    }

    public List<PedidoDetalleView> listarDetallePorPedido(int pedidoId) {
        List<PedidoDetalleView> lista = new ArrayList<>();
        String sql = """
                SELECT p.id AS pedido_id, pr.id AS producto_id, p.fecha, c.nombre AS cliente, pr.nombre AS producto,
                       SUM(dp.cantidad) AS cantidad, pr.precio, (SUM(dp.cantidad) * pr.precio) AS total
                FROM detalle_pedido dp
                JOIN pedidos p ON dp.pedido_id = p.id
                JOIN clientes c ON p.cliente_id = c.id
                JOIN productos pr ON dp.producto_id = pr.id
                WHERE p.id = ?
                GROUP BY p.id, pr.id, p.fecha, c.nombre, pr.nombre, pr.precio
                ORDER BY pr.id
                """;

        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, pedidoId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    PedidoDetalleView fila = new PedidoDetalleView(
                            rs.getInt("pedido_id"),
                            rs.getInt("producto_id"),
                            rs.getDate("fecha"),
                            rs.getString("cliente"),
                            rs.getString("producto"),
                            rs.getInt("cantidad"),
                            rs.getDouble("precio"),
                            rs.getDouble("total")
                    );
                    lista.add(fila);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error al listar detalle del pedido: " + e.getMessage());
            throw new RuntimeException("Error al listar detalle del pedido: " + e.getMessage(), e);
        }

        return lista;
    }

    public void agregarProductoAPedido(int pedidoId, int productoId, int cantidad) {
        String sqlExistePedido = "SELECT id FROM pedidos WHERE id = ?";
        String sqlStockProducto = "SELECT stock FROM productos WHERE id = ?";
        String sqlExisteDetalle = "SELECT id, cantidad FROM detalle_pedido WHERE pedido_id = ? AND producto_id = ? LIMIT 1";
        String sqlInsertDetalle = "INSERT INTO detalle_pedido (pedido_id, producto_id, cantidad) VALUES (?, ?, ?)";
        String sqlActualizarDetalle = "UPDATE detalle_pedido SET cantidad = cantidad + ? WHERE id = ?";
        String sqlActualizarStock = "UPDATE productos SET stock = stock - ? WHERE id = ? AND stock >= ?";

        try (Connection con = ConexionBD.getConnection()) {
            con.setAutoCommit(false);

            try {
                try (PreparedStatement psExiste = con.prepareStatement(sqlExistePedido)) {
                    psExiste.setInt(1, pedidoId);
                    try (ResultSet rsExiste = psExiste.executeQuery()) {
                        if (!rsExiste.next()) {
                            con.rollback();
                            throw new SQLException("No existe pedido con ID " + pedidoId + ".");
                        }
                    }
                }

                int stockActual;
                try (PreparedStatement psStock = con.prepareStatement(sqlStockProducto)) {
                    psStock.setInt(1, productoId);
                    try (ResultSet rsStock = psStock.executeQuery()) {
                        if (!rsStock.next()) {
                            con.rollback();
                            throw new SQLException("No existe producto con ID " + productoId + ".");
                        }
                        stockActual = rsStock.getInt("stock");
                    }
                }

                if (stockActual < cantidad) {
                    con.rollback();
                    throw new SQLException("Stock insuficiente para el producto " + productoId
                            + ". Stock actual: " + stockActual + ", solicitado: " + cantidad + ".");
                }

                Integer detalleId = null;
                try (PreparedStatement psExisteDetalle = con.prepareStatement(sqlExisteDetalle)) {
                    psExisteDetalle.setInt(1, pedidoId);
                    psExisteDetalle.setInt(2, productoId);
                    try (ResultSet rsDetalle = psExisteDetalle.executeQuery()) {
                        if (rsDetalle.next()) {
                            detalleId = rsDetalle.getInt("id");
                        }
                    }
                }

                if (detalleId == null) {
                    try (PreparedStatement psInsertDetalle = con.prepareStatement(sqlInsertDetalle)) {
                        psInsertDetalle.setInt(1, pedidoId);
                        psInsertDetalle.setInt(2, productoId);
                        psInsertDetalle.setInt(3, cantidad);
                        psInsertDetalle.executeUpdate();
                    }
                } else {
                    try (PreparedStatement psActualizarDetalle = con.prepareStatement(sqlActualizarDetalle)) {
                        psActualizarDetalle.setInt(1, cantidad);
                        psActualizarDetalle.setInt(2, detalleId);
                        psActualizarDetalle.executeUpdate();
                    }
                }

                try (PreparedStatement psActualizarStock = con.prepareStatement(sqlActualizarStock)) {
                    psActualizarStock.setInt(1, cantidad);
                    psActualizarStock.setInt(2, productoId);
                    psActualizarStock.setInt(3, cantidad);
                    int filas = psActualizarStock.executeUpdate();
                    if (filas == 0) {
                        con.rollback();
                        throw new SQLException("No se pudo actualizar el stock del producto " + productoId + ".");
                    }
                }

                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.out.println("Error al añadir producto al pedido: " + e.getMessage());
            throw new RuntimeException("Error al añadir producto al pedido: " + e.getMessage(), e);
        }
    }

    public void eliminarProductoDePedido(int pedidoId, int productoId, int cantidad) {
        String sqlExistePedido = "SELECT id FROM pedidos WHERE id = ?";
        String sqlCantidadActual = "SELECT COALESCE(SUM(cantidad), 0) AS cantidad_total FROM detalle_pedido WHERE pedido_id = ? AND producto_id = ?";
        String sqlBorrarLineas = "DELETE FROM detalle_pedido WHERE pedido_id = ? AND producto_id = ?";
        String sqlInsertRestante = "INSERT INTO detalle_pedido (pedido_id, producto_id, cantidad) VALUES (?, ?, ?)";
        String sqlReponerStock = "UPDATE productos SET stock = stock + ? WHERE id = ?";

        try (Connection con = ConexionBD.getConnection()) {
            con.setAutoCommit(false);

            try {
                try (PreparedStatement psExiste = con.prepareStatement(sqlExistePedido)) {
                    psExiste.setInt(1, pedidoId);
                    try (ResultSet rsExiste = psExiste.executeQuery()) {
                        if (!rsExiste.next()) {
                            con.rollback();
                            throw new SQLException("No existe pedido con ID " + pedidoId + ".");
                        }
                    }
                }

                int cantidadActual;
                try (PreparedStatement psCantidadActual = con.prepareStatement(sqlCantidadActual)) {
                    psCantidadActual.setInt(1, pedidoId);
                    psCantidadActual.setInt(2, productoId);
                    try (ResultSet rsCantidad = psCantidadActual.executeQuery()) {
                        rsCantidad.next();
                        cantidadActual = rsCantidad.getInt("cantidad_total");
                    }
                }

                if (cantidadActual <= 0) {
                    con.rollback();
                    throw new SQLException("El producto " + productoId + " no está en el pedido " + pedidoId + ".");
                }
                if (cantidad > cantidadActual) {
                    con.rollback();
                    throw new SQLException("No puedes eliminar " + cantidad + " unidades. Cantidad actual: " + cantidadActual + ".");
                }

                int cantidadRestante = cantidadActual - cantidad;

                try (PreparedStatement psBorrar = con.prepareStatement(sqlBorrarLineas)) {
                    psBorrar.setInt(1, pedidoId);
                    psBorrar.setInt(2, productoId);
                    psBorrar.executeUpdate();
                }

                if (cantidadRestante > 0) {
                    try (PreparedStatement psInsertRestante = con.prepareStatement(sqlInsertRestante)) {
                        psInsertRestante.setInt(1, pedidoId);
                        psInsertRestante.setInt(2, productoId);
                        psInsertRestante.setInt(3, cantidadRestante);
                        psInsertRestante.executeUpdate();
                    }
                }

                try (PreparedStatement psReponer = con.prepareStatement(sqlReponerStock)) {
                    psReponer.setInt(1, cantidad);
                    psReponer.setInt(2, productoId);
                    int filas = psReponer.executeUpdate();
                    if (filas == 0) {
                        con.rollback();
                        throw new SQLException("No se pudo reponer stock del producto " + productoId + ".");
                    }
                }

                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.out.println("Error al eliminar producto del pedido: " + e.getMessage());
            throw new RuntimeException("Error al eliminar producto del pedido: " + e.getMessage(), e);
        }
    }

    public List<Integer> listarIdsPedidos() {
        List<Integer> ids = new ArrayList<>();
        String sql = "SELECT id FROM pedidos ORDER BY id";

        try (Connection con = ConexionBD.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                ids.add(rs.getInt("id"));
            }
        } catch (SQLException e) {
            System.out.println("Error al listar IDs de pedidos: " + e.getMessage());
            throw new RuntimeException("Error al listar IDs de pedidos: " + e.getMessage(), e);
        }

        return ids;
    }

    public boolean eliminarPedidoYReponerStock(int pedidoId) {
        String sqlExistePedido = "SELECT id FROM pedidos WHERE id = ?";
        String sqlDetalles = """
                SELECT producto_id, SUM(cantidad) AS cantidad_total
                FROM detalle_pedido
                WHERE pedido_id = ?
                GROUP BY producto_id
                """;
        String sqlReponerStock = "UPDATE productos SET stock = stock + ? WHERE id = ?";
        String sqlEliminarDetalles = "DELETE FROM detalle_pedido WHERE pedido_id = ?";
        String sqlEliminarPedido = "DELETE FROM pedidos WHERE id = ?";

        try (Connection con = ConexionBD.getConnection()) {
            con.setAutoCommit(false);

            try {
                try (PreparedStatement psExiste = con.prepareStatement(sqlExistePedido)) {
                    psExiste.setInt(1, pedidoId);
                    try (ResultSet rsExiste = psExiste.executeQuery()) {
                        if (!rsExiste.next()) {
                            con.rollback();
                            return false;
                        }
                    }
                }

                try (PreparedStatement psDetalles = con.prepareStatement(sqlDetalles)) {
                    psDetalles.setInt(1, pedidoId);
                    try (ResultSet rsDetalles = psDetalles.executeQuery()) {
                        while (rsDetalles.next()) {
                            int productoId = rsDetalles.getInt("producto_id");
                            int cantidadTotal = rsDetalles.getInt("cantidad_total");

                            try (PreparedStatement psReponer = con.prepareStatement(sqlReponerStock)) {
                                psReponer.setInt(1, cantidadTotal);
                                psReponer.setInt(2, productoId);
                                int filas = psReponer.executeUpdate();
                                if (filas == 0) {
                                    con.rollback();
                                    throw new SQLException("No se pudo reponer stock del producto " + productoId + ".");
                                }
                            }
                        }
                    }
                }

                try (PreparedStatement psEliminarDetalles = con.prepareStatement(sqlEliminarDetalles)) {
                    psEliminarDetalles.setInt(1, pedidoId);
                    psEliminarDetalles.executeUpdate();
                }

                try (PreparedStatement psEliminarPedido = con.prepareStatement(sqlEliminarPedido)) {
                    psEliminarPedido.setInt(1, pedidoId);
                    int filasPedido = psEliminarPedido.executeUpdate();
                    if (filasPedido == 0) {
                        con.rollback();
                        return false;
                    }
                }

                con.commit();
                System.out.println("Pedido eliminado y stock repuesto. ID: " + pedidoId);
                return true;
            } catch (SQLException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.out.println("Error al eliminar pedido y reponer stock: " + e.getMessage());
            throw new RuntimeException("Error al eliminar pedido y reponer stock: " + e.getMessage(), e);
        }
    }
}
