@echo off
title SP-Mart Backend Server (Port 8080)
echo ======================================================================
echo           SP-MART - Electronics E-Commerce Web Application
echo ======================================================================
echo  Starting Spring Boot Application on http://localhost:8080 ...
echo  Press Ctrl+C to stop the server anytime.
echo ======================================================================
echo.

cd /d "%~dp0"
call mvn spring-boot:run

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Server encountered an error or was stopped.
    pause
)
