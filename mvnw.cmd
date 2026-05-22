@ECHO OFF
SETLOCAL

set "SCRIPT_DIR=%~dp0"
set "MAVEN_PROJECTBASEDIR=%SCRIPT_DIR%"

if defined MAVEN_HOME (
  set "WRAPPER_MVN=%MAVEN_HOME%\bin\mvn.cmd"
  if exist "%WRAPPER_MVN%" goto run
)

if defined M2_HOME (
  set "WRAPPER_MVN=%M2_HOME%\bin\mvn.cmd"
  if exist "%WRAPPER_MVN%" goto run
)

set "WRAPPER_MVN=C:\Program Files\NetBeans-23\netbeans\java\maven\bin\mvn.cmd"
if exist "%WRAPPER_MVN%" goto run

where mvn >NUL 2>NUL
if %ERRORLEVEL% EQU 0 (
  set "WRAPPER_MVN=mvn"
  goto run
)

echo Maven was not found. Set MAVEN_HOME or install Maven/NetBeans bundled Maven.
exit /B 1

:run
call "%WRAPPER_MVN%" %*
exit /B %ERRORLEVEL%
