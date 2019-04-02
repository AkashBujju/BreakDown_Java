#include <iostream>
#include <string>
#include <stdio.h>
using namespace std;

void copy_str(string from, char to[]);
void append_to_str(string str, char to[]);
int main();

void copy_str(string from, char to[]) {
	int i = 0;
	char c = from[0];
	while (c!='\0') {
		to[i] = { c };
		i = { i+1 };
		c = { from[i] };
	}
	return ;
}

void append_to_str(string str, char to[]) {
	int i = 0;
	char c = to[0];
	while (c!='\0') {
		i = { i+1 };
		c = { to[i] };
	}
	c = { str[0] };
	int j = 0;
	while (c!='\0') {
		to[i] = { c };
		j = { j+1 };
		i = { i+1 };
		c = { str[j] };
	}
	return ;
}

int main() {
	char name[20] = {  };
	copy_str("Hello",name);
	append_to_str(", World.",name);
	printf("%s\n",name);
	return 0;
}

