@echo off
g++ -std=c++14 asset\output.cpp -o output
IF %ERRORLEVEL% EQU 0 asset\output.exe
