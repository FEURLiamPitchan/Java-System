$conn = New-Object System.Data.OleDb.OleDbConnection('Provider=Microsoft.ACE.OLEDB.12.0;Data Source=C:\Projects\dashboard_resident\barangay.accdb')
$conn.Open()
$cmd = $conn.CreateCommand()
$cmd.CommandText = 'SELECT * FROM document_requests'
$reader = $cmd.ExecuteReader()
Write-Host "All documents in database:"
Write-Host ""
while ($reader.Read()) {
    Write-Host "Request ID: $($reader['request_id'])"
    Write-Host "  Document Type: $($reader['document_type'])"
    Write-Host "  Status: $($reader['status'])"
    Write-Host "  Date Requested: $($reader['date_requested'])"
    Write-Host "  Purpose: $($reader['purpose'])"
    Write-Host "  User Email: $($reader['user_email'])"
    Write-Host ""
}
$reader.Close()
$conn.Close()
