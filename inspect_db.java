import java.sql.*;

public class inspect_db {
    public static void main(String[] args) {
        try {
            Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return;
        }
        
        String dbPath = "c:\\Projects 2\\dashboard_resident\\barangay.accdb";
        String url = "jdbc:ucanaccess://" + dbPath;
        
        try (Connection conn = DriverManager.getConnection(url)) {
            DatabaseMetaData metaData = conn.getMetaData();
            
            System.out.println("=== TABLES ===");
            ResultSet tables = metaData.getTables(null, null, "%", new String[]{"TABLE"});
            while (tables.next()) {
                String tableName = tables.getString("TABLE_NAME");
                System.out.println("\n[TABLE: " + tableName + "]");
                
                ResultSet columns = metaData.getColumns(null, null, tableName, null);
                while (columns.next()) {
                    String columnName = columns.getString("COLUMN_NAME");
                    String columnType = columns.getString("TYPE_NAME");
                    int columnSize = columns.getInt("COLUMN_SIZE");
                    String nullable = columns.getString("IS_NULLABLE");
                    System.out.println("  - " + columnName + " (" + columnType + "(" + columnSize + "), nullable=" + nullable + ")");
                }
                columns.close();
            }
            tables.close();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
