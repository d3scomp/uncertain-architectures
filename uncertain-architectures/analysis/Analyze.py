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
import numpy as np
import xml.etree.ElementTree as etree
from multiprocessing import Process
from Scenarios import *
from Configuration import *


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


def finalizeOldestAnalysis():
    analysis = analyzed[0]
    analysis.join()
    analyzed.pop(0)


def analyzeLog(simulationSignature, logDirName):
    
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

    durations = [d.duration() for d in dirtinesses]
    
    bigPercentile = np.percentile(durations, PERCENTILE)
    print("The {}th percentile of dirtiness event durations is {}".format(PERCENTILE, str(bigPercentile)))
    
    outputFilePath = os.path.join(CSV_DIR,simulationSignature + "_" + logDirName + ".csv")
    outputFile = open(outputFilePath, "w")
    outputFile.write(str(bigPercentile)+"\n")
    outputFile.close()
    
    print("Analysis results written to " + outputFilePath)


def analyzeSignature(signature):
    logsDir = os.path.join(LOGS_DIR, signature)
    
    if not os.path.isdir(logsDir):
        raise Exception("Logs from scenario {} are missing.".format(signature))
    if not os.path.exists(CSV_DIR):
        os.makedirs(CSV_DIR)
    
    print("Analyzing logs of signature: " + signature)
                    
    for root, dirs, files in os.walk(logsDir):
        for logDirName in dirs:
            if (len(analyzed) >= CORES) :
                finalizeOldestAnalysis()

            analysis = Process(target = analyzeLog, args = (signature, logDirName))
            analyzed.append(analysis)
            analysis.start()

    # finalize the rest
    while len(analyzed) > 0:
        finalizeOldestAnalysis()


def printHelp():
    print("\nUsage:")
    print("\tpython Analyze.py scenario iterations")
    print("\nArguments:")
    print("\tscenario - index of the required scenario")
    print("\titerations - number of iterations of the simulation")
    print("\nDescription:")
    print("\tThe scenario to analyze has to be already simulated and the logs "
          "produced by the simulation has to be available."
          "\n\tThe available scenarios to simulate and analyze can be found by running:"
          "\n\t\tpython Scenarios.py")


def extractScenarioArgs(args):
    # Check argument count (1st argument is this script name)
    if len(args) < 3:
        raise ArgError("Invalid arguments")
    try:
        scenarioArgs = [];
        # 1st arg is script name, last is number of iterations
        for i in range(1, len(args) - 1):
            s = int(args[i])
            if len(scenarios) <= s:
                raise ArgError("Invalid arguments")
            if(s < 0):
                raise ArgError("Invalid arguments")
            scenarioArgs.append(s)
        return scenarioArgs
    except ValueError:
        raise ArgError(ValueError)


def extractIterationsArg(args):
    # Check argument count (1st argument is this script name)
    argsLen = len(args) 
    if argsLen < 3:
        raise ArgError("Invalid arguments")
    try:
        cnt = int(args[argsLen - 1])
        if(cnt < 1):
            raise ArgError("Invalid arguments")
        return cnt
    except ValueError:
        raise ArgError(ValueError)


if __name__ == '__main__':
    try:
        scenarioArgs = extractScenarioArgs(sys.argv)
        iterations = extractIterationsArg(sys.argv)
        
        start = time.time()
        for scenario in scenarioArgs:
            signature = getScenarioSignature(scenario, iterations)
            print("Analyzing scenario {} with signature {}"
                  .format(scenario, signature))
            analyzeSignature(signature)
        end = time.time()
        
        print("Analysis lasted for {:.2f} mins".format((end-start)/60))
        print("Results placed to {}".format(CSV_DIR))
    except ArgError:
        printHelp()
    except Exception as e:
        print(e.__str__())

