'''
Created on Dec 31, 2015

The configuration for the rest of the scripts in the analysis folder.

@author: Dominik Skoda
'''

import os


RESULTS_DIR = os.path.join('..','results')
''' The directory where the results from simulation,
    analysis and plots are placed. '''
    
LOGS_DIR = os.path.join(RESULTS_DIR,'logs')
''' The directory where the logs produced by simulations are placed. '''

CSV_DIR = os.path.join(RESULTS_DIR,'csv')
''' The directory where values computed by analysis are placed. '''

FIGURES_DIR = os.path.join(RESULTS_DIR,'figures')
''' The directory where plots are placed. '''

DEFAULT_ITERATIONS_CNT = 1
''' The default number of simulations count. '''

loggingPropertiesPath = "src\logging.properties"
''' The location of logging properties file. '''

CORES = 2
''' The number of processor cores to utilize. The number of simulations
    to run in parallel. '''