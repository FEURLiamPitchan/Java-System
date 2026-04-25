$conn = New-Object System.Data.OleDb.OleDbConnection('Provider=Microsoft.ACE.OLEDB.12.0;Data Source=C:\Projects 2\dashboard_resident\barangay.accdb')
$conn.Open()

$tables = @('users', 'residents', 'document_requests', 'payments', 'complaints', 'announcements', 'notifications', 'settings', 'finances', 'logs')

foreach ($table in $tables) {
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "TABLE: $table" -ForegroundColor Yellow
    Write-Host "========================================" -ForegroundColor Cyan
    
    try {
        $cmd = $conn.CreateCommand()
        $cmd.CommandText = "SELECT TOP 1 * FROM $table"
        $reader = $cmd.ExecuteReader()
        
        $schema = $reader.GetSchemaTable()
        foreach ($row in $schema.Rows) {
            Write-Host "  - $($row['ColumnName']) ($($row['DataType'].Name))"
        }
        
        $reader.Close()
    } catch {
        Write-Host "  ERROR: $_" -ForegroundColor Red
    }
    
    Write-Host ""
}

$conn.Close()
