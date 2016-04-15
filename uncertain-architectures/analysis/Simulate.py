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


def simulate(scenarioIndex, botCnt, iterations):
    scenario = scenarios[scenarioIndex]
          
    print('Spawning simulation processes...')
    # invoke number of iterations with the same configuration
    for i in range(1,iterations+1):
        if (len(simulated) >= CORES) :
            finalizeOldestSimulation()
        
        # Prepare parameters
        params = []
        params.append(os.path.join(LOGS_DIR,
                                   getSignature(scenario, iterations),
                                   'log_' + str(i)))
        params.append(str(SIMULATION_DURATION))
        params.append(str(botCnt))
        
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
        cmd = ['java', '-jar', '../target/uncertain-architectures-0.0.1-SNAPSHOT-jar-with-dependencies.jar']
        
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
    print("\tpython Simulate.py scenario botCnt iterations")
    print("\nArguments:")
    print("\tscenario - index of the required scenario")
    print("\tbotCnt - number of robots to simulate")
    print("\titerations - number of simulations to perform (optional)")
    print("\nDescription:")
    print("\tDefault number of simulations to perform is 1."
          "\n\tThe available scenarios to simulate can be found by running:"
          "\n\t\tpython Scenarios.py")


def extractScenarioArg(args):
    # Check argument count (1st argument is this script name)
    if len(args) != 4:
        raise ArgError("Invalid arguments")
    try:
        scenario = int(args[1])
        if len(scenarios) <= scenario:
            raise ArgError("Invalid arguments")
        if(scenario < 0):
            raise ArgError("Invalid arguments")
        return scenario
    except ValueError:
        raise ArgError(ValueError)
    
    
def extractBotCntArg(args):
    try:
        cnt = int(args[2])
        if cnt < 1:
            raise ArgError("Invalid arguments")
        return cnt
    except ValueError:
        raise ArgError(ValueError)


def extractIterationsArg(args):
    try:
        cnt = int(args[3])
        if(cnt < 1):
            raise ArgError("Invalid arguments")
        return cnt
    except ValueError:
        raise ArgError(ValueError)


if __name__ == '__main__':
    print("Creating jar with dependencies...")
    shellRequired = True if sys.platform == 'win32' else False
    call(['mvn', '-f..', 'package'], shell=shellRequired)
    print("jar prepared.")
    
    try:
        scenario = extractScenarioArg(sys.argv)
        botCnt = extractBotCntArg(sys.argv)
        iterations = extractIterationsArg(sys.argv)
        
        print("Simulating scenario {} with signature {} {}-times."
              .format(scenario, getScenarioSignature(scenario), iterations))
        
        start = time.time()
        simulate(scenario, botCnt, iterations)
        end = time.time()
        
        print("All simulations lasted for {:.2f} mins".format((end-start)/60))
        print("Results placed to {}"
              .format(getScenarioSignature(scenario, iterations)))
    except ArgError:
        printHelp()
