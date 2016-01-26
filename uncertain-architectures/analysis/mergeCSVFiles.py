'''
Created on Jan 20, 2016

@author: Ilias
'''
import os
import Simulations
from os import listdir

outputFilePath = os.path.join(Simulations.csv_dir,"merged-results.csv")
outputFile = open(outputFilePath, "w")

for csv_file_name in [f for f in listdir(Simulations.csv_dir)]:

    csv_file_full_path = os.path.join(Simulations.csv_dir, csv_file_name)
    resultsFile = open(csv_file_full_path, "r")
    
    for line in resultsFile.readlines():
        outputFile.write(line)
            
outputFile.close()
print("Analysis results merged into " + outputFilePath)

