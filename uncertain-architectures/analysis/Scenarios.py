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

# Scenarios
scenarios = []
scenarios.append({DDF:False, DF:False, UMS:False})
scenarios.append({DDF:True, CS:False, DF:False, UMS:False})
scenarios.append({DDF:True, CS:True, DF:False, UMS:False})
scenarios.append({DDF:False, DF:True, FCI: False, UMS:False})
scenarios.append({DDF:False, DF:True, FCI:True, UMS:False})
scenarios.append({DDF:False, DF:False, UMS:True, PROBABILITY:0.0001})
scenarios.append({DDF:True, CS:False, DF:True, FCI: False, UMS:False})
scenarios.append({DDF:True, CS:True, DF:True, FCI: True, UMS:True, PROBABILITY:0.0001})

#################################################


def getSignature(scenario, iterations = 0):
    outputFileName = []
    if scenario[DDF]:
        outputFileName.append("DDF-")
        outputFileName.append("CS-" if (scenario[CS]) else "!CS-")
    else:
        outputFileName.append("!DDF-")
    if scenario[DF]:
        outputFileName.append("DF-") 
        outputFileName.append("FCI-" if (scenario[FCI]) else "!FCI-")
    else:
        outputFileName.append("!DF-")
    if scenario[UMS]:
        outputFileName.append(("UMS-" + str(scenario[PROBABILITY])))
    else:
        outputFileName.append("!UMS")
    if iterations > 0:
        outputFileName.append("-it-" + str(iterations))
    return ''.join(outputFileName)


def getScenarioSignature(scenario, iterations = 0):
    return getSignature(scenarios[scenario], iterations)


def listScenarios():
    print("\nExplanations of used shortcuts:")
    print("DDF - Dirt detection failure")
    print("CS - Collaborative sensing")
    print("DF - Dock failure")
    print("FCI - Faulty component isolation")
    print("UMS - Unspecified mode switching")
    print("\nAn exclamation mark (!) in front of a shortcut "
          "means the feature/failure is inactive, "
          "otherwise it is active.\nThe probability following "
          "the UMS is the starting UMS probability.")
    print("\nAvailable Scenarios:")
    for i, scenario in enumerate(scenarios):
        print("{}) {}".format(i, getSignature(scenario)))
    print("\n")


if __name__ == '__main__':
    listScenarios()
    