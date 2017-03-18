#!/usr/bin/python

import sys

def HappensBefore(C_e, i, C_f, j):
	if i == j:
		if C_e[i] < C_f[i]:
			return True
	else:
		if C_e[i] <= C_f[i]:
			return True
	return False;

print('Number of arguments:',len(sys.argv), 'arguments.')
print('Argument List:', str(sys.argv))
print('Number of ndoes', sys.argv[1])
n = int(sys.argv[1])


matrix = []

path = "config-0.out";
print(path)
with open(path, 'r') as f:
	for j, line in enumerate(f):
		row = [];
		row.append([int(i) for i in line.split()])
		matrix.append(row)	
	
out = []
for i in range (1, n):
	path = "config-" + str(i) +".out";
	print(path)
	out.append(path)
	with open(path, 'r') as f:
		for j, line in enumerate(f):
			matrix[j].append([int(i) for i in line.split()])			

for row in matrix:
	print(row)
	isConsistent = True;
	for i in range( len(row)):
		for j in range (i + 1, len(row)):
			if (HappensBefore(row[i], i, row[j], j)):
				isConsistent = False;
				print(str(row[i]) + " " + str(i) + " Happens Before " + str(row[j]) + " " + str(j))
			if (HappensBefore(row[j], j, row[i], i)):
				isConsistent = False;
				print(str(row[j]) + " " + str(j) + " Happens Before " + str(row[i]) + " " + str(i))
	print(isConsistent)
			


	

