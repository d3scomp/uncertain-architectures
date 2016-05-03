'''
Created on Dec 8, 2015

This script extracts and computes the dirtiness duration times from logs
produced by simulations.

@author: Ilias
@author: Dominik Skoda
'''

import os
import sys
import xml.etree.ElementTree as etree
from Scenarios import *
from Configuration import *


TRIM_FULL_PACKAGE_NAME = True


class Modes:
    
    def __init__(self, fromMode, toMode):
        self.fromMode = fromMode
        self.toMode = toMode
        
    def __str__(self):
        return "{} -> {}".format(self.fromMode, self.toMode)
    
    def __eq__(self, other):
        return isinstance(other, self.__class__) \
            and self.fromMode == other.fromMode \
            and self.toMode == other.toMode
            
    def __ne__(self, other):
        return not self.__eq__(other)
    
    def __lt__(self, other):
        return self.fromMode < other.fromMode \
            or (self.fromMode == other.fromMode \
                and self.toMode < other.toMode)
            
    def __gt__(self, other):
        return self.fromMode > other.fromMode \
            or (self.fromMode == other.fromMode \
                and self.toMode > other.toMode)
    
    def __hash__(self):
        return hash(self.fromMode) + 107 * hash(self.toMode)


def incrementDictValue(inDict, item, value):
    if item not in inDict.keys():
        inDict[item] = 0
    inDict[item] += value


def extractTransitionRecords(records, transitions):
    for r in records:
        fromMode = r.find('oldMode').text
        toMode = r.find('newMode').text
        if TRIM_FULL_PACKAGE_NAME:
            fromMode = fromMode[fromMode.rindex('.')+1:]
            toMode = toMode[toMode.rindex('.')+1:]
        modes = Modes(fromMode, toMode)
        incrementDictValue(transitions, modes, 1)


def mergeDicts(inputDict, outputDict):
    for item in inputDict.keys():
        incrementDictValue(outputDict, item, inputDict[item])


def analyzeLog(simulationSignature, logDirName, phase_sep):
    logDir = os.path.join(LOGS_DIR, simulationSignature, logDirName)
    print("Analyzing " + logDir)
    
    tree = etree.parse(os.path.join(logDir, RUNTIME_LOG_FILE))  
    root = tree.getroot()
        
    transitions = {}
    nonDetTransitions = {}
    transitionsRecords = root.findall("*[@eventType='cz.cuni.mff.d3s.jdeeco.modes.runtimelog.ModeRecord']")
    nonDerTransitionsRecords = root.findall("*[@eventType='cz.cuni.mff.d3s.jdeeco.adaptation.modeswitching.runtimelog.NonDetModeRecord']")
    
    print("Found " + str(len(transitionsRecords)) + " transition records")
    print("Found " + str(len(nonDerTransitionsRecords)) + " non deterministic transition records")
    
    extractTransitionRecords(transitionsRecords, transitions)
    extractTransitionRecords(nonDerTransitionsRecords, nonDetTransitions)

    return (transitions, nonDetTransitions)


def printTransitions(transitions):
    lineLen = 0
    for transition in transitions.keys():
        lineLen = max(lineLen, len(str(transition)))
    for transition in sorted(transitions.keys()):
        indent = lineLen - len(str(transition))
        print("{} : {}{}".format(transition, " " * indent, transitions[transition]))   


def countTransitions(transitions):
    count = 0
    for transition in transitions.keys():
        count += transitions[transition]
        
    return count


def analyzeSignature(signature, phase_sep):
    logsDir = os.path.join(LOGS_DIR, signature)
    
    if not os.path.isdir(logsDir):
        raise Exception("Logs from scenario {} are missing.".format(signature))
    if not os.path.exists(CSV_DIR):
        os.makedirs(CSV_DIR)
    
    transitions = {}
    nonDetTransitions = {}
    
    #for root, dirs, files in os.walk(logsDir):
    for _, dirs, _ in os.walk(logsDir):
        for logDirName in dirs:
            res = analyzeLog(signature, logDirName, phase_sep)
            mergeDicts(res[0], transitions)
            mergeDicts(res[1], nonDetTransitions)

    print("\nNumber of transitions taken: {}".format(countTransitions(transitions)))
    printTransitions(transitions)
    print("\nNumber of non deterministic transitions taken: {}".format(countTransitions(nonDetTransitions)))
    printTransitions(nonDetTransitions)
    

def printHelp():
    print("\nUsage:")
    print("\tpython ModeSwitchStat.py scenario1 [scenario2 [...]] ")
    print("\nArguments:")
    print("\tscenario - index of the required scenario")
    print("\nDescription:")
    print("\tThe scenario to analyze has to be already simulated and the logs "
          "produced by the simulation has to be available."
          "\n\tThe available scenarios to simulate and analyze can be found by running:"
          "\n\t\tpython Scenarios.py")


def extractArgs(args):
    # Check argument count (1st argument is this script name)
    if len(args) < 2:
        raise ArgError("At least one scenario argument is required")
    
    scenarioIndices = []
    for i in range(1, len(args)):
        scenarioIndex = int(args[i])
        if len(scenarios) <= scenarioIndex or 0 > scenarioIndex:
            raise ArgError("Scenario index value {} is out of range.".format(scenarioIndex))
        scenarioIndices.append(scenarioIndex)
    
    return scenarioIndices


if __name__ == '__main__':
    try:            
        scenarioArgs = extractArgs(sys.argv)
        
        for scenario in scenarioArgs:
            signature = getScenarioSignature(scenario)
            print("\n\nAnalyzing scenario {} with signature {}\n".format(scenario, signature))
            analyzeSignature(signature, scenarioArgs[scenario])
        
    except ArgError:
        printHelp()
    except Exception as e:
        print(e.__str__())

