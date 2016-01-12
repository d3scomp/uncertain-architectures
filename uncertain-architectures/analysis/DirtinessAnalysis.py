'''
Created on Dec 8, 2015

@author: Ilias
'''
import xml.etree.ElementTree as etree
import numpy as np
import os
import Simulations

class Dirtiness:
    "Holds information about a dirtiness event. The event starts the first time a node gets dirty and finishes when it's clean again"
    
    simulationTime = 500000
    
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
        
def analyze(simulationSignature):
    
    os.makedirs(Simulations.cvs_dir, exist_ok=True)
    
    simulation_signature_logs_dir = os.path.join(Simulations.logs_dir,simulationSignature)
    simulation_signature_cvs_file_path = os.path.join(Simulations.cvs_dir,simulationSignature+".cvs")
    
    log_dir_names = []
    for root, dirs, files in os.walk(simulation_signature_logs_dir):
        for log_dir_name in dirs:
            simulation_signature_logs_dir_full_name = os.path.join(simulation_signature_logs_dir,log_dir_name) 
            log_dir_names.append(simulation_signature_logs_dir_full_name)
    
    bigPercentiles = []

    outputFile = open(simulation_signature_cvs_file_path, "w")
    
    for log_dir in log_dir_names : 
        
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
        bigPercentiles.append(bigPercentile)
    
    average = np.average(bigPercentiles)
    outputFile.write("\n")
    outputFile.write(str(average))
    
    median = np.percentile(bigPercentiles, 50)
    outputFile.write("\n")
    outputFile.write(str(median))
    
    outputFile.close()
    
    print("Analysis results written to " + simulationSignature)
    
if __name__ == '__main__':   
    
    analyze()