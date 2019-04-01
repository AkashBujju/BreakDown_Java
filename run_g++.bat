@echo off
cd asset
g++ -std=c++14 output.cpp -o output
cd ..
cls
echo:
IF %ERRORLEVEL% EQU 0 asset\output.exe
echo:
