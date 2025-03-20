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

if not exist "lib" (
    echo Creating lib directory...
    mkdir "lib"
    echo Directory created at: %CD%\lib
)

:: Check required libraries
if not exist "lib\jinput.jar" (
    echo Missing jinput.jar
    echo Please download the following files and place them in the lib directory:
    echo - jinput.jar
    echo - jinput-platform.jar
    echo - jutils.jar
    echo Download from: https://jar-download.com/artifacts/net.java.jinput/jinput/2.0.9
    pause
    exit /b 1
)

:: Set classpath with all required libraries
set CLASSPATH="%CD%\bin;%CD%\lib\jinput.jar;%CD%\lib\*"

:: Compile with absolute paths and libraries
echo Compiling Java files...
javac -verbose -encoding UTF-8 -d bin -cp %CLASSPATH% src\com\game\*.java
if errorlevel 1 (
    echo Compilation failed!
    pause
    exit /b 1
)

echo.
echo Compilation successful!
echo Running the game...
java -cp %CLASSPATH% -Djava.library.path=lib\native com.game.ShootingGame

pause
