package dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class PedidoDAO {

    public void crearPedido(int clienteId) {
        String sql = "INSERT INTO pedidos (cliente_id, fecha) VALUES (?, ?)";

        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, clienteId);
            ps.setDate(2, new Date(System.currentTimeMillis()));

            ps.executeUpdate();
            System.out.println("Pedido creado correctamente.");

        } catch (SQLException e) {
            System.out.println("Error al crear pedido: " + e.getMessage());
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
}
