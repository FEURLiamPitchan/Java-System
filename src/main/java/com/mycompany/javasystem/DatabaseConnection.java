package com.mycompany.javasystem;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.nio.file.Files;
import java.nio.file.Path;

public class DatabaseConnection {

    private static final String DB_PATH = "C:\\ESD\\barangay.accdb";
    private static final String URL = "jdbc:ucanaccess://" + DB_PATH;

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        if (!Files.exists(Path.of(DB_PATH))) {
            throw new SQLException("Database file not found: " + DB_PATH);
        }
        return DriverManager.getConnection(URL);
    }
}