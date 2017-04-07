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
LOG_DIR = "LOG_DIR"
DDF = "DIRT_DETECTION_FAILURE_ON" # Dirt detection failure
DDF_TIME = "DIRT_DETECTION_FAILURE_TIME"
CS = "CORRELATION_ON" # Collaborative sensing
DF = "DOCK_FAILURE_ON" # Dock failure
DF_TIME = "DOCK_FAILURE_TIME"
FCI = "ROLE_REMOVAL_ON" # Faulty component isolation
# PROBABILITY = "NON_DET_INIT_PROBABILITY" # Starting probability for UMS
# PROBABILITY_STEP = "NON_DET_PROBABILITY_STEP" # Probability step for UMS
# UMS_START = "NON_DET_START_TIME" # The UMS start time
# UMS_END = "NON_DET_END_TIME" # The UMS end time
ROBOT_CNT = "ROBOT_COUNT" # The robot count
DOCK_CNT = "DOCK_COUNT" # The dock count
DURATION = "SIMULATION_DURATION" # The Simulation Duration
WARM_UP_TIME = "WARM_UP_TIME" # The warm up time
WITH_SEED = "WITH_SEED"
SEED = "ENVIRONMENT_SEED"

DIRT_GENERATION_PERIOD_LABEL = "DIRT_GENERATION_PERIOD_VAR";
DIRT_GENERATION_PERIOD_VALUE = 600;
CLEAN_PLAN_EXCHANGE_PERIOD_LABEL = "CLEAN_PLAN_EXCHANGE_PERIOD_VAR";
CLEAN_PLAN_EXCHANGE_PERIOD_VALUE = 600;

# ENHANCING MODE SWITCHING
UMS = "NON_DETERMINISM_ON" # Unspecified mode switching
TRANSITION_PROBABILITY = "TRANSITION_PROBABILITY" 
TRANSITION_PRIORITY = "TRANSITION_PRIORITY"
NON_DETERMINISM_TRAINING = "NON_DETERMINISM_TRAINING"
NON_DETERMINISM_TRAIN_FROM = "NON_DETERMINISM_TRAIN_FROM"
NON_DETERMINISM_TRAIN_TO = "NON_DETERMINISM_TRAIN_TO"
NON_DETERMINISM_TRAINING_OUTPUT = "NON_DETERMINISM_TRAINING_OUTPUT"

# Mode Switch Properties
MSP = "MODE_SWITCH_PROPS_ON";
MODE_SWITCH_PROPS_TRAINING = "MODE_SWITCH_PROPS_TRAINING";
MODE_SWITCH_PROPS_PROPERTY = "MODE_SWITCH_PROPS_PROPERTY";
MODE_SWITCH_PROPS_VALUE = "MODE_SWITCH_PROPS_VALUE";

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

# Scenarios
scenarios = []
# Baseline
scenarios.append({DDF:False, DF:False, UMS:False, MSP:False, ROBOT_CNT:4, DOCK_CNT:3,
                  DURATION:SIMULATION_DURATION, WARM_UP_TIME:SIMULATION_WARM_UP})
# Dirt detection failure
scenarios.append({DDF:True, CS:False, DF:False, UMS:False, MSP:False, ROBOT_CNT:4, DOCK_CNT:3,
                  DURATION:SIMULATION_DURATION, WARM_UP_TIME:SIMULATION_WARM_UP})
scenarios.append({DDF:True, CS:True, DF:False, UMS:False, MSP:False, ROBOT_CNT:4, DOCK_CNT:3,
                  DURATION:SIMULATION_DURATION, WARM_UP_TIME:SIMULATION_WARM_UP})
# Dock failure
scenarios.append({DDF:False, DF:True, FCI:False, UMS:False, MSP:False, ROBOT_CNT:4, DOCK_CNT:3,
                  DURATION:SIMULATION_DURATION, WARM_UP_TIME:SIMULATION_WARM_UP})
scenarios.append({DDF:False, DF:True, FCI:True, UMS:False, MSP:False, ROBOT_CNT:4, DOCK_CNT:3,
                  DURATION:SIMULATION_DURATION, WARM_UP_TIME:SIMULATION_WARM_UP})
