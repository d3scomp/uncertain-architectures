'''
Created on Jan 8, 2016

Definition of available scenarios to simulate.
Provides a method to generate a signature of a scenario to distinguish
folders and files produced by simulating and analyzing the scenario.

Explanations of used shortcuts:
    DDF - Dirt detection failure
    CS - Collaborative sensing
    DF - Dock failure
    FCI - Faulty component isolation
    UMS - Unspecified mode switching

An exclamation mark (!) in front of a shortcut means the feature/failure
is inactive, otherwise it is active.
The probability following the UMS is the starting UMS probability.

@author: Ilias
@author: Dominik Skoda
'''
from Configuration import SIMULATION_DURATION, LEARNING_PHASE_END,\
    LEARNING_PHASE_START
from Configuration import DDF_DEFAULT_TIME
from Configuration import DF_DEFAULT_TIME

#################################################
# SCENARIOS
#################################################

# Parameters
DDF = "DDF" # Dirt detection failure
DDF_TIME = "DDF_time"
CS = "CS" # Collaborative sensing
DF = "DF" # Dock failure
DF_TIME = "DF_time"
FCI = "FCI" # Faulty component isolation
UMS = "UMS" # Unspecified mode switching
PROBABILITY = "Probability" # Starting probability for UMS
PROBABILITY_STEP = "step" # Probability step for UMS
UMS_START = "UMS_start_time" # The UMS start time
UMS_END = "UMS_end_time" # The UMS end time
ROBOT_CNT = "robot_cnt" # The robot count
DURATION = "duration" # The Simulation Duration

# Scenarios
scenarios = []
# 3 Bot
scenarios.append({DDF:False, DF:False, UMS:False, ROBOT_CNT:3, DURATION:SIMULATION_DURATION})
scenarios.append({DDF:True, CS:False, DF:False, UMS:False, ROBOT_CNT:3, DURATION:SIMULATION_DURATION})
scenarios.append({DDF:True, CS:True, DF:False, UMS:False, ROBOT_CNT:3, DURATION:SIMULATION_DURATION})
scenarios.append({DDF:False, DF:True, FCI:False, UMS:False, ROBOT_CNT:3, DURATION:SIMULATION_DURATION})
scenarios.append({DDF:False, DF:True, FCI:True, UMS:False, ROBOT_CNT:3, DURATION:SIMULATION_DURATION})
# 5 Bot
scenarios.append({DDF:False, DF:True, DF_TIME:0, FCI:True, UMS:False, ROBOT_CNT:5,
                  DURATION:(SIMULATION_DURATION + LEARNING_PHASE_END)})
scenarios.append({DDF:False, DF:True, FCI:True, UMS:True,
                  PROBABILITY:0.000001,
                  PROBABILITY_STEP:0.0000005,
                  UMS_START:LEARNING_PHASE_START,
                  UMS_END:LEARNING_PHASE_END,
                  ROBOT_CNT:5,
                  DURATION:(SIMULATION_DURATION + LEARNING_PHASE_END)})
scenarios.append({DDF:True, CS:True, DF:True, FCI: True, UMS:True,
                  PROBABILITY:0.000001,
                  PROBABILITY_STEP:0.0000005,
                  UMS_START:LEARNING_PHASE_START,
                  UMS_END:LEARNING_PHASE_END,
                  ROBOT_CNT:5,
                  DURATION:(SIMULATION_DURATION + LEARNING_PHASE_END)})

# All fails, no meta-adaptation
scenarios.append({DDF:True, CS:False, DF:True, FCI: False, UMS:False})

#################################################


def getSignature(scenario, iterations = 0, detailed = False):
    ''' Compiles the signature of the given scenario. '''
    outputSignature = []
    outputSignature.append("{:02})-".format(scenarios.index(scenario)))
    if scenario[DDF]:
        outputSignature.append("DDF-")
        if detailed:
            if DDF_TIME in scenario:
                outputSignature.append(str(scenario[DDF_TIME]))
            else:    
                outputSignature.append(str(DDF_DEFAULT_TIME))
            outputSignature.append("-")
        outputSignature.append("CS-" if (scenario[CS]) else "!CS-")
    else:
        outputSignature.append("!DDF-")
    if scenario[DF]:
        outputSignature.append("DF-")
        if detailed:
            if DF_TIME in scenario:
                outputSignature.append(str(scenario[DF_TIME]))
            else:    
                outputSignature.append(str(DF_DEFAULT_TIME))
            outputSignature.append("-")
        outputSignature.append("FCI-" if (scenario[FCI]) else "!FCI-")
    else:
        outputSignature.append("!DF-")
    if scenario[UMS]:
        outputSignature.append("UMS")
        if detailed:
            outputSignature.append("-P" + str(scenario[PROBABILITY]))
            outputSignature.append("-S" + str(scenario[PROBABILITY_STEP]))
            outputSignature.append("-B" + str(scenario[UMS_START]))
            outputSignature.append("-E" + str(scenario[UMS_END]))
        else:
            outputSignature.append("-" + str(scenarios.index(scenario)))
    else:
        outputSignature.append("!UMS")
    if iterations > 0:
        outputSignature.append("-it-" + str(iterations))
    return ''.join(outputSignature)


def getScenarioSignature(scenarioIndex, iterations = 0):
    ''' Compiles the signature of the given scenario. '''
    return getSignature(scenarios[scenarioIndex], iterations)


def listScenarios():
    print("\nExplanations of used shortcuts:")
    print("DDF - Dirt detection failure")
    print("CS - Collaborative sensing")
    print("DF - Dock failure")
    print("FCI - Faulty component isolation")
    print("UMS - Unspecified mode switching")
    print("P - Starting probability for UMS")
    print("S - Probability step for UMS")
    print("B - The UMS start time (beginning)")
    print("E - The UMS end time")
    print("\nAn exclamation mark (!) in front of a shortcut "
          "means the feature/failure is inactive, "
          "otherwise it is active.\nA number following "
          "the UMS is the scenario index that contains the "
          "detailed UMS configuration.")
    print("\nAvailable Scenarios:")
    for scenario in scenarios:
        print(getSignature(scenario, detailed = True))
    print("\n")


if __name__ == '__main__':
    listScenarios()
    