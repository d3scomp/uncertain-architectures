'''
Created on Jan 8, 2016

@author: Ilias
'''
from Simulations import *

i = 1

start = time.time()

################################################
################ Role removal ##################
################################################

simulateScenario(
    probabilityOfExtraTransition = 0,
    correlation = False,
    roleRemoval = False,
    dirtDetectionFailure = False, 
    dockFailure = False,
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

################################################
################ Probabilities #################
################################################

# simulateScenario(
#     probabilityOfExtraTransition = 0.01,
#     correlation = False,
#     roleRemoval = True,
#     dirtDetectionFailure = False,
#     dockFailure = True,
#     iterations = i)
#     
# simulateScenario(
#     probabilityOfExtraTransition = 0.005,
#     correlation = False,
#     roleRemoval = True,
#     dirtDetectionFailure = False,
#     dockFailure = True,
#     iterations = i)
#   
# simulateScenario(
#     probabilityOfExtraTransition = 0.001,
#     correlation = False,
#     roleRemoval = True,
#     dirtDetectionFailure = False,
#     dockFailure = True,
#     iterations = i)
#   
# simulateScenario(
#     probabilityOfExtraTransition = 0.0005,
#     correlation = False,
#     roleRemoval = True,
#     dirtDetectionFailure = False,
#     dockFailure = True,
#     iterations = i)
  
# simulateScenario(
#     probabilityOfExtraTransition = 0.0001,
#     correlation = False,
#     roleRemoval = True,
#     dirtDetectionFailure = False,
#     dockFailure = True,
#     iterations = i)

end = time.time()
print("All simulations lasted for %.2f mins" % ((end-start)/60))
