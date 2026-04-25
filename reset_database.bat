@echo off
echo ========================================
echo Database Reset Script
echo ========================================
echo.
echo This script will:
echo 1. Backup your current database
echo 2. Delete the old database
echo 3. Let the system create a new one with correct column names
echo.
pause

cd "C:\Projects 2\dashboard_resident"

echo.
echo Step 1: Creating backup...
if exist barangay.accdb (
    copy barangay.accdb barangay_backup_%date:~-4,4%%date:~-10,2%%date:~-7,2%_%time:~0,2%%time:~3,2%%time:~6,2%.accdb
    echo Backup created successfully!
) else (
    echo No database file found to backup.
)

echo.
echo Step 2: Deleting old database...
if exist barangay.accdb (
    del barangay.accdb
    echo Old database deleted!
) else (
    echo No database file to delete.
)

echo.
echo Step 3: Database will be recreated when you run the application
echo.
echo ========================================
echo DONE! 
echo ========================================
echo.
echo Next steps:
echo 1. Run your Java application
echo 2. The system will automatically create barangay.accdb with correct column names
echo 3. You can login with: resident@email.com / resident123
echo.
pause
