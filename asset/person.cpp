#include <iostream>
#include <string>
#include <stdio.h>
using namespace std;

struct Person {
	char *name;
	int age = 0;
};

int main();
void copy(string str_1, char *str_2);
void print_person(Person *person);
Person* make_person(string name, int age);

int main() {
	Person *person_1 = { make_person("p1",10) };
	print_person(person_1);
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

void print_person(Person *person) {
	printf("Name: %s, Age %d\n",person->name,person->age);
	return ;
}

Person* make_person(string name, int age) {
	Person *person = { (Person*)(malloc(sizeof(Person) * 1)) };
	char *person_name = (char*)(malloc(sizeof(char) * 50));
	copy(name,person_name);
	person->name = { person_name };
	person->age = { age };
	return person;
}

