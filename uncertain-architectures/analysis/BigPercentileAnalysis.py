'''
Created on Dec 31, 2015

@author: Ilias
'''
import matplotlib.pyplot as plt

resultsFile = open("../results/simulationResults.csv", "r")
bigPercentiles = []

for line in resultsFile.readlines():
    tokens = line.split(";")
    bigPercentile = float(tokens[0])
    bigPercentiles.append(bigPercentile)

plt.figure(1)
plt.boxplot(bigPercentiles)
plt.ylabel('NinetyPercentiles')
plt.savefig("../results/NinetyfifthPercentiles.pdf")