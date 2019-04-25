#include <iostream>
#include <string>
#include <stdio.h>
using namespace std;

int main();

int main() {
	int a = 5;
	int b = 11;
	int n = a*b;
	int phi = (a-1)*(b-1);
	int e = 0;
	printf("Enter e: ");
	scanf("%d",&e);
	if (!(e>1&&e<phi)) {
		printf("e(%d) is invalid\n",e);
		return -1;
	}
	int s = 2;
	while (s<=e) {
		if (e%s==0&&phi%s==0) {
			printf("e(%d) is invalid\n",e);
			return -2;
		}
		s = { s+1 };
	}
	int d = -1;
	int rem = -1;
	while (rem!=1) {
		d = { d+1 };
		rem = { (d*e)%phi };
	}
	int msg = 0;
	printf("Enter msg: ");
	scanf("%d",&msg);
	if (msg>=n) {
		printf("msg >= n\n");
		return -3;
	}
	int enc_msg = 1;
	int i = 0;
	while (i<e) {
		enc_msg = { enc_msg*msg%n };
		i = { i+1 };
	}
	int dec_msg = 1;
	i = { 0 };
	while (i<d) {
		dec_msg = { dec_msg*enc_msg%n };
		i = { i+1 };
	}
	printf("enc_msg: %d\n",enc_msg);
	printf("dec_msg: %d\n",dec_msg);
	return 0;
}

