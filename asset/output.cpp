#include <iostream>
#include <string>
#include <stdio.h>
using namespace std;

int a = 10;
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

int main(int argc, char **argv);

int main(int argc, char **argv) {
	printf("Enter 5 numbers: ");
	int nums[5] = { 0,0,0,0,0 };
	int i = 0;
	int max = nums[0];
	while (i<5) {
		int tmp = 0;
		scanf("%d",&tmp);
		nums[i] = { tmp };
		if (nums[i]>max) {
			max = { nums[i] };
		}
		i = { i+1 };
	}
	if (max>0) {
		printf("max > 0\n");
	}
	else if (max<0) {
		printf("max < 0\n");
	}
else {
	printf("max = 0\n");
}
	printf("Max: %d\n",max);
}

