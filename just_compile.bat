@echo off

cd out
rm *.class

cd ..

set dir=src/
javac -d out/ -cp out\ %dir%SymbolTable.java %dir%ErrorLog.java %dir%SyntaxChecker.java %dir%InfixToPostFix.java %dir%SequenceInfo.java %dir%Main.java %dir%MyFile.java %dir%Util.java
