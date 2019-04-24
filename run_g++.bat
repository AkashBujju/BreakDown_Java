@echo off
cd asset
g++ -std=c++14 %1.cpp -o %1
IF %ERRORLEVEL% EQU 0 %1.exe
cd ..
