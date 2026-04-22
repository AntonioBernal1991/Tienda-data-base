package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DetallePedidoDAO {

    public void agregarProductoAPedido(int pedidoId, int productoId, int cantidad) {
        String sql = "INSERT INTO detalle_pedido (pedido_id, producto_id, cantidad) VALUES (?, ?, ?)";

        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, pedidoId);
            ps.setInt(2, productoId);
            ps.setInt(3, cantidad);

            ps.executeUpdate();
            System.out.println("Producto añadido al pedido.");

        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public void verDetallePedidos() {
        String sql = """
            SELECT p.id AS pedido_id, c.nombre AS cliente, pr.nombre AS producto, dp.cantidad
            FROM detalle_pedido dp
            JOIN pedidos p ON dp.pedido_id = p.id
            JOIN clientes c ON p.cliente_id = c.id
            JOIN productos pr ON dp.producto_id = pr.id
            ORDER BY p.id
        """;

        try (Connection con = ConexionBD.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            System.out.println("\n--- DETALLE DE PEDIDOS ---");

            while (rs.next()) {
                System.out.println(
                        "Pedido: " + rs.getInt("pedido_id") +
                                " | Cliente: " + rs.getString("cliente") +
                                " | Producto: " + rs.getString("producto") +
                                " | Cantidad: " + rs.getInt("cantidad")
                );
            }

        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}