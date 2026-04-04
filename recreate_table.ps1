Write-Host "Recreating document_requests table..."
Write-Host ""

$conn = New-Object System.Data.OleDb.OleDbConnection('Provider=Microsoft.ACE.OLEDB.12.0;Data Source=C:\Projects\dashboard_resident\barangay.accdb')
$conn.Open()

try {
    # Drop existing table
    Write-Host "Dropping old document_requests table..."
    $cmd = $conn.CreateCommand()
    $cmd.CommandText = 'DROP TABLE document_requests'
    $cmd.ExecuteNonQuery()
    Write-Host "  ✓ Old table dropped"
    Write-Host ""
    
    # Create new table
    Write-Host "Creating new document_requests table..."
    $cmd.CommandText = @"
CREATE TABLE document_requests (
    id AUTOINCREMENT PRIMARY KEY,
    request_id TEXT(255),
    document_type TEXT(255),
    full_name TEXT(255),
    address MEMO,
    birthdate TEXT(50),
    civil_status TEXT(50),
    purpose TEXT(255),
    years_of_residency TEXT(50),
    status TEXT(50),
    date_requested TEXT(50),
    user_email TEXT(255)
)
"@
    $cmd.ExecuteNonQuery()
    Write-Host "  ✓ New table created successfully!"
    Write-Host ""
    Write-Host "Table structure:"
    Write-Host "  - id (Primary Key)"
    Write-Host "  - request_id"
    Write-Host "  - document_type"
    Write-Host "  - full_name"
    Write-Host "  - address"
    Write-Host "  - birthdate"
    Write-Host "  - civil_status"
    Write-Host "  - purpose"
    Write-Host "  - years_of_residency"
    Write-Host "  - status"
    Write-Host "  - date_requested"
    Write-Host "  - user_email"
    Write-Host ""
    Write-Host "SUCCESS! You can now use the application to add document requests."
    
} catch {
    Write-Host "ERROR: $_"
} finally {
    $conn.Close()
}

Write-Host ""
Write-Host "Press any key to exit..."
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
