package dao;

import org.example.database.DataBaseConnection;

import java.sql.Connection;
import java.sql.SQLException;

public class ConexionBD {
    public static Connection getConnection() throws SQLException {
        return DataBaseConnection.getConnection();
    }
}
