@echo off
setlocal
if not defined JAVA_HOME if exist "C:\Program Files\Android\Android Studio\jbr\bin\java.exe" set "JAVA_HOME=C:\Program Files\Android\Android Studio\jbr"
set "GAME_JAVA=javaw.exe"
if defined JAVA_HOME set "GAME_JAVA=%JAVA_HOME%\bin\javaw.exe"
cd /d "%~dp0assets"
if not exist "..\lwjgl3\build\libs\midtermwork-1.0.0.jar" (
  echo Build the game first: gradlew.bat lwjgl3:jar
  pause
  exit /b 1
)
start "midtermwork" "%GAME_JAVA%" -jar "..\lwjgl3\build\libs\midtermwork-1.0.0.jar"
endlocal
