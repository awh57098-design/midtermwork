@echo off
setlocal
if not defined JAVA_HOME if exist "C:\Program Files\Android\Android Studio\jbr\bin\java.exe" set "JAVA_HOME=C:\Program Files\Android\Android Studio\jbr"
cd /d "%~dp0"
call gradlew.bat lwjgl3:run --console=plain
if errorlevel 1 pause
endlocal
