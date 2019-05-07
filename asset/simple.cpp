#include <iostream>
#include <string>
#include <stdio.h>
using namespace std;

int main();

int main() {
	printf("Enter name: ");
	char *name = (char*)(malloc(sizeof(char) * 50));
	scanf("%s",name);
	printf("Hi %s\n",name);
	return 0;
}

