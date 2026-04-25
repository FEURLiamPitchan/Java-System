// Test database connection and operations
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class TestDatabase {
    public static void main(String[] args) {
        try {
            // Test connection
            Connection conn = DatabaseConnection.getConnection();
            System.out.println("Database connection successful!");
            
            // Test if users table exists and has data
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users");
            if (rs.next()) {
                System.out.println("Users table has " + rs.getInt(1) + " records");
            }
            
            // List all users
            rs = stmt.executeQuery("SELECT email, role FROM users");
            System.out.println("Current users:");
            while (rs.next()) {
                System.out.println("- " + rs.getString("email") + " (" + rs.getString("role") + ")");
            }
            
            // Test insert
            PreparedStatement insertStmt = conn.prepareStatement(
                "INSERT INTO users (email, password, role, date_created) VALUES (?, ?, ?, ?)");
            insertStmt.setString(1, "test@email.com");
            insertStmt.setString(2, "test123");
            insertStmt.setString(3, "resident");
            insertStmt.setString(4, "2024-06-15");
            
            int result = insertStmt.executeUpdate();
            System.out.println("Insert result: " + result + " row(s) affected");
            
            rs.close();
            stmt.close();
            insertStmt.close();
            conn.close();
            
        } catch (Exception e) {
            System.err.println("Database test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}