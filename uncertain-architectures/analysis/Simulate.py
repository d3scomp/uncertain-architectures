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
            for fromMode, toMode in missingTransitions:
                params.append("{}={}".format(LOG_DIR, os.path.join(LOGS_DIR,
                                                                   getSignature(scenario),
                                                                   fromMode + "-"  + toMode + "_"
                                                                   + str(i))))
                params = params + prepareUMSParams(scenario, fromMode, toMode, i);
                spawnSimulation(params, i)
        else:
            params.append("{}={}".format(LOG_DIR, os.path.join(LOGS_DIR,
                                                               getSignature(scenario),
                                                               'log_' + str(i))))
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
        filename = os.path.join(LOGS_DIR,
                                getSignature(scenario),
                                UMS_LOGS, 'log')
        params.append("{}={}".format(NON_DETERMINISM_TRAINING_OUTPUT, filename))
        
    for key, value in scenario.items():
        params.append("{}={}".format(key, value))

    return params


def prepareUMSParams(scenario, fromMode, toMode, iteration):
    params = []
    params.append("{}={}".format(NON_DETERMINISM_TRAIN_FROM, fromMode))
    params.append("{}={}".format(NON_DETERMINISM_TRAIN_TO, toMode))
    filename = os.path.join(LOGS_DIR, getSignature(scenario), UMS_LOGS,
                            fromMode + "-"  + toMode + "/" +
                            'log_' + str(iteration))
    params.append("{}={}".format(NON_DETERMINISM_TRAINING_OUTPUT, filename))
        
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
