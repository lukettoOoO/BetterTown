package com.mycompany.bettertown.login;


import static com.mycompany.bettertown.ImageConverter.byteArrayToImageIcon;
import static com.mycompany.bettertown.ImageConverter.imageIconToByteArray;
import com.mycompany.bettertown.IssueData;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;
import java.sql.Statement;
import javax.swing.ImageIcon;

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
    String sql = "INSERT INTO issue (title, description, image_data, priority, city, address, date, username, status, latitude, longitude) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
        stmt.setString(1, data.getTitle());
        stmt.setString(2, data.getDescription());
        //stmt.setString(3, data.getImage());
        byte[] photoBytes=null;
        ImageIcon imageIcon=data.getImage();
        if(imageIcon!=null)
        {
        try {
        photoBytes=imageIconToByteArray(imageIcon,"png");
        } catch (IOException e){
        System.err.println("Error converting image to bytes");
        e.printStackTrace();
        }
        }
        stmt.setBytes(3,photoBytes);
        stmt.setInt(4, data.getPriority());
        stmt.setString(5, data.getCity());
        stmt.setString(6, data.getAddress());
        stmt.setDate(7, new java.sql.Date(data.getDate().getTime()));
        stmt.setString(8, data.getUsername());
        stmt.setString(9, data.getStatus());
        stmt.setDouble(10, data.getLatitude());
        stmt.setDouble(11, data.getLongitude());

        stmt.executeUpdate();
        ResultSet rs = stmt.getGeneratedKeys();
        System.out.println("Generated keys: " + rs);
        if (rs.next()) {
            int generatedId = rs.getInt(1);
            data.setId(generatedId);
            System.out.println("Generated ID: " + generatedId);
        } else {
            System.out.println("No generated keys found.");
        }
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
            data.setId(rs.getInt("id"));
            data.setTitle(rs.getString("title"));
            data.setDescription(rs.getString("description"));
            //data.setPhoto(rs.getString("photo"));
            byte[] photoBytes = rs.getBytes("image_data"); // Citim tabloul de octeți din coloana 'photo'
                if (photoBytes != null) {
                    try {
                        ImageIcon retrievedIcon = byteArrayToImageIcon(photoBytes);
                        data.setImage(retrievedIcon); // Setează ImageIcon-ul în obiectul IssueData
                    } catch (IOException e) {
                        System.err.println("Eroare la conversia datelor imagine în ImageIcon: " + e.getMessage());
                        // Poți alege să setezi imaginea ca null sau să gestionezi eroarea diferit
                        data.setImage(null);
                    }
                } else {
                    data.setImage(null); // Setează imaginea ca null dacă nu există date foto
                }
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
    
    public static void deleteIssue(IssueData data) {
    String sql = "DELETE FROM issue WHERE id = ?";

    try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
        System.out.println("ID: " + data.getId());
        stmt.setInt(1, data.getId());
        stmt.executeUpdate();
    } catch (SQLException e) {
        e.printStackTrace();
    }
}

    
    public static void updateWaypoint(IssueData data) {
    String sql = "UPDATE issue SET title = ?, description = ?, image_data = ?, priority = ?, city = ?, address = ?, date = ?, username = ?, status = ?, latitude = ?, longitude = ? WHERE id = ?";

    try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setString(1,data.getTitle());
        stmt.setString(2, data.getDescription());
 
        byte[] photoBytes=null;
        ImageIcon imageIcon=data.getImage();
        if(imageIcon!=null)
        {
        try {
        photoBytes=imageIconToByteArray(imageIcon,"png");
        } catch (IOException e){
        System.err.println("Error converting image to bytes");
        e.printStackTrace();
        }
        }
        stmt.setBytes(3,photoBytes);
        stmt.setInt(4, data.getPriority());
        stmt.setString(5, data.getCity());
        stmt.setString(6, data.getAddress());
        stmt.setDate(7, new java.sql.Date(data.getDate().getTime()));
        stmt.setString(8, data.getUsername());
        stmt.setString(9, data.getStatus());
        stmt.setDouble(10, data.getLatitude());
        stmt.setDouble(11, data.getLongitude());
        stmt.setInt(12, data.getId());
        if (data.getId() == 0) {
            System.err.println("Eroare: ID-ul pentru update este 0 sau never set!");
        }
        System.out.println("Updating issue with ID: " + data.getId());
        int rowsAffected = stmt.executeUpdate();
        System.out.println("Rows updated: " + rowsAffected);
    } catch (SQLException e) {
        e.printStackTrace();
    }
}
}