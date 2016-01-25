@echo off
TITLE QMATIC API GATEWAY
SET ROOT=%~dp0
CALL :RESOLVE "%ROOT%\..\" API_GW_HOME

rem Find latest java
SET GW_HOME_JDK_DIR=%API_GW_HOME%jdk
for /f "tokens=*" %%i in ('dir /b /a:D /o:-n "%GW_HOME_JDK_DIR%\jdk*"') DO (
    set JAVA_HOME=%GW_HOME_JDK_DIR%\%%i
    GOTO JAVA_FOUND
)
GOTO JAVA_NOT_FOUND

:JAVA_FOUND

echo Using JDK : %JAVA_HOME%

pushd .

rem Generated start script assumes start from gw root directory
cd /d "%API_GW_HOME%"
set START_SCRIPT=%API_GW_HOME%bin\api-gateway.bat

call "%START_SCRIPT%"

popd

GOTO :EOF

:RESOLVE
SET %2=%~f1
GOTO :EOF

:JAVA_NOT_FOUND
echo Unable to find java, unable to start
GOTO :EOF
