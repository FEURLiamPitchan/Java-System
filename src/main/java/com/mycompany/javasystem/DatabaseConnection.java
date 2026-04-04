package com.mycompany.javasystem;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.io.File;
import java.io.IOException;

public class DatabaseConnection {

    private static final String DB_PATH = "C:\\Projects\\dashboard_resident\\barangay.accdb";
    private static final String URL = "jdbc:ucanaccess://" + DB_PATH + ";memory=false;showWarnings=false;sysSchema=true;immediatelyReleaseResources=true";

    // Static block to suppress UCanAccess warnings
    static {
        try {
            // Suppress UCanAccess and HSQLDB logging
            java.util.logging.Logger.getLogger("org.hsqldb").setLevel(java.util.logging.Level.SEVERE);
            java.util.logging.Logger.getLogger("net.ucanaccess").setLevel(java.util.logging.Level.SEVERE);
            java.util.logging.Logger.getLogger("hsqldb.db").setLevel(java.util.logging.Level.SEVERE);
            
            // Disable console warnings
            System.setProperty("hsqldb.reconfig_logging", "false");
            System.setProperty("textdb.allow_full_path", "true");
        } catch (Exception e) {
            // Ignore logging configuration errors
        }
    }

    public static Connection getConnection() throws SQLException {
        try {
            // Suppress UCanAccess warnings
            System.setProperty("hsqldb.reconfig_logging", "false");
            
            Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
            System.out.println("UCanAccess driver loaded successfully");
            
            // Check if database file exists, create if not
            File dbFile = new File(DB_PATH);
            System.out.println("Database path: " + DB_PATH);
            System.out.println("Database file exists: " + dbFile.exists());
            
            if (!dbFile.exists()) {
                System.out.println("Creating new Access database: " + DB_PATH);
                createAccessDatabase();
            }
            
            // Connect to Access database
            System.out.println("Attempting to connect to database...");
            Connection conn = DriverManager.getConnection(URL);
            System.out.println("Database connection established successfully");
            
            // Initialize tables if they don't exist
            initializeDatabaseTables(conn);
            
            System.out.println("Successfully connected to barangay.accdb database");
            return conn;
            
        } catch (ClassNotFoundException e) {
            System.err.println("UCanAccess driver not found. Please ensure ucanaccess JAR files are in classpath.");
            System.err.println("Error details: " + e.getMessage());
            e.printStackTrace();
            throw new SQLException("Database driver not available", e);
        } catch (SQLException e) {
            System.err.println("Error connecting to Access Database: " + e.getMessage());
            System.err.println("Database URL: " + URL);
            e.printStackTrace();
            throw e;
        }
    }
    
    private static void createAccessDatabase() throws SQLException {
        try {
            // Suppress warnings during database creation
            java.util.logging.Logger.getLogger("org.hsqldb").setLevel(java.util.logging.Level.SEVERE);
            java.util.logging.Logger.getLogger("net.ucanaccess").setLevel(java.util.logging.Level.SEVERE);
            
            // Ensure project directory exists
            File projectDir = new File("C:\\Projects\\dashboard_resident");
            if (!projectDir.exists()) {
                projectDir.mkdirs();
            }
            
            // Create empty Access database
            Connection conn = DriverManager.getConnection(URL + ";newdatabaseversion=V2010");
            System.out.println("Access database created successfully");
            conn.close();
            
        } catch (SQLException e) {
            System.err.println("Error creating Access database: " + e.getMessage());
            throw e;
        }
    }
    