# Too many robots for docking stations
scenarios.append({DDF:False, DF:False, UMS:False, MSP:False, ROBOT_CNT:4, DOCK_CNT:1,
                  DURATION:SIMULATION_DURATION, WARM_UP_TIME:SIMULATION_WARM_UP})
# scenarios.append({DDF:False, DF:False, UMS:True,
#                   PROBABILITY:0.0001,
#                   PROBABILITY_STEP:0.00005,
#                   UMS_START:LEARNING_PHASE_START,
#                   UMS_END:LEARNING_PHASE_END,
#                   ROBOT_CNT:4,
#                   DOCK_CNT:1, 
#                   DURATION:(SIMULATION_DURATION + LEARNING_PHASE_END),
#                   WARM_UP_TIME:SIMULATION_WARM_UP})
# # All fails, no meta-adaptation
# scenarios.append({DDF:True, CS:False, DF:True, FCI:False, UMS:False,
#                   ROBOT_CNT:4, DOCK_CNT:2, DURATION:SIMULATION_DURATION,
#                   WARM_UP_TIME:SIMULATION_WARM_UP})
# # All fails, all meta-adaptation
# scenarios.append({DDF:True, CS:True, DF:True, FCI:True, UMS:True,
#                   PROBABILITY:0.0001,
#                   PROBABILITY_STEP:0.00005,
#                   UMS_START:LEARNING_PHASE_START,
#                   UMS_END:LEARNING_PHASE_END,
#                   ROBOT_CNT:4, DOCK_CNT:2, 
#                   DURATION:(SIMULATION_DURATION + LEARNING_PHASE_END),
#                   WARM_UP_TIME:SIMULATION_WARM_UP})
# 
# # To test different probabilities of EMS
# scenarios.append({DDF:False, DF:False, UMS:True,
#                   PROBABILITY:0.000001,
#                   PROBABILITY_STEP:0.0000005,
#                   UMS_START:LEARNING_PHASE_START,
#                   UMS_END:LEARNING_PHASE_END,
#                   ROBOT_CNT:4,
#                   DOCK_CNT:1, 
#                   DURATION:(SIMULATION_DURATION + LEARNING_PHASE_END),
#                   WARM_UP_TIME:SIMULATION_WARM_UP})
# scenarios.append({DDF:False, DF:False, UMS:True,
#                   PROBABILITY:0.00001,
#                   PROBABILITY_STEP:0.000005,
#                   UMS_START:LEARNING_PHASE_START,
#                   UMS_END:LEARNING_PHASE_END,
#                   ROBOT_CNT:4,
#                   DOCK_CNT:1, 
#                   DURATION:(SIMULATION_DURATION + LEARNING_PHASE_END),
#                   WARM_UP_TIME:SIMULATION_WARM_UP})
# scenarios.append({DDF:False, DF:False, UMS:True,
#                   PROBABILITY:0.001,
#                   PROBABILITY_STEP:0.0005,
#                   UMS_START:LEARNING_PHASE_START,
#                   UMS_END:LEARNING_PHASE_END,
#                   ROBOT_CNT:4,
#                   DOCK_CNT:1, 
#                   DURATION:(SIMULATION_DURATION + LEARNING_PHASE_END),
#                   WARM_UP_TIME:SIMULATION_WARM_UP})

scenarios.append({DDF:False, DF:False, UMS:True, MSP:False,
                  NON_DETERMINISM_TRAINING:True,
                  TRANSITION_PROBABILITY:0.001,
                  TRANSITION_PRIORITY:10, 
                  ROBOT_CNT:4, DOCK_CNT:1,
                  DURATION:SIMULATION_DURATION,
                  WARM_UP_TIME:SIMULATION_WARM_UP})
scenarios.append({DDF:False, DF:False, UMS:True, MSP:False,
                  NON_DETERMINISM_TRAINING:True,
                  TRANSITION_PROBABILITY:0.01,
                  TRANSITION_PRIORITY:10, 
                  ROBOT_CNT:4, DOCK_CNT:1,
                  DURATION:SIMULATION_DURATION,
                  WARM_UP_TIME:SIMULATION_WARM_UP})
