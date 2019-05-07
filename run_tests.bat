@echo off
cls
echo Running tests ...
echo.

java -cp out\ Main demo -silent
echo compiled demo
java -cp out\ Main copy -silent
echo compiled copy 
java -cp out\ Main rsa -silent
echo compiled rsa 
java -cp out\ Main person -silent
echo compiled Person 

echo.
echo Ran all Tests.
