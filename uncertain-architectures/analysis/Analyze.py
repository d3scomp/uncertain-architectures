'''
Created on Dec 8, 2015

This script extracts and computes the dirtiness duration times from logs
produced by simulations.

@author: Ilias
@author: Dominik Skoda
'''

import os
import sys
import time
import shutil
import re
import numpy as np
import xml.etree.ElementTree as etree
from multiprocessing import Process
from Scenarios import *
from Configuration import *


PHASE_DELIM_TIME = "phaseAt"

analyzed = []


class Dirtiness:
    """ Holds information about a dirtiness event. The event starts the first
        time a node gets dirty and finishes when it's clean again. """
    
    def __init__(self, node, startTime, initialIntensity):
        self.node = node
        self.startTime = startTime
        self.endTime = None
        self.intensitites = []
        self.intensitites.append({"time":startTime, "intensity":initialIntensity})
        
    def intensityChanged(self, time, newIntensity):
        self.intensitites.append({"time":time, "intensity":newIntensity})
        if newIntensity == 0:
            self.endTime = time
        
    def eventCompleted(self):
        return self.endTime != None
    
    def getEndTime(self):
        return self.endTime if self.endTime != None else SIMULATION_DURATION
    
    def duration(self):
        return self.getEndTime() - self.startTime 
        
    def __str__(self):
        return "Dirtiness [node: {0:3d}\tduration: {1:7d}\tstart/end time: {2:6d}/{3:6d}\tchanges: {4}".format(
                self.node, self.duration(), self.startTime, self.getEndTime(), self.intensitites)

class ProbabilityRecord:
    """ Holds information about a non-deterministic probability event."""
    
    def __init__(self, time, probability, component):
        self.time = time
        self.probability = probability
        self.component = component
        
    def __str__(self):
        return "EMS probability for {} at {} is {}".format(
                self.component, self.time, self.probability)
        

def finalizeOldestAnalysis():
    analysis = analyzed[0]
    analysis.join()
    analyzed.pop(0)


def analyzeLog(simulationSignature, logDirName, phase_sep):
    logDir = os.path.join(LOGS_DIR, simulationSignature, logDirName)
    print("Analyzing " + logDir)
    
    tree = etree.parse(os.path.join(logDir, RUNTIME_LOG_FILE))  
    root = tree.getroot()                    
        
    dirtinesses = []
    dirtinessRecords = root.findall("*[@eventType='cz.cuni.mff.d3s.jdeeco.ua.visualization.DirtinessRecord']")
    
    print("Found " + str(len(dirtinessRecords)) + " dirtiness records")
    
    for r in dirtinessRecords:
        node = int(r.find('node').text)
        time = int(r.attrib["time"])
        intensity = float(r.find('intensity').text)
        existingDirtiness = [d for d in dirtinesses if d.node == node and d.endTime == None]
        if len(existingDirtiness) > 0:
            if len(existingDirtiness) == 1:
                existingDirtiness[0].intensityChanged(time, intensity)
            else :
                raise Exception("Found more than one non-completed dirtinesses in node " + node)
        else :
            dirtiness = Dirtiness(node, time, intensity)
            dirtinesses.append(dirtiness)
        
    print("Found " + str(len(dirtinesses)) + " dirtiness events")

    # Dirtiness durations
    warmed = [d for d in dirtinesses if d.startTime > 0]
    print("warmed length is {}".format(len(warmed)));
    print("dirtinesses length is {}".format(len(dirtinesses)));
    durations_phase1 = [d.duration() for d in warmed if d.startTime < phase_sep]
    durations_phase2 = [d.duration() for d in warmed if d.startTime >= phase_sep]
