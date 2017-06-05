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

from Configuration import *

#################################################
# SCENARIOS
#################################################

# Parameters
SCENARIO_NAME = "SCENARIO_NAME"
LOG_DIR = "LOG_DIR"
DDF = "DIRT_DETECTION_FAILURE_ON" # Dirt detection failure
DDF_TIME = "DIRT_DETECTION_FAILURE_TIME"
CS = "CORRELATION_ON" # Collaborative sensing
DF = "DOCK_FAILURE_ON" # Dock failure
DF_TIME = "DOCK_FAILURE_TIME"
FCI = "ROLE_REMOVAL_ON" # Faulty component isolation
ROBOT_CNT = "ROBOT_COUNT" # The robot count
DOCK_CNT = "DOCK_COUNT" # The dock count
DURATION = "SIMULATION_DURATION" # The Simulation Duration
WARM_UP_TIME = "WARM_UP_TIME" # The warm up time
WITH_SEED = "WITH_SEED"
SEED = "ENVIRONMENT_SEED"

# ENHANCING MODE SWITCHING
UMS = "NON_DETERMINISM_ON" # Unspecified mode switching
TRANSITION_PROBABILITY = "TRANSITION_PROBABILITY" 
TRANSITION_PRIORITY = "TRANSITION_PRIORITY"
NON_DETERMINISM_TRAINING = "NON_DETERMINISM_TRAINING"
NON_DETERMINISM_TRAINING_DEGREE = "NON_DETERMINISM_TRAINING_DEGREE"
NON_DETERMINISM_TRAIN_TRANSITIONS = "NON_DETERMINISM_TRAIN_TRANSITIONS"
NON_DETERMINISM_TRAINING_OUTPUT = "NON_DETERMINISM_TRAINING_OUTPUT"

# Mode Switch Properties
MSP = "MODE_SWITCH_PROPS_ON";
MODE_SWITCH_PROPS_TRAINING = "MODE_SWITCH_PROPS_TRAINING";
MODE_SWITCH_PROPS_TRAINING_DEGREE = "MODE_SWITCH_PROPS_TRAINING_DEGREE"
MODE_SWITCH_PROPS_PROPERTIES = "MODE_SWITCH_PROPS_PROPERTIES";

# Transitions not present in the default robot's mode chart
missingTransitions = [
    ("DirtApproachMode","ChargingMode"),
    ("DirtApproachMode","WaitingMode"),
    ("DirtApproachMode","DeadBatteryMode"),
    ("ChargingMode","DirtApproachMode"),
    ("ChargingMode","WaitingMode"),
    ("ChargingMode","CleanMode"),
    ("ChargingMode","DockingMode"),
    ("ChargingMode","DeadBatteryMode"),
    ("WaitingMode","DirtApproachMode"),
    ("WaitingMode","ChargingMode"),
    ("WaitingMode","CleanMode"),
    ("WaitingMode","SearchMode"),
    ("WaitingMode","DeadBatteryMode"),
    ("CleanMode","ChargingMode"),
    ("CleanMode","WaitingMode"),
    ("CleanMode","DeadBatteryMode"),
    ("DockingMode","DirtApproachMode"),
    ("DockingMode","CleanMode"),
    ("DockingMode","SearchMode"),
    ("SearchMode","ChargingMode"),
    ("SearchMode","WaitingMode"),
    ("SearchMode","CleanMode"),
    ("DeadBatteryMode","DirtApproachMode"),
    ("DeadBatteryMode","ChargingMode"),
    ("DeadBatteryMode","WaitingMode"),
    ("DeadBatteryMode","CleanMode"),
    ("DeadBatteryMode","DockingMode"),
    ("DeadBatteryMode","SearchMode")]
missingTransitionsReduced = [
    ("SearchMode","WaitingMode"),
    ("DockingMode","SearchMode"),
    ("DockingMode","DirtApproachMode"),
    ("DeadBatteryMode","WaitingMode")]

# Properties used in scenarios with mode switching properties adjustment
adjustedProperties = [
    ("CHARGED_LEVEL", "0.9"),
    ("CHARGED_LEVEL", "0.7"),
    ("CHARGED_LEVEL", "0.5"),
    ("DRAINED_LEVEL", "0.1"),
    ("DRAINED_LEVEL", "0.3"),
    ("DRAINED_LEVEL", "0.5"),
    ("FOUND_ENOUGH", "1"),
    ("FOUND_ENOUGH", "3"),
    ("FOUND_ENOUGH", "7"),
    ("FOUND_ENOUGH", "9"),
    ("CLEANED_ENOUGH", "1"),
    ("CLEANED_ENOUGH", "2"),
    ("CLEANED_ENOUGH", "3")]

