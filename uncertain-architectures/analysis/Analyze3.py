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
    
    logDir = ".."
    logFiles = [os.path.join(logDir, "train.txt"), os.path.join(logDir, "train2.txt")]
   
    sum = 0
    cnt = 0
    utility = []
   
    for file in logFiles:  
        lsum = 0
        lcnt = 0 
        with open(file) as f:
            for line in f:
                utility.append(int(line))
                lsum += int(line)
                lcnt += 1
                
        print("lsum {}".format(lsum))
        print("lcnt {}".format(lcnt))
        sum += lsum
        cnt += lcnt
                    
            
    
    print("sum {}".format(sum))
    print("cnt {}".format(cnt))
    print("util {}".format(sum / cnt))
    
    print("Ploting...")
    plt.boxplot(utility)
    plt.savefig("{}.png".format(os.path.join(logDir, "utility")))


if __name__ == '__main__':
    print("Started.")
    analyzeLog()
    print("Finished")
    
