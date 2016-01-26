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
    
def simulate(probabilityOfExtraTransition = 0, 
             correlation = False, 
             roleRemoval = False, 
             dirtDetectionFailure = False, 
             dockFailure = False,
             iterations = 10,
             simulation_signature = "sample"):
    root = os.path.dirname(os.path.realpath(__file__))
    classpath = os.path.join(root, '..' , 'dist' ,'*' + os.pathsep + root, '..' + os.pathsep + '.')
      
    print('Spawning simulation processes...')
    # invoke 10 iterations with the same configuration
    for i in range(1,iterations+1):
        
        if (len(simulated) >= cores) :
            finalizeOldestSimulation()
 
        cmd = ['java', 
            '-classpath', classpath,
            '-Djava.util.logging.config.file=%s' % (loggingPropertiesPath.replace('\\', '/')),
            'cz.cuni.mff.d3s.jdeeco.ua.demo.Run',
            os.path.join(logs_dir,simulation_signature, 'log_' + str(i)),
            str(probabilityOfExtraTransition),
            "true" if (correlation) else "false",
            "true" if (roleRemoval) else "false",
            "true" if (dirtDetectionFailure) else "false",
            "true" if (dockFailure) else "false"]
        
        simulation = Popen(cmd)     
        simulated.append(simulation)
    
    # finalize the rest    
    while len(simulated) > 0:
        finalizeOldestSimulation()
        
    print("Simulation processes finished.")
    
def getSimulationSignature(probabilityOfExtraTransition, correlation, roleRemoval, dirtDetectionFailure, dockFailure, iterations):
    outputFileName = []
    outputFileName.append("DDF-" if (dirtDetectionFailure) else "!DDF-")
    outputFileName.append("DF-" if (dockFailure) else "!DF-") 
    outputFileName.append("P" + str(probabilityOfExtraTransition) + "-") 
    outputFileName.append("C-" if (correlation) else "!C-")
    outputFileName.append("RR-" if (roleRemoval) else "!RR-")
    outputFileName.append("it" + str(iterations))
    return ''.join(outputFileName)

def simulateScenario(probabilityOfExtraTransition, correlation, roleRemoval, dirtDetectionFailure, dockFailure, iterations):
    
    start = time.time()
     
    simulation_signature = getSimulationSignature(probabilityOfExtraTransition, correlation, roleRemoval, dirtDetectionFailure, dockFailure, iterations)
    
    simulate(probabilityOfExtraTransition, correlation, roleRemoval, dirtDetectionFailure, dockFailure, iterations, simulation_signature)
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
    