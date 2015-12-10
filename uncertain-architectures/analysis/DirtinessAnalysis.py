'''
Created on Dec 8, 2015

@author: Ilias
'''
import xml.etree.ElementTree as etree

class Dirtiness:
    "Holds information about a dirtiness event. The event starts the first time a node gets dirty and finished when it's clean again"
    simulationTime = 100000
    
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
    
    def __str__(self):
        endTime = self.endTime if self.endTime != None else self.simulationTime
        return "Dirtiness [node: {0:3d} \t duration: {1:7d}  \t start/end time: {2:6d}/{3:6d} \t changes: {4}".format(self.node, endTime - self.startTime, self.startTime, endTime, self.intensitites)
        
if __name__ == '__main__':      

    tree = etree.parse('../logs/runtime/runtimeData.xml')  
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
        
    for d in dirtinesses:
        print(d)    