scenarios.append({DDF:False, DF:False, UMS:True, MSP:False,
                  NON_DETERMINISM_TRAINING:True,
                  TRANSITION_PROBABILITY:0,
                  TRANSITION_PRIORITY:10, 
                  ROBOT_CNT:4, DOCK_CNT:1,
                  DURATION:SIMULATION_DURATION,
                  WARM_UP_TIME:SIMULATION_WARM_UP})
scenarios.append({DDF:False, DF:False, UMS:True, MSP:False,
                  NON_DETERMINISM_TRAINING:True,
                  TRANSITION_PROBABILITY:1,
                  TRANSITION_PRIORITY:10, 
                  ROBOT_CNT:4, DOCK_CNT:1,
                  DURATION:SIMULATION_DURATION,
                  WARM_UP_TIME:SIMULATION_WARM_UP})
# UMS baseline
scenarios.append({DDF:False, DF:False, UMS:False, MSP:False,
                  NON_DETERMINISM_TRAINING:False,
                  ROBOT_CNT:4, DOCK_CNT:1,
                  DURATION:SIMULATION_DURATION,
                  WARM_UP_TIME:SIMULATION_WARM_UP})
# Mode Switch Properties
scenarios.append({DDF:False, DF:False, UMS:False, MSP:True,
                  MODE_SWITCH_PROPS_TRAINING:True,
                  MODE_SWITCH_PROPS_PROPERTY:"CHARGED_LEVEL",
                  MODE_SWITCH_PROPS_VALUE:0.9,
                  ROBOT_CNT:4, DOCK_CNT:1,
                  DURATION:SIMULATION_DURATION,
                  WARM_UP_TIME:SIMULATION_WARM_UP})
scenarios.append({DDF:False, DF:False, UMS:False, MSP:True,
                  MODE_SWITCH_PROPS_TRAINING:True,
                  MODE_SWITCH_PROPS_PROPERTY:"CHARGED_LEVEL",
                  MODE_SWITCH_PROPS_VALUE:0.7,
                  ROBOT_CNT:4, DOCK_CNT:1,
                  DURATION:SIMULATION_DURATION,
                  WARM_UP_TIME:SIMULATION_WARM_UP})
scenarios.append({DDF:False, DF:False, UMS:False, MSP:True,
                  MODE_SWITCH_PROPS_TRAINING:True,
                  MODE_SWITCH_PROPS_PROPERTY:"CHARGED_LEVEL",
                  MODE_SWITCH_PROPS_VALUE:0.5,
                  ROBOT_CNT:4, DOCK_CNT:1,
                  DURATION:SIMULATION_DURATION,
                  WARM_UP_TIME:SIMULATION_WARM_UP})
scenarios.append({DDF:False, DF:False, UMS:False, MSP:True,
                  MODE_SWITCH_PROPS_TRAINING:True,
                  MODE_SWITCH_PROPS_PROPERTY:"DRAINED_LEVEL",
                  MODE_SWITCH_PROPS_VALUE:0.1,
                  ROBOT_CNT:4, DOCK_CNT:1,
                  DURATION:SIMULATION_DURATION,
                  WARM_UP_TIME:SIMULATION_WARM_UP})
scenarios.append({DDF:False, DF:False, UMS:False, MSP:True,
                  MODE_SWITCH_PROPS_TRAINING:True,
                  MODE_SWITCH_PROPS_PROPERTY:"DRAINED_LEVEL",
                  MODE_SWITCH_PROPS_VALUE:0.3,
                  ROBOT_CNT:4, DOCK_CNT:1,
                  DURATION:SIMULATION_DURATION,
                  WARM_UP_TIME:SIMULATION_WARM_UP})
scenarios.append({DDF:False, DF:False, UMS:False, MSP:True,
                  MODE_SWITCH_PROPS_TRAINING:True,
                  MODE_SWITCH_PROPS_PROPERTY:"DRAINED_LEVEL",
                  MODE_SWITCH_PROPS_VALUE:0.5,
                  ROBOT_CNT:4, DOCK_CNT:1,
                  DURATION:SIMULATION_DURATION,
                  WARM_UP_TIME:SIMULATION_WARM_UP})