#     result_phase1 = np.percentile(durations_phase1, PERCENTILE) if len(durations_phase1) > 0 else 0
#     result_phase2 = np.percentile(durations_phase2, PERCENTILE) if len(durations_phase2) > 0 else 0
#     print("The {}th percentile of dirtiness event durations is {}".format(PERCENTILE, str(result_phase1)))
#     print("The {}th percentile of dirtiness event durations is {}".format(PERCENTILE, str(result_phase2)))
    result_phase1 = np.mean(durations_phase1) if len(durations_phase1) > 0 else 0
    result_phase2 = np.mean(durations_phase2) if len(durations_phase2) > 0 else 0
    print("The mean of dirtiness event durations is {}".format(str(result_phase1)))
    print("The mean of dirtiness event durations is {}".format(str(result_phase2)))

    outputFilePath = os.path.join(CSV_DIR,simulationSignature + "_" + logDirName + "_phase1" + ".csv")
    outputFile = open(outputFilePath, "w")
    outputFile.write(str(result_phase1)+"\n")
    outputFile.close()
    
    outputFilePath = os.path.join(CSV_DIR,simulationSignature + "_" + logDirName + "_phase2" + ".csv")
    outputFile = open(outputFilePath, "w")
    outputFile.write(str(result_phase2)+"\n")
    outputFile.close()
    
    probabilities = {}
    probabilityRecords = root.findall("*[@eventType='cz.cuni.mff.d3s.jdeeco.adaptation.modeswitching.NonDeterministicLevelRecord']")
    print("probabilityRecords: {}".format(len(probabilityRecords)))
    for r in probabilityRecords:
        component = r.find('component').text
        probability = float(r.find("probability").text)
        time = int(r.attrib["time"])
        probabilityRecord = ProbabilityRecord(time, probability, component)
        if component in probabilities:
            oldRecord = probabilities[component]
            if oldRecord.time < time:
                probabilities.pop(component)
                probabilities[component] = probabilityRecord
        else:
            probabilities[component] = probabilityRecord
            
    outputFilePath = os.path.join(CSV_DIR,simulationSignature + "_" + logDirName + "_probabilities" + ".csv")
    outputFile = open(outputFilePath, "w")
    for component in probabilities.keys():
        outputFile.write(str(probabilities[component])+"\n")
    outputFile.close()       
    
    print("Analysis results written to " + outputFilePath)


def analyzeSignature(signature, phase_sep):
    validDir = re.compile('.+_\d+')
    logsDir = os.path.join(LOGS_DIR, signature)
    
    if not os.path.isdir(logsDir):
        raise Exception("Logs from scenario {} are missing.".format(signature))
    if not os.path.exists(CSV_DIR):
        os.makedirs(CSV_DIR)
    
    print("Analyzing logs of signature: " + signature)
                    
    for _, dirs, _ in os.walk(logsDir):
        for logDirName in dirs:
            if(validDir.match(logDirName) == None):
                continue;
            if (len(analyzed) >= CORES) :
                finalizeOldestAnalysis()

            analysis = Process(target = analyzeLog, args = (signature, logDirName, phase_sep))
            analyzed.append(analysis)
            analysis.start()

    # finalize the rest
    while len(analyzed) > 0:
        finalizeOldestAnalysis()


def printHelp():
    print("\nUsage:")
    print("\tpython Analyze.py scenario1 [scenario2 [phaseAt<time>] [...]] ")
    print("\nArguments:")
    print("\tscenario - index of the required scenario")
    print("\tphaseAt<time> - time to split phases of the scenario")
    print("\nDescription:")
    print("\tThe scenario to analyze has to be already simulated and the logs "
          "produced by the simulation has to be available."
          "\n\tThe available scenarios to simulate and analyze can be found by running:"
          "\n\t\tpython Scenarios.py")


def extractArgs(args):
    # Check argument count (1st argument is this script name)
    if len(args) < 2:
        raise ArgError("At least one scenario argument is required")
    
    scenarioIndices = {}
    lastIndex = -1;
    for i in range(1, len(args)):
        if str(args[i]).startswith(PHASE_DELIM_TIME):
            if lastIndex == -1:
                raise ArgError("{} arg has to follow scenario index.".format(PHASE_DELIM_TIME))
            scenarioIndices[lastIndex]= int(args[i][len(PHASE_DELIM_TIME):])
            lastIndex = -1
            continue
        
        scenarioIndex = int(args[i])
        if len(scenarios) <= scenarioIndex or 0 > scenarioIndex:
            raise ArgError("Scenario index value {} is out of range.".format(scenarioIndex))
        scenarioIndices[scenarioIndex] = 0
        lastIndex = scenarioIndex
    
    return scenarioIndices


if __name__ == '__main__':
    try:
                    
        scenarioArgs = extractArgs(sys.argv)
        
        start = time.time()
        for scenario in scenarioArgs.keys():
            signature = getScenarioSignature(scenario)
            print("Analyzing scenario {} with signature {}"
                  .format(scenario, signature))
            analyzeSignature(signature, scenarioArgs[scenario])
        end = time.time()
        
        print("Analysis lasted for {:.2f} mins".format((end-start)/60))
        print("Results placed to {}".format(CSV_DIR))
    except ArgError:
        printHelp()
    except Exception as e:
        print(e.__str__())

