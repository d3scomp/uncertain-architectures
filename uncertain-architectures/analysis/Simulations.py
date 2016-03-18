'''
Created on Dec 31, 2015

@author: Ilias
'''
import time
from DirtinessAnalysis import *
from BigPercentileAnalysis import *
from subprocess import *

results_dir = os.path.join('..','results')
logs_dir = os.path.join(results_dir,'logs')
csv_dir = os.path.join(results_dir,'csv')
figures_dir = os.path.join(results_dir,'figures')

loggingPropertiesPath = "src\logging.properties"

cores = 2

simulated = []

def finalizeOldestSimulation():
    simulation = simulated[0]
    simulation.wait()
    simulated.pop(0)
    
def simulate(correlation = False, 
             roleRemoval = False, 
             dirtDetectionFailure = False, 
             dockFailure = False,
             unspecifiedModeSwitching = False,
             modeSwitchStartProbability = 0.0001,
             iterations = 10,
             simulation_signature = "sample"):
    root = os.path.dirname(os.path.realpath(__file__))
    classpath = os.path.join(root, '..' , 'dist' ,'*' + os.pathsep + root, '..' + os.pathsep + '.')
      
    print('Spawning simulation processes...')
    # invoke 10 iterations with the same configuration
    for i in range(1,iterations+1):
        
        if (len(simulated) >= cores) :
            finalizeOldestSimulation()
 
        # Signature of the main function:
        # Run log_dir
        #    DIRT_DETECTION_FAILURE_ON [CORRELATION_ON]
        #    DOCK_FAILURE_ON [ROLE_REMOVAL_ON]
        #    NON_DETERMINISM_ON [NON_DET_INIT_PROBABILITY]
        
        # Prepare parameters
        params = []
        params.append(os.path.join(logs_dir,simulation_signature, 'log_' + str(i)))
        if dirtDetectionFailure:
            params.append("true")
            params.append("true" if (correlation) else "false")
        else:
            params.append("false")
        if dockFailure:
            params.append("true")
            params.append("true" if (roleRemoval) else "false")
        else:
            params.append("false")
        if unspecifiedModeSwitching:
            params.append("true")
            params.append(str(modeSwitchStartProbability))
        else:
            params.append("false")
        cmd = ['mvn.cmd', 'exec:java', '-f..','-Dexec.args=' + ' '.join(params)]
        print(cmd)
        simulation = Popen(cmd)     
        simulated.append(simulation)
    
    # finalize the rest    
    while len(simulated) > 0:
        finalizeOldestSimulation()
        
    print("Simulation processes finished.")
    
def getSimulationSignature(correlation,
                           roleRemoval,
                           dirtDetectionFailure,
                           dockFailure,
                           unspecifiedModeSwitching,
                           modeSwitchStartProbability,
                           iterations):
    outputFileName = []
    outputFileName.append("DDF-" if (dirtDetectionFailure) else "!DDF-")
    outputFileName.append("DF-" if (dockFailure) else "!DF-") 
    outputFileName.append(("UMS" + str(modeSwitchStartProbability) + "-") if (unspecifiedModeSwitching) else "!UMS-") 
    outputFileName.append("C-" if (correlation) else "!C-")
    outputFileName.append("RR-" if (roleRemoval) else "!RR-")
    outputFileName.append("it" + str(iterations))
    return ''.join(outputFileName)

def simulateScenario(correlation,
                     roleRemoval,
                     dirtDetectionFailure,
                     dockFailure,
                     unspecifiedModeSwitching,
                     modeSwitchStartProbability,
                     iterations):
    
    start = time.time()
     
    simulation_signature = getSimulationSignature(correlation, roleRemoval, dirtDetectionFailure,
                                                  dockFailure, unspecifiedModeSwitching,
                                                  modeSwitchStartProbability, iterations)
    
    simulate(correlation, roleRemoval, dirtDetectionFailure, dockFailure,
             unspecifiedModeSwitching, modeSwitchStartProbability, iterations, simulation_signature)
    analyze_signature(simulation_signature, cores)
    plot(probabilityOfExtraTransition > 0)
    
    end = time.time()
    
    print("Simulation, analysis, and plotting for scenario with signature %s lasted for %.2f mins" % (simulation_signature, (end-start)/60))
    
if __name__ == '__main__':
    
    probabilityOfExtraTransition = 0
    correlation = False
    roleRemoval = False
    dirtDetectionFailure = False
    dockFailure = False
    iterations = 5
    
    simulateScenario(probabilityOfExtraTransition, correlation, roleRemoval, dirtDetectionFailure, dockFailure, iterations)
    