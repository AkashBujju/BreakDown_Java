@echo off

cd out
del *.class

cd ..

set dir=src/
javac -d out/ -cp out\ %dir%Translater.java %dir%EvalExp.java %dir%SemanticAnalyser.java %dir%LexicalAnalyser.java %dir%Info.java %dir%SymbolTable.java %dir%ErrorLog.java %dir%SyntaxAnalyser.java %dir%InfixToPostFix.java %dir%SequenceInfo.java %dir%Main.java %dir%MyFile.java %dir%Util.java
