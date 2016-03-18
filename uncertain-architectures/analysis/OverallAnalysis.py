'''
Created on Jan 8, 2016

@author: Ilias
'''
from Simulations import *



if __name__ == '__main__':

    i = 1
    
    start = time.time()
    
    ################################################
    ################ Role removal ##################
    ################################################
    
    simulateScenario(
        correlation = False,
        roleRemoval = False,
        dirtDetectionFailure = False, 
        dockFailure = False,
        unspecifiedModeSwitching = False,
        modeSwitchStartProbability = 0,
        iterations = i)
       
    # simulateScenario(
    #     probabilityOfExtraTransition = 0,
    #     correlation = False,
    #     roleRemoval = False,
    #     dirtDetectionFailure = False,
    #     dockFailure = True,
    #     iterations = i)
    
    # simulateScenario(
    #     probabilityOfExtraTransition = 0,
    #     correlation = False,
    #     roleRemoval = True,
    #     dirtDetectionFailure = False,
    #     dockFailure = True,
    #     iterations = i)
    
    ################################################
    ################## Correlation #################
    ################################################
    
    # simulateScenario(
    #     probabilityOfExtraTransition = 0,
    #     correlation = False,
    #     roleRemoval = False,
    #     dirtDetectionFailure = True,
    #     dockFailure = False,
    #     iterations = i)
    
    # simulateScenario(
    #     probabilityOfExtraTransition = 0,
    #     correlation = True,
    #     roleRemoval = False,
    #     dirtDetectionFailure = True,
    #     dockFailure = False,
    #     iterations = i)
    
    
    end = time.time()
    print("All simulations lasted for %.2f mins" % ((end-start)/60))