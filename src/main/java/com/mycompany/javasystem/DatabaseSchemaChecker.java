package com.mycompany.javasystem;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;

public class DatabaseSchemaChecker {
    public static void main(String[] args) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            DatabaseMetaData metaData = conn.getMetaData();
            
            System.out.println("=== USERS TABLE COLUMNS ===");
            ResultSet columns = metaData.getColumns(null, null, "users", null);
            while (columns.next()) {
                String columnName = columns.getString("COLUMN_NAME");
                String columnType = columns.getString("TYPE_NAME");
                System.out.println(columnName + " - " + columnType);
            }
            columns.close();
            
            System.out.println("\n=== NOTIFICATIONS TABLE COLUMNS ===");
            columns = metaData.getColumns(null, null, "notifications", null);
            while (columns.next()) {
                String columnName = columns.getString("COLUMN_NAME");
                String columnType = columns.getString("TYPE_NAME");
                System.out.println(columnName + " - " + columnType);
            }
            columns.close();
            
            System.out.println("\n=== DOCUMENT_REQUESTS TABLE COLUMNS ===");
            columns = metaData.getColumns(null, null, "document_requests", null);
            while (columns.next()) {
                String columnName = columns.getString("COLUMN_NAME");
                String columnType = columns.getString("TYPE_NAME");
                System.out.println(columnName + " - " + columnType);
            }
            columns.close();
            
            conn.close();
            System.out.println("\nSchema check completed!");
            
        } catch (Exception e) {
            System.err.println("Error checking schema: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