# Dirt generation with respect to cleaning plan exchange
# ratio 1:1
scenarios.append({DDF:False, DF:False, UMS:False, MSP:False, ROBOT_CNT:4, DOCK_CNT:3,
                  DURATION:SIMULATION_DURATION, WARM_UP_TIME:SIMULATION_WARM_UP,
                  DIRT_GENERATION_PERIOD_LABEL:DIRT_GENERATION_PERIOD_VALUE,
                  CLEAN_PLAN_EXCHANGE_PERIOD_LABEL:CLEAN_PLAN_EXCHANGE_PERIOD_VALUE})
# ratio 1:10
scenarios.append({DDF:False, DF:False, UMS:False, MSP:False, ROBOT_CNT:4, DOCK_CNT:3,
                  DURATION:SIMULATION_DURATION, WARM_UP_TIME:SIMULATION_WARM_UP,
                  DIRT_GENERATION_PERIOD_LABEL:DIRT_GENERATION_PERIOD_VALUE,
                  CLEAN_PLAN_EXCHANGE_PERIOD_LABEL:int(0.1*CLEAN_PLAN_EXCHANGE_PERIOD_VALUE)})
# ratio 1:100
scenarios.append({DDF:False, DF:False, UMS:False, MSP:False, ROBOT_CNT:4, DOCK_CNT:3,
                  DURATION:SIMULATION_DURATION, WARM_UP_TIME:SIMULATION_WARM_UP,
                  DIRT_GENERATION_PERIOD_LABEL:DIRT_GENERATION_PERIOD_VALUE,
                  CLEAN_PLAN_EXCHANGE_PERIOD_LABEL:int(0.01*CLEAN_PLAN_EXCHANGE_PERIOD_VALUE)})
# ratio 10:1
scenarios.append({DDF:False, DF:False, UMS:False, MSP:False, ROBOT_CNT:4, DOCK_CNT:3,
                  DURATION:SIMULATION_DURATION, WARM_UP_TIME:SIMULATION_WARM_UP,
                  DIRT_GENERATION_PERIOD_LABEL:DIRT_GENERATION_PERIOD_VALUE,
                  CLEAN_PLAN_EXCHANGE_PERIOD_LABEL:int(10*CLEAN_PLAN_EXCHANGE_PERIOD_VALUE)})

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
            outputSignature.append("-T" if (scenario[NON_DETERMINISM_TRAINING]) else "-!T")
            outputSignature.append("-P" + str(scenario[TRANSITION_PROBABILITY]))
        else:
            outputSignature.append("-" + str(scenarios.index(scenario)))
    else:
        outputSignature.append("!UMS")
    if scenario[MSP]:
        outputSignature.append("MSP")
        if detailed:            
            outputSignature.append("-T" if (scenario[MODE_SWITCH_PROPS_TRAINING]) else "-!T")
            outputSignature.append("-P" + str(scenario[MODE_SWITCH_PROPS_PROPERTY]))
            outputSignature.append("-V" + str(scenario[MODE_SWITCH_PROPS_VALUE]))
        else:
            outputSignature.append("-" + str(scenarios.index(scenario)))
    if detailed:
        outputSignature.append("-Robot" + str(scenario[ROBOT_CNT]))
        outputSignature.append("-Dock" + str(scenario[DOCK_CNT]))
        outputSignature.append("-Duration" + str(scenario[DURATION]))
        outputSignature.append("-WarmUp" + str(scenario[WARM_UP_TIME]))
    if iterations > 0:
        outputSignature.append("-it-" + str(iterations))
    return ''.join(outputSignature)


def getScenarioSignature(scenarioIndex, iterations = 0):
    ''' Compiles the signature of the given scenario. '''
    return getSignature(scenarios[scenarioIndex], iterations)


def getLogFile(scenario, iteration, fromMode = None, toMode = None):
    if(scenario[UMS]):
        return os.path.join(LOGS_DIR,
                            getSignature(scenario),
                            fromMode + "-"  + toMode + "_" + str(iteration))
        
    return os.path.join(LOGS_DIR,
                        getSignature(scenario),
                        'log_' + str(iteration))
    
    
def getUMSLogFile(scenario, iteration, fromMode = None, toMode = None):
    if(scenario[UMS]):
        return os.path.join(LOGS_DIR,
                            getSignature(scenario),
                            UMS_LOGS,
                            fromMode + "-"  + toMode,
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
    