    private static void initializeDatabaseTables(Connection conn) throws SQLException {
        // Check if tables exist, create if not
        if (!tableExists(conn, "users")) {
            System.out.println("Creating tables for the first time...");
            createTables(conn);
            insertSampleData(conn);
        } else {
            System.out.println("Database tables already exist");
        }
    }

    
    private static boolean tableExists(Connection conn, String tableName) {
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM " + tableName);
            rs.close();
            stmt.close();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }
    

    
    private static void createTables(Connection conn) throws SQLException {
        Statement stmt = conn.createStatement();
        
        // Create users table
        stmt.execute("CREATE TABLE users (" +
            "id AUTOINCREMENT PRIMARY KEY, " +
            "email TEXT(255), " +
            "password TEXT(255), " +
            "role TEXT(50), " +
            "created_at TEXT(50)" +
            ")");
        
        // Create payments table
        stmt.execute("CREATE TABLE payments (" +
            "ID AUTOINCREMENT PRIMARY KEY, " +
            "payment_id TEXT(255), " +
            "ref_number TEXT(255), " +
            "resident_name TEXT(255), " +
            "payment_type TEXT(255), " +
            "amount CURRENCY, " +
            "status TEXT(50), " +
            "date_created TEXT(50), " +
            "archived YESNO" +
            ")");
        
        // Create complaints table
        stmt.execute("CREATE TABLE complaints (" +
            "id AUTOINCREMENT PRIMARY KEY, " +
            "complaint_id TEXT(255), " +
            "complainant_name TEXT(255), " +
            "incident_type TEXT(255), " +
            "location TEXT(255), " +
            "date_filed TEXT(50), " +
            "status TEXT(50), " +
            "incident_details MEMO, " +
            "photo_path TEXT(500), " +
            "admin_response MEMO, " +
            "is_read YESNO" +
            ")");
        
        // Create announcements table
        stmt.execute("CREATE TABLE announcements (" +
            "id AUTOINCREMENT PRIMARY KEY, " +
            "announcement_id TEXT(255), " +
            "title TEXT(255), " +
            "content MEMO, " +
            "priority TEXT(50), " +
            "category TEXT(100), " +
            "posted_by TEXT(255), " +
            "date_posted TEXT(50)" +
            ")");
        
        // Create document_requests table with correct field names matching Access
        stmt.execute("CREATE TABLE document_requests (" +
            "id AUTOINCREMENT PRIMARY KEY, " +
            "request_id TEXT(255), " +
            "document_type TEXT(255), " +
            "full_name TEXT(255), " +
            "address MEMO, " +
            "birthdate TEXT(50), " +
            "civil_status TEXT(50), " +
            "purpose TEXT(255), " +
            "years_of_residency TEXT(50), " +
            "status TEXT(50), " +
            "date_requested TEXT(50)" +
            ")");
        
        stmt.close();
        System.out.println("All database tables created successfully in barangay.accdb");
    }
    
    private static void insertSampleData(Connection conn) {
        try {
            // Check if data already exists
            Statement checkStmt = conn.createStatement();
            ResultSet rs = checkStmt.executeQuery("SELECT COUNT(*) FROM users");
            if (rs.next() && rs.getInt(1) > 0) {
                System.out.println("Sample data already exists, skipping insertion");
                rs.close();
                checkStmt.close();
                return;
            }
            rs.close();
            checkStmt.close();
            
            System.out.println("Inserting sample data for the first time...");
            
            // Insert sample users ONLY
            PreparedStatement userStmt = conn.prepareStatement(
                "INSERT INTO users (email, password, role, created_at) VALUES (?, ?, ?, ?)");
            
            String[][] users = {
                {"admin@barangay.com", "admin123", "admin", "2024-06-01"},
                {"resident@email.com", "resident123", "resident", "2024-06-01"}
            };
            
            for (String[] user : users) {
                userStmt.setString(1, user[0]);
                userStmt.setString(2, user[1]);
                userStmt.setString(3, user[2]);
                userStmt.setString(4, user[3]);
                userStmt.executeUpdate();
            }
            userStmt.close();
            
            System.out.println("Basic user accounts created successfully");
        } catch (SQLException e) {
            System.err.println("Error inserting sample data: " + e.getMessage());
            e.printStackTrace();
        }
    }
}