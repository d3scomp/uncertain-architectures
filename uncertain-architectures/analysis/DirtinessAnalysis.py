'''
Created on Dec 8, 2015

@author: Ilias
'''
import xml.etree.ElementTree as etree
import matplotlib.pyplot as plt
# import pylab

def getDurationFromConfigFile():
        configFile = open("../config/simulationParameters.txt")
        for line in configFile.readlines():
            tokens = line.split(";")
            name = tokens[0]
            if name == "duration": 
                duration = int(tokens[1])
                Dirtiness.simulationTime = duration 
                print("The duration of the simulation is set to " + str(duration))

class Dirtiness:
    "Holds information about a dirtiness event. The event starts the first time a node gets dirty and finished when it's clean again"
    simulationTime = None
    
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

    tree = etree.parse('../logs/runtime/runtimeData.xml')  
    root = tree.getroot()                    
        
    dirtinesses = []
    dirtinessRecords = root.findall("*[@eventType='cz.cuni.mff.d3s.jdeeco.ua.visualization.DirtinessRecord']")
    print("Found " + str(len(dirtinessRecords)) + " dirtiness records")
    
    getDurationFromConfigFile()
    
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
        
    print("Printing " + str(len(dirtinesses)) + " dirtiness events")
    for d in dirtinesses:
        print(d)    

    print("Plotting...")

    plt.figure(1)
    plt.plot([d.node for d in dirtinesses],[d.duration() for d in dirtinesses], 'bo')
    plt.ylabel('Duration of dirtinesses')
    plt.savefig("../results/scatter-plot.pdf")
        
    plt.figure(2)
    plt.boxplot([d.duration() for d in dirtinesses])
    plt.ylabel('Duration of dirtinesses')
    plt.savefig("../results/boxplot.pdf")
#     plt.show()

