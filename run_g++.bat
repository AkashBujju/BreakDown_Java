@echo off
cd asset
g++ -std=c++14 output.cpp -o output
IF %ERRORLEVEL% EQU 0 output.exe
cd ..
echo:
