#include <iostream>
#include <string>
#include <stdio.h>
using namespace std;

struct Person {
	string name = "null";
	int age = 0;
};

struct Student {
	Person person[2];
	string usn;
	int marks[2] = { 1,2 };
	double height = 0.0;
	double weight = 0.0;
};

int main(int argc, char **argv) {
	Person person_1 = { "jay",2 };
	Person person_2 = { "jay",2 };
	int marks[] = { 80,90,75,30,55 };
	Student student_1 = { person_1,person_2,"001",2,3,4.0,5.0, };
	Student student_2 = { person_1,person_2,"002",4,5 };
	Student *students[] = { &student_1,&student_2 };
	int numbers[] = { 10,12,34,56,97,100 };
	int size = 6;
	int i = 1;
	int max = numbers[0];
	if (numbers[i]<max) {
		int tmp = 1;
	}
	string a = students[0]->person[0].name;
	string b = student_1.usn;
	int d = person_1.age;
	string e = students[0]->usn;
	double f = student_1.height;
	string g = student_1.person[1].name;
	int num = 0;
	int tmp_3 = *(&num);
	string name = "1"+"2";
	int **data_int = make_object(int*,foo_1(marks));
	Person *data_persons = { make_object(Person,5) };
}

string append(string s1, string s2) {
}

int foo_2(int *a) {
}

int foo_1(int a[]) {
}

void demo() {
}

