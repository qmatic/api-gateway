@echo off
SET QP_GW_BIN_DIR=%~dp0
"%QP_GW_BIN_DIR%\curl.exe" -s -X POST http://localhost:9091/shutdown
if %ERRORLEVEL% EQU 0 (
    echo Shutdown successfull
    GOTO :EOF
)
echo Shutdown failed