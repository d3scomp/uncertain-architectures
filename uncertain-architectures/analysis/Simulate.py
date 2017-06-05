'''
Created on Dec 31, 2015

This script is to ease the invocation of multiple simulation iterations,
taking advantage of multi-core processors.

Usage:
    python Simulate.py scenario [iterations]

Arguments:
    scenario - index of the required scenario
    iterations - number of simulations to perform (optional)

Description:
    Default number of simulations to perform is 1.
    The available scenarios to simulate can be found by running:
        python Scenarios.py

@see: Configuration.py
@see: Scenarios.py

@author: Ilias
@author: Dominik Skoda
'''

import os
import sys
import time
from subprocess import *
from Scenarios import *
from Configuration import *


ENABLE_SEED = True # Seed usage
seed = 0
seed_step = 1

simulated = []


def finalizeOldestSimulation():
    simulation = simulated[0]
    simulation.wait()
    simulated.pop(0)


def simulate(scenarioIndex):
    scenario = scenarios[scenarioIndex]
          
    print('Spawning simulation processes...')
    
    # invoke number of iterations with the same configuration
    for i in range(1,SIMULATION_ITERATIONS+1):
        params = prepareParameters(scenario, i)
        if scenario[UMS]:    
            if NON_DETERMINISM_TRAINING in scenario and scenario[NON_DETERMINISM_TRAINING]:
                    prepareUMSScenario(scenario, params, i)
            else:
                print("Unsupported scenario!")
        elif scenario[MSP]:
            if MODE_SWITCH_PROPS_TRAINING in scenario and scenario[MODE_SWITCH_PROPS_TRAINING]:
                    prepareMSPScenario(scenario, params, i)
            else:
                print("Unsupported scenario!")
            
        else:
            params.append("{}={}".format(LOG_DIR, getLogFile(scenario, i)))
            spawnSimulation(params, i)
        
    # finalize the rest
    while len(simulated) > 0:
        finalizeOldestSimulation()
        
    print("Simulation processes finished.")
   
   
def spawnSimulation(params, iteration):
    # Compose invocation command
    cmd = ['java', '-Xmx4096m', '-jar', '../target/uncertain-architectures-0.0.1-SNAPSHOT-jar-with-dependencies.jar']
    cmd.extend(params)
    
    # Wait for free core
    if (len(simulated) >= CORES) :
        finalizeOldestSimulation()
    
    print(cmd)
    print("Iteration {}".format(iteration))
    
    simulation = Popen(cmd)
    simulated.append(simulation)
    
    
def prepareParameters(scenario, iteration):
    # Prepare parameters
    params = []
    
    if(ENABLE_SEED):
        global seed
        params.append("{}={}".format(WITH_SEED, True))
        params.append("{}={}".format(SEED, seed))
        seed += seed_step
        
    if not scenario[UMS]:
        params.append("{}={}".format(NON_DETERMINISM_TRAINING_OUTPUT,
                                     getUMSLogFile(scenario, iteration)))
        
    for key, value in scenario.items():        
        # ignore parameters that are used by this script but not by the simulation
        if key in {SCENARIO_NAME,
                   NON_DETERMINISM_TRAINING,
                   NON_DETERMINISM_TRAINING_DEGREE,
                   MODE_SWITCH_PROPS_TRAINING,
                   MODE_SWITCH_PROPS_TRAINING_DEGREE}:
            continue;
        
        params.append("{}={}".format(key, value))

    return params


def prepareUMSScenario(scenario, params, iteration):
    if scenario[NON_DETERMINISM_TRAINING_DEGREE] == 1:
        transitions = missingTransitions
    else:
        transitions = missingTransitionsReduced
    
    runUMSScenario(scenario, transitions, [], [], params, iteration, scenario[NON_DETERMINISM_TRAINING_DEGREE])
    

def runUMSScenario(scenario, transitions, preparedTransitions, simulatedTransitions, params, iteration, degree):
    if degree <= 0:
        for item in simulatedTransitions:
            if set(preparedTransitions).issubset(set(item)):
                return # skip if already done
        params = params + prepareUMSParams(scenario, ";".join(preparedTransitions) + ";", iteration)
        # remember what was done
        simulatedTransitions.append(preparedTransitions)
        spawnSimulation(params, iteration)
    else:
        for fromMode, toMode in transitions:
            sTransition = "{}-{}".format(fromMode, toMode)
            if sTransition not in preparedTransitions:
                nextDegreeTransitions = list(preparedTransitions) # create a copy of the given list
                nextDegreeTransitions.append(sTransition)
                runUMSScenario(scenario, transitions, nextDegreeTransitions, simulatedTransitions, params, iteration, degree-1)
                

def prepareUMSParams(scenario, transitions, iteration):
    params = []
    params.append("{}={}".format(LOG_DIR, getLogFile(scenario, iteration, transitions)))
    params.append("{}={}".format(NON_DETERMINISM_TRAIN_TRANSITIONS, transitions))
    params.append("{}={}".format(NON_DETERMINISM_TRAINING_OUTPUT,
                                 getUMSLogFile(scenario, iteration, transitions)))
        
    return params


def prepareMSPScenario(scenario, params, iteration):
    if scenario[MODE_SWITCH_PROPS_TRAINING_DEGREE] == 1:
        properties = adjustedProperties
    else:
        properties = adjustedPropertiesReduced
    
    runMSPScenario(scenario, properties, [], [], params, iteration, scenario[MODE_SWITCH_PROPS_TRAINING_DEGREE])


def runMSPScenario(scenario, properties, preparedProperties, simulatedProperties, params, iteration, degree):
    if degree <= 0:
        for item in simulatedProperties:
            if set(preparedProperties).issubset(set(item)):
                return # skip if already done
        params = params + prepareMSPParams(scenario, ";".join(preparedProperties) + ";", iteration)
        # remember what was done
        simulatedProperties.append(preparedProperties)
        spawnSimulation(params, iteration)
    else:
        for prop, value in properties:
            sProperty = "{}({})".format(prop, value)
            if sProperty not in preparedProperties:
                nextDegreeeProperties = list(preparedProperties) # create a copy of the given list
                nextDegreeeProperties.append(sProperty)
                runMSPScenario(scenario, properties, nextDegreeeProperties, simulatedProperties, params, iteration, degree-1)


def prepareMSPParams(scenario, properties, iteration):
    params = []
    params.append("{}={}".format(LOG_DIR, getLogFile(scenario, iteration, properties)))
    params.append("{}={}".format(MODE_SWITCH_PROPS_PROPERTIES, properties))
        
    return params


def printHelp():
    print("\nUsage:")
    print("\tpython Simulate.py scenario1 [scenario2 [...]]")
    print("\nArguments:")
    print("\tscenario - index of the required scenario")
    print("\nDescription:")
    print("\tThe available scenarios to simulate can be found by running:"
          "\n\t\tpython Scenarios.py")


def extractScenarioArgs(args):
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
    print("Creating jar with dependencies...")
    shellRequired = True if sys.platform == 'win32' else False
    call(['mvn', '-f..', 'package'], shell=shellRequired)
    print("jar prepared.")
    
    try:
        si = extractScenarioArgs(sys.argv)        
        
        start = time.time()
        for i in si:
            print("Simulating scenario {} with signature {}"
                  .format(i, getScenarioSignature(i)))
            simulate(i)
            print("Results placed to {}".format(getScenarioSignature(i)))
        end = time.time()
        
        print("All simulations lasted for {:.2f} mins".format((end-start)/60))
    except ArgError:
        printHelp()
