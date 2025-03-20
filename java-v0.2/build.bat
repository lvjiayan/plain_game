@echo off
chcp 65001
echo Building Shooting Game...
echo Current directory: %CD%

:: Create required directories
if not exist "bin" (
    echo Creating bin directory...
    mkdir "bin"
    echo Directory created at: %CD%\bin
)

:: Compile with absolute paths
echo Compiling Java files...
javac -verbose -encoding UTF-8 -d bin src\*.java
if errorlevel 1 (
    echo Compilation failed!
    pause
    exit /b 1
)

echo.
echo Compilation successful!
echo Running the game...
java -cp "%CD%\bin" com.game.ShootingGame

pause
