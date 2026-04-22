package dao;

import model.Producto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ProductoDAO {

    public void insertarProducto(Producto producto) {
        String sql = "INSERT INTO productos (nombre, precio, stock) VALUES (?, ?, ?)";

        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, producto.getNombre());
            ps.setDouble(2, producto.getPrecio());
            ps.setInt(3, producto.getStock());

            ps.executeUpdate();

            try (ResultSet claves = ps.getGeneratedKeys()) {
                if (claves.next()) {
                    producto.setId(claves.getInt(1));
                }
            }

            System.out.println("Producto insertado correctamente con id " + producto.getId() + ".");

        } catch (SQLException e) {
            System.out.println("Error al insertar producto: " + e.getMessage());
            throw new RuntimeException("Error al insertar producto: " + e.getMessage(), e);
        }
    }

    public List<Producto> listarProductos() {
        List<Producto> lista = new ArrayList<>();
        String sql = "SELECT * FROM productos ORDER BY id";

        try (Connection con = ConexionBD.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                Producto producto = new Producto(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getDouble("precio"),
                        rs.getInt("stock")
                );
                lista.add(producto);
            }

        } catch (SQLException e) {
            System.out.println("Error al listar productos: " + e.getMessage());
        }

        return lista;
    }

    public Producto buscarPorId(int id) {
        String sql = "SELECT * FROM productos WHERE id = ?";

        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Producto(
                            rs.getInt("id"),
                            rs.getString("nombre"),
                            rs.getDouble("precio"),
                            rs.getInt("stock")
                    );
                }
            }

            return null;

        } catch (SQLException e) {
            System.out.println("Error al buscar producto: " + e.getMessage());
            throw new RuntimeException("Error al buscar producto: " + e.getMessage(), e);
        }
    }

    public boolean actualizarProducto(Producto producto) {
        String sql = "UPDATE productos SET nombre = ?, precio = ?, stock = ? WHERE id = ?";

        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, producto.getNombre());
            ps.setDouble(2, producto.getPrecio());
            ps.setInt(3, producto.getStock());
            ps.setInt(4, producto.getId());

            int filas = ps.executeUpdate();

            if (filas > 0) {
                System.out.println("Producto actualizado correctamente.");
                return true;
            } else {
                System.out.println("No existe un producto con ese id.");
                return false;
            }

        } catch (SQLException e) {
            System.out.println("Error al actualizar producto: " + e.getMessage());
            throw new RuntimeException("Error al actualizar producto: " + e.getMessage(), e);
        }
    }

    public boolean eliminarProducto(int id) {
        String sql = "DELETE FROM productos WHERE id = ?";

        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            int filas = ps.executeUpdate();

            if (filas > 0) {
                System.out.println("Producto eliminado correctamente.");
                return true;
            } else {
                System.out.println("No existe un producto con ese id.");
                return false;
            }

        } catch (SQLException e) {
            System.out.println("Error al eliminar producto: " + e.getMessage());
            throw new RuntimeException("Error al eliminar producto: " + e.getMessage(), e);
        }
    }
}

