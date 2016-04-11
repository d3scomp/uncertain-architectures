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
from Configuration import SIMULATION_DURATION

#################################################
# SCENARIOS
#################################################

# Parameters
DDF = "DDF" # Dirt detection failure
CS = "CS" # Collaborative sensing
DF = "DF" # Dock failure
FCI = "FCI" # Faulty component isolation
UMS = "UMS" # Unspecified mode switching
PROBABILITY = "Probability" # Starting probability for UMS
PROBABILITY_STEP = "step" # Probability step for UMS
UMS_START = "start_time" # The UMS start time
UMS_END = "end_time" # The UMS end time

# Scenarios
scenarios = []
scenarios.append({DDF:False, DF:False, UMS:False})
scenarios.append({DDF:True, CS:False, DF:False, UMS:False})
scenarios.append({DDF:True, CS:True, DF:False, UMS:False})
scenarios.append({DDF:False, DF:True, FCI: False, UMS:False})
scenarios.append({DDF:False, DF:True, FCI:True, UMS:False})
scenarios.append({DDF:False, DF:True, FCI:True, UMS:True,
                  PROBABILITY:0.0001,
                  PROBABILITY_STEP:0.00005,
                  UMS_START:0,
                  UMS_END:SIMULATION_DURATION})
scenarios.append({DDF:True, CS:False, DF:True, FCI: False, UMS:False})
scenarios.append({DDF:True, CS:True, DF:True, FCI: True, UMS:True,
                  PROBABILITY:0.0001,
                  PROBABILITY_STEP:0.00005,
                  UMS_START:0,
                  UMS_END:SIMULATION_DURATION})
# Different UMS starting probability and step
scenarios.append({DDF:False, DF:True, FCI:True, UMS:True,
                  PROBABILITY:0.000001,
                  PROBABILITY_STEP:0.0000005,
                  UMS_START:0,
                  UMS_END:SIMULATION_DURATION})
scenarios.append({DDF:False, DF:True, FCI:True, UMS:True,
                  PROBABILITY:0.00001,
                  PROBABILITY_STEP:0.000005,
                  UMS_START:0,
                  UMS_END:SIMULATION_DURATION})
scenarios.append({DDF:False, DF:True, FCI:True, UMS:True,
                  PROBABILITY:0.001,
                  PROBABILITY_STEP:0.0005,
                  UMS_START:0,
                  UMS_END:SIMULATION_DURATION})
scenarios.append({DDF:False, DF:True, FCI:True, UMS:True,
                  PROBABILITY:0.01,
                  PROBABILITY_STEP:0.005,
                  UMS_START:0,
                  UMS_END:SIMULATION_DURATION})
# End in 300000
scenarios.append({DDF:False, DF:True, FCI:True, UMS:True,
                  PROBABILITY:0.000001,
                  PROBABILITY_STEP:0.0000005,
                  UMS_START:0,
                  UMS_END:300000})
scenarios.append({DDF:False, DF:True, FCI:True, UMS:True,
                  PROBABILITY:0.00001,
                  PROBABILITY_STEP:0.000005,
                  UMS_START:0,
                  UMS_END:300000})
scenarios.append({DDF:False, DF:True, FCI:True, UMS:True,
                  PROBABILITY:0.001,
                  PROBABILITY_STEP:0.0005,
                  UMS_START:0,
                  UMS_END:300000})
scenarios.append({DDF:False, DF:True, FCI:True, UMS:True,
                  PROBABILITY:0.01,
                  PROBABILITY_STEP:0.005,
                  UMS_START:0,
                  UMS_END:300000})
# End in 150000
scenarios.append({DDF:False, DF:True, FCI:True, UMS:True,
                  PROBABILITY:0.000001,
                  PROBABILITY_STEP:0.0000005,
                  UMS_START:0,
                  UMS_END:150000})
scenarios.append({DDF:False, DF:True, FCI:True, UMS:True,
                  PROBABILITY:0.00001,
                  PROBABILITY_STEP:0.000005,
                  UMS_START:0,
                  UMS_END:150000})
scenarios.append({DDF:False, DF:True, FCI:True, UMS:True,
                  PROBABILITY:0.001,
                  PROBABILITY_STEP:0.0005,
                  UMS_START:0,
                  UMS_END:150000})
scenarios.append({DDF:False, DF:True, FCI:True, UMS:True,
                  PROBABILITY:0.01,
                  PROBABILITY_STEP:0.005,
                  UMS_START:0,
                  UMS_END:150000})
# Start in 100000 End in 300000
scenarios.append({DDF:False, DF:True, FCI:True, UMS:True,
                  PROBABILITY:0.000001,
                  PROBABILITY_STEP:0.0000005,
                  UMS_START:100000,
                  UMS_END:300000})
scenarios.append({DDF:False, DF:True, FCI:True, UMS:True,
                  PROBABILITY:0.00001,
                  PROBABILITY_STEP:0.000005,
                  UMS_START:100000,
                  UMS_END:300000})
scenarios.append({DDF:False, DF:True, FCI:True, UMS:True,
                  PROBABILITY:0.001,
                  PROBABILITY_STEP:0.0005,
                  UMS_START:100000,
                  UMS_END:300000})
scenarios.append({DDF:False, DF:True, FCI:True, UMS:True,
                  PROBABILITY:0.01,
                  PROBABILITY_STEP:0.005,
                  UMS_START:100000,
                  UMS_END:300000})

#################################################


def getSignature(scenario, iterations = 0, detailed = False):
    ''' Compiles the signature of the given scenario. '''
    outputSignature = []
    if scenario[DDF]:
        outputSignature.append("DDF-")
        outputSignature.append("CS-" if (scenario[CS]) else "!CS-")
    else:
        outputSignature.append("!DDF-")
    if scenario[DF]:
        outputSignature.append("DF-") 
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
    for i, scenario in enumerate(scenarios):
        print("{}) {}".format(i, getSignature(scenario, detailed = True)))
    print("\n")


if __name__ == '__main__':
    listScenarios()
    