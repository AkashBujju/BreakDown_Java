#include <iostream>
#include <string>
#include <stdio.h>
using namespace std;

struct Person {
	string name = "none";
	int age = 0;
};

int main();
int add(int a, int b);
void print_person(Person *person);
void print_str(string str);

int main() {
	int a = 10;
	int *ptr = &a;
	int foo = add(10+(*ptr),20);
	printf("foo: %d\n",foo);
	return 0;
}

int add(int a, int b) {
	return a+b;
}

void print_person(Person *person) {
	printf("Name: ");
	print_str(person->name);
	printf(", Age: %d\n",person->age);
	return ;
}

void print_str(string str) {
	int i = 0;
	char c = str[i];
	while (c!='\0') {
		printf("%c",c);
		i = { i+1 };
		c = { str[i] };
	}
	return ;
}

