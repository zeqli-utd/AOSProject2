#!/usr/bin/python

import sys

class Vector:
	def __init__(self, i, vector):
		self.pid = i
		self.v = vector
		self.vLen = len(vector)
		
		
	def getValue(self, i):
		return self.v[i]

class GlobalState:
	'A collection of local state'
	
	def __init__(self, n):
		self.numProc = n;
		self.G = []
		
	# Push a vector into global state
	def push(self, vClock):
		if isinstance(vClock, Vector):
			if vClock.vLen != self.numProc:
				raise IndexError('Vector size incomp')
			if len(self.G) >= self.numProc:
				raise IndexError('GlobalState is full!')
				
			self.G.append(vClock)
		else:
			raise TypeError('Not a vector')
	
	def isConsistent(self):
		vMax = [0 for i in range(self.numProc)]
		for vClock in self.G:
			v = vClock.v
			for i in range(self.numProc):
				vMax[i] = max(vMax[i], v[i])
		for i in range(self.numProc):
			if self.G[i].getValue(i) != vMax[i]:
				print('Value {:d} in V_max {:s} != Value {:d} in G[i] {:s}'.format(int(vMax[i]), \
				str(vMax), self.G[i].getValue(i), str(self.G[i].v)))
				return False
		return True

v1 = Vector(0, [1, 1])
v2 = Vector(1, [0, 0])
G = GlobalState(2)
G.push(v1)
G.push(v2)
print(G.isConsistent())


print('Number of arguments:',len(sys.argv), 'arguments.')
print('Argument List:', str(sys.argv))
print('Number of ndoes', sys.argv[1])
n = int(sys.argv[1])
globalStateCollection = []
files = []

for i in range(n):
	path = 'config-' + str(i) + '.out'
	with open(path, 'r') as f:
		for j, line in enumerate(f):
			vClock = Vector(i, [int(k) for k in line.split()])
			if j >= len(globalStateCollection):
				G = GlobalState(n)  # New GlobalState
				G.push(vClock)
				globalStateCollection.append(G)
			else:
				G = globalStateCollection[j]
				G.push(vClock)


				
for i in range(len(globalStateCollection)):
	G = globalStateCollection[i]
	print("Snapshot " + str(i) + " Consistency: ", end='')
	print(G.isConsistent())
	count = 0
	for vClock in G.G:
		print(' ' + str(count) + ' ' + str(vClock.v))
		count += 1
	print()

	
			

	
	
