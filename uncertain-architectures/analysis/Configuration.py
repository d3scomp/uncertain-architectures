'''
Created on Dec 31, 2015

The configuration for the rest of the scripts in the analysis folder.

@author: Dominik Skoda
'''

import os
from enum import Enum


class ArgError(Exception):
    ''' Custom error to indicate invalid argument. '''
    def __init__(self, value):
        self.value = value
    def __str__(self):
        return repr(self.value)
    
class Method(Enum):
    PERCENTILE = 1
    MEAN = 2
    SAMPLES = 3
    

###############################################################################
# HW RESOURCES
###############################################################################

CORES = 25
''' The number of processor cores to utilize. The number of simulations
    to run in parallel. '''
    
###############################################################################


###############################################################################
# LOCATIONS
###############################################################################

RESULTS_DIR = os.path.join('..','results')
''' The directory where the results from simulation,
    analysis and plots are placed. '''
    
LOGS_DIR = os.path.join(RESULTS_DIR,'logs')
''' The directory where the logs produced by simulations are placed. '''

UMS_LOGS = "UMS_logs"

CSV_DIR = os.path.join(RESULTS_DIR,'csv')
''' The directory where values computed by analysis are placed. '''

FIGURES_DIR = os.path.join(RESULTS_DIR,'figures')
''' The directory where plots are placed. '''

LOGGING_PROPERTIES_PATH = "src\logging.properties"
''' The location of logging properties file. '''

RUNTIME_LOG_FILE = "runtimeData.xml"
''' The name of the file containing runtime logs produced by a simulation. '''

###############################################################################


###############################################################################
# SIMULATION CONFIGURATION
###############################################################################

SIMULATION_WARM_UP = 50000
''' The time, in milliseconds, the simulation needs to warm up. '''

SIMULATION_DURATION = 600000
''' The length of each simulation in milliseconds. '''

LEARNING_PHASE_START = 100000
''' The start of learning phase in milliseconds. '''

LEARNING_PHASE_END = 300000
''' The end of learning phase in milliseconds. '''

DDF_DEFAULT_TIME = 100000
''' The default time of dirt detection failure in milliseconds. '''

DF_DEFAULT_TIME = 50000
''' The default time of dock failure in milliseconds. '''

SIMULATION_ITERATIONS = 100
''' The number of simulation iterations '''

###############################################################################


###############################################################################
# ANALYSIS CONFIGURATION
###############################################################################

METHOD = Method.MEAN

PERCENTILE = 90
''' The percentile to use for analysis. '''

TIME_DIVISOR = 1000
''' A number to be used to divide the times to plot. '''

PLOT_LABELS = False
''' Indicates whether the produced plot should contain signature labels. '''

UMS_BASELINE = 1

PROJECT_VERSION = "1.0.0"

###############################################################################