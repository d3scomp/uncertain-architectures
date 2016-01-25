'''
Created on Jan 20, 2016

@author: Ilias
'''
import os
import Simulations
from os import listdir

outputFilePath = os.path.join(Simulations.cvs_dir,"merged-results.csv")
outputFile = open(outputFilePath, "w")

for cvs_file_name in [f for f in listdir(Simulations.cvs_dir)]:

    cvs_file_full_path = os.path.join(Simulations.cvs_dir, cvs_file_name)
    resultsFile = open(cvs_file_full_path, "r")
    
    for line in resultsFile.readlines():
        outputFile.write(line)
            
outputFile.close()
print("Analysis results merged into " + outputFilePath)

