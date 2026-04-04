$conn = New-Object System.Data.OleDb.OleDbConnection('Provider=Microsoft.ACE.OLEDB.12.0;Data Source=C:\Projects\dashboard_resident\barangay.accdb')
$conn.Open()
$cmd = $conn.CreateCommand()
$cmd.CommandText = 'SELECT TOP 1 * FROM document_requests'
$reader = $cmd.ExecuteReader()
$schemaTable = $reader.GetSchemaTable()

Write-Host "Exact column names in document_requests table:"
Write-Host "=============================================="
foreach ($row in $schemaTable.Rows) {
    Write-Host $row["ColumnName"]
}

$reader.Close()
$conn.Close()
