@echo off
title SP-Mart Electronics E-Commerce Server (Port 8080)
echo ======================================================================
echo           SP-MART - Electronics E-Commerce Web Application
echo ======================================================================
echo  Starting SP-Mart Server ^& Public Mobile Tunnel...
echo.
echo  Local URL:        http://localhost:8080
echo  Mobile/Online:    Starts automatically via Cloudflare Tunnel
echo.
echo  Demo Logins:
echo    * Buyer:    buyer@spmart.com    / Buyer@123
echo    * Seller:   seller@spmart.com   / Seller@123
echo    * Admin:    admin@spmart.com    / Admin@123
echo.
echo  To STOP the server, press Ctrl+C or close this window.
echo ======================================================================
echo.

cd /d "%~dp0"

:: Free port 8080 from any conflicting app (e.g., Yogeswari-Mart or old session)
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":8080 " ^| findstr "LISTENING"') do (
    taskkill /F /PID %%a >nul 2>&1
)
taskkill /F /IM cloudflared.exe >nul 2>&1

:: Start Cloudflare Public Tunnel for Mobile Phones (if cloudflared.exe exists)
if exist "cloudflared.exe" (
    start "SP-Mart Public Mobile Tunnel" /min cmd /c "cloudflared.exe tunnel --url http://localhost:8080"
)

:: Wait until SP-Mart server actually responds with HTTP 200, then open browser
start /b powershell -NoProfile -WindowStyle Hidden -Command "for($i=0;$i -lt 45;$i++){try{$r=Invoke-WebRequest -Uri 'http://localhost:8080/login' -UseBasicParsing -TimeoutSec 2;if($r.StatusCode -eq 200 -and $r.Content -match 'SP MART'){Start-Process 'http://localhost:8080/';break}}catch{};Start-Sleep -Seconds 1}"

:: Start via pre-built executable JAR (fast ~5s boot), or fallback to maven
if exist "target\sp-mart-1.0.0.jar" (
    java -jar target\sp-mart-1.0.0.jar
) else (
    call mvn spring-boot:run
)

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Server encountered an error or was stopped.
    pause
)
