package com.mycompany.javasystem;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String DB_PATH = System.getProperty("user.home") + "\\Documents\\barangay.accdb";
    private static final String URL = "jdbc:ucanaccess://" + DB_PATH;

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return DriverManager.getConnection(URL);
    }
}