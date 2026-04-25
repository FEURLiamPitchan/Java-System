@echo off
echo Cleaning project...
rmdir /s /q "target\classes\com\mycompany\javasystem" 2>nul
echo.
echo Project cleaned. Please rebuild in NetBeans:
echo 1. Right-click on the project
echo 2. Select "Clean and Build"
echo 3. Wait for build to complete
echo 4. Run the application
echo.
pause
