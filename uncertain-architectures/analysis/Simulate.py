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
        if (len(simulated) >= CORES) :
            finalizeOldestSimulation()
        
        # Prepare parameters
        params = []
        params.append(os.path.join(LOGS_DIR,
                                   getSignature(scenario),
                                   'log_' + str(i)))
        params.append(str(scenario[WARM_UP_TIME]))
        params.append(str(scenario[DURATION]))
        params.append(str(scenario[ROBOT_CNT]))
        params.append(str(scenario[DOCK_CNT]))
        
        if scenario[DDF]:
            params.append("true")
            if DDF_TIME in scenario:
                params.append(str(scenario[DDF_TIME]))
            else:    
                params.append(str(DDF_DEFAULT_TIME))
            params.append("true" if (scenario[CS]) else "false")
        else:
            params.append("false")
        if scenario[DF]:
            params.append("true")
            if DF_TIME in scenario:
                params.append(str(scenario[DF_TIME]))
            else:    
                params.append(str(DF_DEFAULT_TIME))
            params.append("true" if (scenario[FCI]) else "false")
        else:
            params.append("false")
        if scenario[UMS]:
            params.append("true")
            params.append(str(scenario[PROBABILITY]))
            params.append(str(scenario[PROBABILITY_STEP]))
            params.append(str(scenario[UMS_START]))
            params.append(str(scenario[UMS_END]))
        else:
            params.append("false")
            
            
        # Compose invocation command
        #mvn = 'mvn.cmd' if sys.platform == 'win32' else 'mvn'
        #cmd = [mvn, 'exec:java', '-f..','-Dexec.args=' + ' '.join(params)]
        cmd = ['java', '-Xmx4096m', '-jar', '../target/uncertain-architectures-0.0.1-SNAPSHOT-jar-with-dependencies.jar']
        cmd.extend(params)
        
        print(cmd)
        print("Iteration {}".format(i))
        
        simulation = Popen(cmd)
        simulated.append(simulation)
    
    # finalize the rest
    while len(simulated) > 0:
        finalizeOldestSimulation()
        
    print("Simulation processes finished.")
   

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