adjustedPropertiesReduced = [
                  ("CHARGED_LEVEL", "0.7"),
                  ("DRAINED_LEVEL", "0.3"),
                  ("FOUND_ENOUGH", "3"),
                  ("CLEANED_ENOUGH", "2")]

# Scenarios
scenarios = []
# Baseline 3 docks
scenarios.append({SCENARIO_NAME:"Baseline (3 docks)",
                  DDF:False, DF:False, UMS:False, MSP:False,
                  ROBOT_CNT:4, DOCK_CNT:3,
                  DURATION:SIMULATION_DURATION,
                  WARM_UP_TIME:SIMULATION_WARM_UP})
# Baseline 1 dock (for UMS and MSP)
scenarios.append({SCENARIO_NAME:"Baseline (1 docks)",
                  DDF:False, DF:False, UMS:False, MSP:False,
                  NON_DETERMINISM_TRAINING:False,
                  ROBOT_CNT:4, DOCK_CNT:1,
                  DURATION:SIMULATION_DURATION,
                  WARM_UP_TIME:SIMULATION_WARM_UP})
# Dirt detection failure
scenarios.append({SCENARIO_NAME:"CS failure\t",
                  DDF:True, CS:False, DF:False, UMS:False, MSP:False,
                  ROBOT_CNT:4, DOCK_CNT:3,
                  DURATION:SIMULATION_DURATION,
                  WARM_UP_TIME:SIMULATION_WARM_UP})
scenarios.append({SCENARIO_NAME:"CS remedy\t",
                  DDF:True, CS:True, DF:False, UMS:False, MSP:False,
                  ROBOT_CNT:4, DOCK_CNT:3,
                  DURATION:SIMULATION_DURATION,
                  WARM_UP_TIME:SIMULATION_WARM_UP})
# Dock failure
scenarios.append({SCENARIO_NAME:"FCI failure",
                  DDF:False, DF:True, FCI:False, UMS:False, MSP:False,
                  ROBOT_CNT:4, DOCK_CNT:3,
                  DURATION:SIMULATION_DURATION,
                  WARM_UP_TIME:SIMULATION_WARM_UP})
scenarios.append({SCENARIO_NAME:"FCI remedy\t",
                  DDF:False, DF:True, FCI:True, UMS:False, MSP:False,
                  ROBOT_CNT:4, DOCK_CNT:3,
                  DURATION:SIMULATION_DURATION,
                  WARM_UP_TIME:SIMULATION_WARM_UP})
# Enhanced mode switching
scenarios.append({SCENARIO_NAME:"EMS p=0.001 deg=1",
                  DDF:False, DF:False, UMS:True, MSP:False,
                  NON_DETERMINISM_TRAINING:True,
                  NON_DETERMINISM_TRAINING_DEGREE:1,
                  TRANSITION_PROBABILITY:0.001,
                  TRANSITION_PRIORITY:10, 
                  ROBOT_CNT:4, DOCK_CNT:1,
                  DURATION:SIMULATION_DURATION,
                  WARM_UP_TIME:SIMULATION_WARM_UP})
scenarios.append({SCENARIO_NAME:"EMS p=0.01 deg=1",
                  DDF:False, DF:False, UMS:True, MSP:False,
                  NON_DETERMINISM_TRAINING:True,
                  NON_DETERMINISM_TRAINING_DEGREE:1,
                  TRANSITION_PROBABILITY:0.01,
                  TRANSITION_PRIORITY:10, 
                  ROBOT_CNT:4, DOCK_CNT:1,
                  DURATION:SIMULATION_DURATION,
                  WARM_UP_TIME:SIMULATION_WARM_UP})
scenarios.append({SCENARIO_NAME:"EMS p=0.001 deg=2",
                  DDF:False, DF:False, UMS:True, MSP:False,
                  NON_DETERMINISM_TRAINING:True,
                  NON_DETERMINISM_TRAINING_DEGREE:2,
                  TRANSITION_PROBABILITY:0.001,
                  TRANSITION_PRIORITY:10, 
                  ROBOT_CNT:4, DOCK_CNT:1,
                  DURATION:SIMULATION_DURATION,
                  WARM_UP_TIME:SIMULATION_WARM_UP})
