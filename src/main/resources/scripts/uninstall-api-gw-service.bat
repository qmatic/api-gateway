@echo off
set SERVICE_NAME=QP_API_GW
echo Removing Orchestra API Gateway service
sc stop %SERVICE_NAME% > NUL
sc delete %SERVICE_NAME% > NUL
reg delete "HKLM\SYSTEM\CurrentControlSet\Services\%SERVICE_NAME%" /f >NUL 2>&1
exit /b 0