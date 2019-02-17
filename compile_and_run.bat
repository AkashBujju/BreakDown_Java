@echo off

set dir=src/
javac -d out/ -cp out\ %dir%Info.java %dir%BreakDown_1.java %dir%ErrorLog.java %dir%Main.java %dir%MyFile.java %dir%Util.java

IF %ERRORLEVEL% EQU 0 java -cp out\ Main
