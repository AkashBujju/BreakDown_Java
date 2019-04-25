#include <iostream>
#include <string>
#include <stdio.h>
using namespace std;

int main();
void copy(string str_1, char *str_2);

int main() {
	char *str = (char*)(malloc(sizeof(char) * 50));
	copy("hello",str);
	printf("str: %s\n",str);
	printf("str: %c\n",str[0]);
	free(str);
	return 0;
}

void copy(string str_1, char *str_2) {
	int i = 0;
	while (str_1[i]!='\0') {
		str_2[i] = { str_1[i] };
		i = { i+1 };
	}
	str_2[i] = { '\0' };
	return ;
}

