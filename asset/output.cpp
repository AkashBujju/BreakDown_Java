#include <iostream>
#include <string>
#include <stdio.h>
using namespace std;

struct Person {
	string name = "none";
	int age = 0;
};

int main();
void print_person(Person *person);
void print_str(string str);

int main() {
	Person *person = { (Person*)(malloc(sizeof(Person) * 1)) };
	person->name = { "akash" };
	person->age = { 20 };
	print_person(person);
	Person person_2 = { "bujju",21 };
	print_person(&person_2);
	return 0;
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

