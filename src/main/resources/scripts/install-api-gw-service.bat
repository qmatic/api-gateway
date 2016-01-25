@echo off
SET ROOT=%~dp0
CALL :RESOLVE "%ROOT%\..\" API_GW_HOME

set LOG_DIR=%API_GW_HOME%\logs
set BIN_DIR=%API_GW_HOME%\bin

set SERVICE_TITLE=Qmatic API Gateway
set SERVICE_NAME=QP_API_GW

%BIN_DIR%\prunsrv //IS//%SERVICE_NAME% --DisplayName="%SERVICE_TITLE%" --Install=%BIN_DIR%\prunsrv.exe --LogPath=%LOG_DIR%  --Startup=auto --StartMode=exe --StopMode=exe --StartImage=%BIN_DIR%\start-api-gateway.bat --StopImage=%BIN_DIR%\stop-api-gateway.bat

IF %ERRORLEVEL% EQU 0 (
  GOTO SUCCESS
)

:FAIL
ECHO %SERVICE_TITLE% installation failed.
GOTO:EOF

:SUCCESS
ECHO %SERVICE_TITLE% service successfully installed.
GOTO:EOF

:RESOLVE
SET %2=%~f1
GOTO :EOF
