cd asset
g++ -std=c++14 output.cpp -o output
if [ $? -eq 0 ]
then
	./output
fi
