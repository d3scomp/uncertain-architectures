'''
Created on Dec 8, 2015

This script extracts and computes the dirtiness duration times from logs
produced by simulations.

@author: Ilias
@author: Dominik Skoda
'''

import os
import sys
import shutil
import re
import array
import numpy as np
import matplotlib.pyplot as plt

def analyzeLog():
    
    logDir = "../logs"
    logFile = os.path.join(logDir, "console.log")
    utilExp = re.compile("Non-deterministic mode switching energy for the non-deterministic level [0-9]*,?[0-9]* at [0-9]* is ([0-9]*,?[0-9]*)")
    durSumExp  = re.compile("Duration Sum: ([0-9]*,?[0-9]*)")
    durCntExp = re.compile("Duration Cnt: ([0-9]*,?[0-9]*)")
    
    utility = array.array("d")
    durSum = array.array("d")
    durCnt = array.array("d")
    
    
    with open(logFile) as f:
        for line in f:
            utilRes = utilExp.search(line)
            if(utilRes != None):
                utility.append(float(utilRes.group(1).replace(',', '.')))
                
            durSumRes = durSumExp.search(line)
            if(durSumRes != None):
                durSum.append(float(durSumRes.group(1).replace(',', '.')))
                
            durCntRes = durCntExp.search(line)
            if(durCntRes != None):
                durCnt.append(float(durCntRes.group(1).replace(',', '.')))
            
    print("util min: {:5f}".format(min(utility)))
    print("util max: {:5f}".format(max(utility)))
    print("util med: {:5f}".format(np.median(utility)))
    print("util avg: {:5f}".format(np.mean(utility)))
    
    print("dSum min: {:5f}".format(min(durSum)))
    print("dSum max: {:5f}".format(max(durSum)))
    print("dSum med: {:5f}".format(np.median(durSum)))
    print("dSum avg: {:5f}".format(np.mean(durSum)))
    
    print("dCnt min: {:5f}".format(min(durCnt)))
    print("dCnt max: {:5f}".format(max(durCnt)))
    print("dCnt med: {:5f}".format(np.median(durCnt)))
    print("dCnt avg: {:5f}".format(np.mean(durCnt)))
    
    print("Ploting...")
    plt.boxplot(utility)
    plt.savefig("{}.png".format(os.path.join(logDir, "utility")))


if __name__ == '__main__':
    print("Started.")
    analyzeLog()
    print("Finished")
    
