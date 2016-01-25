'''
Created on Dec 8, 2015

@author: Ilias
'''
import xml.etree.ElementTree as etree
import numpy as np
import os
import Simulations
import multiprocessing
from os import listdir
from os.path import join, isfile

class Dirtiness:
    "Holds information about a dirtiness event. The event starts the first time a node gets dirty and finishes when it's clean again"
    
    simulationTime = 600000
    
    def __init__(self, node, startTime, initialIntensity):
        self.node = node
        self.startTime = startTime
        self.endTime = None
        self.intensitites = []
        self.intensitites.append({"time": startTime,"intensity": initialIntensity})
        
    def intensityChanged(self, time, newIntensity):
        self.intensitites.append({"time": time,"intensity": newIntensity})
        if newIntensity == 0:
            self.endTime = time
        
    def eventCompleted(self):
        return self.endTime != None
    
    def getEndTime(self):
        return self.endTime if self.endTime != None else self.simulationTime
    
    def duration(self):
        return self.getEndTime() - self.startTime 
        
    def __str__(self):
        return "Dirtiness [node: {0:3d} \t duration: {1:7d}  \t start/end time: {2:6d}/{3:6d} \t changes: {4}".format(self.node, self.duration(), self.startTime, self.getEndTime(), self.intensitites)
      
def analyzeLog(simulationSignature, log_dir_name):
    
    outputFilePath = os.path.join(Simulations.cvs_dir,simulationSignature + "_" + log_dir_name + ".csv")
    outputFile = open(outputFilePath, "w")

    log_dir = os.path.join(Simulations.logs_dir, simulationSignature, log_dir_name)
    print("Analyzing " + log_dir)
    
    tree = etree.parse(os.path.join(log_dir,'runtimeData.xml'))  
    root = tree.getroot()                    
        
    dirtinesses = []
    dirtinessRecords = root.findall("*[@eventType='cz.cuni.mff.d3s.jdeeco.ua.visualization.DirtinessRecord']")
    print("Found " + str(len(dirtinessRecords)) + " dirtiness records")
    
    for r in dirtinessRecords:
        node = int(r[1].text)
        time = int(r.attrib["time"])
        intensity = float(r[0].text)
        existingDirtiness = [d for d in dirtinesses if d.node == node and d.endTime==None]
        if len(existingDirtiness) > 0:
            if len(existingDirtiness) == 1:
                existingDirtiness[0].intensityChanged(time, intensity)
            else :
                raise Exception("Found more than one non-completed dirtinesses in node " + node)
        else :
            dirtiness = Dirtiness(node, time, intensity)
            dirtinesses.append(dirtiness)
        
    print("Found " + str(len(dirtinesses)) + " dirtiness events")

    durations = [d.duration() for d in dirtinesses]
    
    bigPercentile = np.percentile(durations, 90)
    print("The 90th percentile of dirtiness event durations is " + str(bigPercentile))
    outputFile.write(str(bigPercentile)+"\n")
    outputFile.close()
    
    print("Analysis results written to " + outputFilePath)

def mergeIntoSingleFile(simulationSignature):
    
    outputFilePath = os.path.join(Simulations.cvs_dir,simulationSignature + ".csv")
    outputFile = open(outputFilePath, "w")

    for cvs_file_name in [f for f in listdir(Simulations.cvs_dir) if isfile(join(Simulations.cvs_dir, f))]:

        if (cvs_file_name.startswith(simulationSignature)):
            
            cvs_file_full_path = os.path.join(Simulations.cvs_dir, cvs_file_name)
            resultsFile = open(cvs_file_full_path, "r")
            
            for line in resultsFile.readlines():
                outputFile.write(line)
                
            resultsFile.close
#             os.remove(cvs_file_full_path)
            
    outputFile.close()
    print("Analysis results merged into " + outputFilePath)
 
simulated = []

def finalizeOldestAnalysis():
    simulation = simulated[0]
    simulation.join()
    simulated.pop(0)
    
def analyze(cores):
          
    os.makedirs(Simulations.cvs_dir, exist_ok=True)
    
    for simulationSignature in [f for f in listdir(Simulations.logs_dir)]:
        print("Analyzing logs of signature: " + simulationSignature)
                        
        for root, dirs, files in os.walk(os.path.join(Simulations.logs_dir,simulationSignature)):
            for log_dir_name in dirs:
                
                if (len(simulated) >= cores) :
                    finalizeOldestAnalysis()

                p = multiprocessing.Process(target=analyzeLog, args=(simulationSignature, log_dir_name))
                simulated.append(p)
                p.start()

        # finalize the rest    
        while len(simulated) > 0:
            finalizeOldestAnalysis()
            
        mergeIntoSingleFile(simulationSignature)
    
if __name__ == '__main__':   
    
    analyze(2)