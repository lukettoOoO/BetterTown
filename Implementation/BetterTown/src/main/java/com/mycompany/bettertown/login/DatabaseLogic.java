package com.mycompany.bettertown.login;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseLogic {
    private static final String URL = "jdbc:mysql://localhost:3306/BetterTown";
    private static final String USER = "root";
    private static final String PASSWORD = "bia142004";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static ProfileData getUserByEmailAndStatus(Connection conn, String email, String status) throws SQLException {
        String sql = "SELECT name, city, password, status FROM users WHERE email = ? AND status = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, email);
        stmt.setString(2, status);
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            return new ProfileData(rs.getString("name"), rs.getString("city"), rs.getString("password"), email, rs.getString("status"));
        }
        return null;
    }
}