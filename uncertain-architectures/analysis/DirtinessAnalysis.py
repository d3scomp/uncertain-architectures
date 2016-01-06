'''
Created on Dec 8, 2015

@author: Ilias
'''
import xml.etree.ElementTree as etree
import matplotlib.pyplot as plt
import numpy as np
import os

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
        if intensity == 0:
            self.endTime = time
        
    def eventCompleted(self):
        return self.endTime != None
    
    def getEndTime(self):
        return self.endTime if self.endTime != None else self.simulationTime
    
    def duration(self):
        return self.getEndTime() - self.startTime 
        
    def __str__(self):
        return "Dirtiness [node: {0:3d} \t duration: {1:7d}  \t start/end time: {2:6d}/{3:6d} \t changes: {4}".format(self.node, self.duration(), self.startTime, self.getEndTime(), self.intensitites)
        
if __name__ == '__main__':      

    dockFailure = False

    correlation = False
    roleRemoval = False
    ExtraTransitions = False
    
    log_dirs = []
    for dir_name in [x[0] for x in os.walk('logs')]:
        if (dir_name.startswith('logs\log_')) :
            log_dirs.append(dir_name)
    
    bigPercentiles = []
    outputFile = open("../results/results.csv", "w")
    
    for log_dir in log_dirs : 
        
        print("Analysing " + log_dir)

        tree = etree.parse(log_dir+'/runtimeData.xml')  
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
    #     for d in dirtinesses:
    #         print(d)    
    
        durations = [d.duration() for d in dirtinesses]
        nodes = [d.node for d in dirtinesses]
        
#         plt.figure(1)
#         plt.plot(nodes,durations, 'bo')
#         plt.ylabel('Duration of dirtinesses')
    #     plt.savefig("../results/scatter-plot.pdf")
    #         
#         plt.figure(2)
#         plt.boxplot(durations)
#         plt.ylabel('Duration of dirtinesses')
#         plt.savefig("../results/boxplot.pdf")
    #     plt.show()
    
    #     median = np.percentile(durations, 50)
    #     print("the median of durations is " + str(median))
        
        bigPercentile = np.percentile(durations, 90)
        print("The 90th percentile of dirtiness event durations is " + str(bigPercentile))
        bigPercentiles.append(bigPercentile)
        outputFile.write(str(bigPercentile)+"\n")
    
    average = np.average(bigPercentiles)
    outputFile.write("\n")
    outputFile.write(str(average))
    outputFile.close()