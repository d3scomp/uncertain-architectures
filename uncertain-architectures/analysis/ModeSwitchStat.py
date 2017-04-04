'''
Created on Dec 8, 2015

This script extracts and computes the dirtiness duration times from logs
produced by simulations.

@author: Ilias
@author: Dominik Skoda
'''

import os
import sys
import re
import xml.etree.ElementTree as etree
from Scenarios import *
from Configuration import *


# TRIM_FULL_PACKAGE_NAME = True


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
        modes = Modes(fromMode, toMode)
        incrementDictValue(transitions, modes, 1)


def mergeDicts(inputDict, outputDict):
    for item in inputDict.keys():
        incrementDictValue(outputDict, item, inputDict[item])


def analyzeLog(scenario, logDirName):
    logDir = os.path.join(LOGS_DIR, getSignature(scenario), logDirName)
    print("Analyzing " + logDir)
    
    tree = etree.parse(os.path.join(logDir, RUNTIME_LOG_FILE))  
    root = tree.getroot()
        
    transitions = {}
    transitionsRecords = root.findall("*[@eventType='cz.cuni.mff.d3s.jdeeco.modes.runtimelog.ModeRecord']")
    
    print("Found " + str(len(transitionsRecords)) + " transition records")
    
    extractTransitionRecords(transitionsRecords, transitions)
    return transitions


def printTransitions(transitions, file, transition = None):
    f = open(file, 'a')
    
    msg = "\n"
    print(msg)
    f.write(msg + "\n")
        
    if(transition != None):
        msg = "Added transition: {}".format(transition)
        print(msg)
        f.write(msg + "\n")
        
    msg = "Number of transitions taken: {}".format(countTransitions(transitions))
    print(msg)
    f.write(msg + "\n")
    
    lineLen = 0
    for transition in transitions.keys():
        lineLen = max(lineLen, len(str(transition)))
    for transition in sorted(transitions.keys()):
        indent = lineLen - len(str(transition))
        msg = "{} : {}{}".format(transition, " " * indent, transitions[transition])
        print(msg)
        f.write(msg + "\n")
    
    f.close()


def countTransitions(transitions):
    count = 0
    for transition in transitions.keys():
        count += transitions[transition]
        
    return count

def analyzeScenario(scenario):
    
    logsDir = os.path.join(LOGS_DIR, getSignature(scenario))
    
    if not os.path.isdir(logsDir):
        raise Exception("Logs from scenario {} are missing.".format(scenarios.index(scenario)))
    if not os.path.exists(CSV_DIR):
        os.makedirs(CSV_DIR)
    
    validDir = re.compile('.+_\d+')
    output = os.path.join(LOGS_DIR, getSignature(scenario), "ModeSwitchStat.txt")
    f = open(output, 'w')
    f.close()
    
    if scenario[UMS]:
        fromTo = re.compile('(\w+)-(\w+)_\d+')
        transitions = {}
        for _, dirs, _ in os.walk(logsDir):
            for logDirName in dirs:
                match = fromTo.match(logDirName)
                if(match != None):
                    fromT = match.group(1)
                    toT = match.group(2)
                    res = analyzeLog(scenario, logDirName)
                    t = "{}-{}".format(fromT, toT)
                    if not transitions.__contains__(t):
                        transitions[t] = {}
                    mergeDicts(res, transitions[t])
        for transition in transitions.keys():
            printTransitions(transitions[transition], output, transition)
    else:
        transitions = {}
        for _, dirs, _ in os.walk(logsDir):
            for logDirName in dirs:
                if(validDir.match(logDirName) != None):
                    res = analyzeLog(scenario, logDirName)
                    mergeDicts(res, transitions)

        printTransitions(transitions, output)
    

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
        
        for scenarioIndex in scenarioArgs:
            scenario = scenarios[scenarioIndex]
            print("\n\nAnalyzing scenario {}\n".format(scenarioIndex))
            analyzeScenario(scenario)
        
    except ArgError:
        printHelp()
    except Exception as e:
        print(e.__str__())

