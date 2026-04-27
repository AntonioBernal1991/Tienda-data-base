package dao;

import model.Cliente;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ClienteDAO {
    private void asegurarColumnaCiudad(Connection con) throws SQLException {
        String sql = "ALTER TABLE clientes ADD COLUMN IF NOT EXISTS ciudad VARCHAR(100) NOT NULL DEFAULT ''";
        try (Statement st = con.createStatement()) {
            st.execute(sql);
        }
    }

    public void insertarCliente(Cliente cliente) {
        String sql = "INSERT INTO clientes (nombre, email, ciudad) VALUES (?, ?, ?)";

        try (Connection con = ConexionBD.getConnection()) {
            asegurarColumnaCiudad(con);
            try (PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setString(1, cliente.getNombre());
                ps.setString(2, cliente.getEmail());
                ps.setString(3, cliente.getCiudad());
                ps.executeUpdate();
            }

            System.out.println("Cliente insertado correctamente.");

        } catch (SQLException e) {
            System.out.println("Error al insertar cliente: " + e.getMessage());
            throw new RuntimeException("Error al insertar cliente: " + e.getMessage(), e);
        }
    }

    public List<Cliente> listarClientes() {
        List<Cliente> lista = new ArrayList<>();
        String sql = "SELECT * FROM clientes ORDER BY id";

        try (Connection con = ConexionBD.getConnection()) {
            asegurarColumnaCiudad(con);
            try (Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery(sql)) {

                while (rs.next()) {
                    Cliente cliente = new Cliente(
                            rs.getInt("id"),
                            rs.getString("nombre"),
                            rs.getString("email"),
                            rs.getString("ciudad")
                    );
                    lista.add(cliente);
                }
            }

        } catch (SQLException e) {
            System.out.println("Error al listar clientes: " + e.getMessage());
            throw new RuntimeException("Error al listar clientes: " + e.getMessage(), e);
        }

        return lista;
    }

    public Cliente buscarPorId(int id) {
        String sql = "SELECT * FROM clientes WHERE id = ?";

        try (Connection con = ConexionBD.getConnection()) {
            asegurarColumnaCiudad(con);
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, id);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return new Cliente(
                                rs.getInt("id"),
                                rs.getString("nombre"),
                                rs.getString("email"),
                                rs.getString("ciudad")
                        );
                    }
                }
            }

            return null;
        } catch (SQLException e) {
            System.out.println("Error al buscar cliente: " + e.getMessage());
            throw new RuntimeException("Error al buscar cliente: " + e.getMessage(), e);
        }
    }

    public boolean actualizarCliente(Cliente cliente) {
        String sql = "UPDATE clientes SET nombre = ?, email = ?, ciudad = ? WHERE id = ?";

        try (Connection con = ConexionBD.getConnection()) {
            asegurarColumnaCiudad(con);
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, cliente.getNombre());
                ps.setString(2, cliente.getEmail());
                ps.setString(3, cliente.getCiudad());
                ps.setInt(4, cliente.getId());

                int filas = ps.executeUpdate();

                if (filas > 0) {
                    System.out.println("Cliente actualizado correctamente.");
                    return true;
                }

                System.out.println("No existe un cliente con ese id.");
                return false;
            }
        } catch (SQLException e) {
            System.out.println("Error al actualizar cliente: " + e.getMessage());
            throw new RuntimeException("Error al actualizar cliente: " + e.getMessage(), e);
        }
    }
}
