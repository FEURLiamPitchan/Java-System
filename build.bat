@echo off
echo Building project and downloading dependencies...
cd /d "c:\Projects\dashboard_resident"
mvn clean compile
echo Build complete!
pause