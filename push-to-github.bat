@echo off
title Push SP-MART to GitHub (Shipraprasath/sp-mart)
echo ======================================================================
echo                  Pushing SP-MART to GitHub Repository
echo             https://github.com/Shipraprasath/sp-mart.git
echo ======================================================================
echo.

set GIT="C:\Users\sprda\.gemini\antigravity\scratch\MinGit\cmd\git.exe"
set REPO_URL=https://github.com/Shipraprasath/sp-mart.git

cd /d "%~dp0"

echo [1/3] Verifying Git remote origin...
%GIT% remote remove origin 2>nul
%GIT% remote add origin %REPO_URL%

echo [2/3] Setting branch to main...
%GIT% branch -M main

echo [3/3] Uploading source code to GitHub...
echo.

%GIT% push -u origin main

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ======================================================================
    echo   SUCCESS! All SP-MART files have been pushed to GitHub!
    echo   View your repository at:
    echo   https://github.com/Shipraprasath/sp-mart
    echo ======================================================================
) else (
    echo.
    echo ======================================================================
    echo  [NOTE] If push failed due to authentication:
    echo  Please use a GitHub Personal Access Token (PAT) as your password.
    echo ======================================================================
)

echo.
pause