scenarios.append({SCENARIO_NAME:"EMS p=0.01 deg=2",
                  DDF:False, DF:False, UMS:True, MSP:False,
                  NON_DETERMINISM_TRAINING:True,
                  NON_DETERMINISM_TRAINING_DEGREE:2,
                  TRANSITION_PROBABILITY:0.01,
                  TRANSITION_PRIORITY:10, 
                  ROBOT_CNT:4, DOCK_CNT:1,
                  DURATION:SIMULATION_DURATION,
                  WARM_UP_TIME:SIMULATION_WARM_UP})
# Mode Switch Properties
scenarios.append({SCENARIO_NAME:"MSP deg=1\t",
                  DDF:False, DF:False, UMS:False, MSP:True,
                  MODE_SWITCH_PROPS_TRAINING:True,
                  MODE_SWITCH_PROPS_TRAINING_DEGREE:1,
                  ROBOT_CNT:4, DOCK_CNT:1,
                  DURATION:SIMULATION_DURATION,
                  WARM_UP_TIME:SIMULATION_WARM_UP})
scenarios.append({SCENARIO_NAME:"MSP deg=2\t",
                  DDF:False, DF:False, UMS:False, MSP:True,
                  MODE_SWITCH_PROPS_TRAINING:True,
                  MODE_SWITCH_PROPS_TRAINING_DEGREE:2,
                  ROBOT_CNT:4, DOCK_CNT:1,
                  DURATION:SIMULATION_DURATION,
                  WARM_UP_TIME:SIMULATION_WARM_UP})


#################################################


def getSignature(scenario, iterations = 0, detailed = False):
    ''' Compiles the signature of the given scenario. '''
    outputSignature = []
    outputSignature.append("{:02})-".format(scenarios.index(scenario)))
    
    if detailed:
        if SCENARIO_NAME in scenario:
            outputSignature.append(" {}\t".format(scenario[SCENARIO_NAME]))
        else:
            raise Exception("Scenario name missing.")
    
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
            if scenario[NON_DETERMINISM_TRAINING]:
                outputSignature.append("-T")
                outputSignature.append(str(scenario[NON_DETERMINISM_TRAINING_DEGREE]))
            else:
                outputSignature.append("-!T")
            outputSignature.append("-P" + str(scenario[TRANSITION_PROBABILITY]))
        else:
            outputSignature.append("-" + str(scenarios.index(scenario)))
    else:
        outputSignature.append("!UMS")
    if scenario[MSP]:
        outputSignature.append("-MSP")
        if detailed:
            if scenario[MODE_SWITCH_PROPS_TRAINING]:
                outputSignature.append("-T")
                outputSignature.append(str(scenario[MODE_SWITCH_PROPS_TRAINING_DEGREE]))
            else:
                outputSignature.append("-!T")
        else:
            outputSignature.append("-" + str(scenarios.index(scenario)))
    if detailed:
        outputSignature.append("-Robot" + str(scenario[ROBOT_CNT]))
        outputSignature.append("-Dock" + str(scenario[DOCK_CNT]))
        outputSignature.append("-Duration" + str(scenario[DURATION]))
        outputSignature.append("-WarmUp" + str(scenario[WARM_UP_TIME]))
    if iterations > 0:
        outputSignature.append("-it-" + str(iterations) + "-")
    return ''.join(outputSignature)


def getScenarioSignature(scenarioIndex, iterations = 0):
    ''' Compiles the signature of the given scenario. '''
    return getSignature(scenarios[scenarioIndex], iterations)


def getLogFile(scenario, iteration, specifier = None):
    if(specifier != None):
        return os.path.join(LOGS_DIR,
                        getSignature(scenario),
                        specifier + "log_" + str(iteration))
        
    return os.path.join(LOGS_DIR,
                        getSignature(scenario),
                        'log_' + str(iteration))
    
    
def getUMSLogFile(scenario, iteration, transitions = None):
    if(scenario[UMS]):
        if(transitions != None):
            return os.path.join(LOGS_DIR,
                            getSignature(scenario),
                            UMS_LOGS,
                            transitions,
                            'log_' + str(iteration))
    
    return os.path.join(LOGS_DIR,
                        getSignature(scenario),
                        UMS_LOGS,
                        'log_' + str(iteration))
    

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
    