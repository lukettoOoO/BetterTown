package com.mycompany.bettertown.login;
import com.mycompany.bettertown.IssueData;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;
import java.sql.Statement;

public class DatabaseLogic {
    private static final String URL = "jdbc:mysql://localhost:3306/BetterTown";
    private static final String USER = "root";
    private static final String PASSWORD = "bia142004";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
//this is for register with database
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
    
    public static void saveWaypoint(IssueData data) {
    String sql = "INSERT INTO issue (title, description, photo, priority, city, address, date, username, status, latitude, longitude) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setString(1, data.getTitle());
        stmt.setString(2, data.getDescription());
        stmt.setString(3, data.getPhoto());
        stmt.setInt(4, data.getPriority());
        stmt.setString(5, data.getCity());
        stmt.setString(6, data.getAddress());
        stmt.setDate(7, new java.sql.Date(data.getDate().getTime()));
        stmt.setString(8, data.getUsername());
        stmt.setString(9, data.getStatus());
        stmt.setDouble(10, data.getLatitude());
        stmt.setDouble(11, data.getLongitude());

        stmt.executeUpdate();
    } catch (SQLException e) {
        e.printStackTrace();
    }
}
    
    public static List<IssueData> getAllIssues() {
    List<IssueData> issues = new ArrayList<>();
    String sql = "SELECT * FROM issue";

    try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
        while (rs.next()) {
            IssueData data = new IssueData();
            data.setTitle(rs.getString("title"));
            data.setDescription(rs.getString("description"));
            data.setPhoto(rs.getString("photo"));
            data.setPriority(rs.getInt("priority"));
            data.setCity(rs.getString("city"));
            data.setAddress(rs.getString("address"));
            data.setDate(rs.getDate("date"));
            data.setUsername(rs.getString("username"));
            data.setStatus(rs.getString("status"));
            data.setLatitude(rs.getDouble("latitude"));
            data.setLongitude(rs.getDouble("longitude"));

            issues.add(data);
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }

    return issues;
}

}