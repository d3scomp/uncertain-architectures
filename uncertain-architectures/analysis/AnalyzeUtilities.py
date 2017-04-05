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
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
import matplotlib.text as mpltext
from matplotlib.font_manager import FontProperties

from Configuration import *
from Scenarios import *



class StringLabel(object):
    def __init__(self, text, color):
        self.my_text = text
        self.my_color = color


class StringLabelHandler(object):
    def legend_artist(self, legend, orig_handle, fontsize, handlebox):
        x0, y0 = handlebox.xdescent, handlebox.ydescent
        width, height = handlebox.width, handlebox.height
        patch = mpltext.Text(x = 0, y = 0, text = orig_handle.my_text,
                             color = orig_handle.my_color,
                             verticalalignment = u'baseline',
                             horizontalalignment = u'left',
                             multialignment = None, fontproperties = None,
                             rotation = 0, linespacing = None, rotation_mode = None)
        handlebox.add_artist(patch)
        return patch
    

def analyzeLog(signature):
    
    logsDir = os.path.join(LOGS_DIR, signature, UMS_LOGS)
    utilities = {}
    
    for dirname, dirnames, filenames in os.walk(logsDir):
        # print path to all filenames.
        for filename in filenames:
            print("Processing: {}".format(os.path.join(dirname, filename)))
            transition = os.path.basename(dirname)
            if not utilities.__contains__(transition):
                utilities[transition] = []
            lineCnt = 0
            fileUtilities= []
            with open(os.path.join(dirname, filename)) as f:
                for line in f:
                    fileUtilities.append(int(line))
                    lineCnt = lineCnt + 1
            print("Found {} records.".format(lineCnt))
            utilities[transition].append(np.percentile(fileUtilities, PERCENTILE))
    
    return utilities


def plot(utilities):
    
    print("Ploting...")
    labels = []
    values = []
    signatures = []
    i = 1
    for k in utilities.keys():
        signatures.append(k)
        labels.append(StringLabel(str(i), "black"))
        i = i + 1
        values.append(utilities[k])
    
    fig = plt.figure()
    bp = plt.subplot()    
    bp.boxplot(values)
    box = bp.get_position()
    bp.set_position([box.x0, box.y0, box.width*0.5, box.height])
    fontP = FontProperties()
    fontP.set_size('small')
    bp.legend(labels, signatures, handler_map = {StringLabel:StringLabelHandler()}, loc='center left', bbox_to_anchor=(1, 0.5), prop = fontP)
    fig.savefig("{}.png".format(os.path.join(LOGS_DIR, signature, "utilities")))
    print("Done.")

    

if __name__ == '__main__':
    try:
        if len(sys.argv) < 2:
            raise ArgError("The script expects one number as a parameter.")
        
        sIndex = int(sys.argv[1])
        baselineUtilities = None;
        
        # Add baseline if available
        if sIndex != 0:
            baselineScenario = scenarios[0]
            logsDir = os.path.join(LOGS_DIR, getSignature(baselineScenario))
            if os.path.isdir(logsDir):
                baselineSignature = getSignature(baselineScenario)
                baselineUtilities = analyzeLog(baselineSignature)
        
        scenario = scenarios[sIndex]
        logsDir = os.path.join(LOGS_DIR, getSignature(scenario))
        if not os.path.isdir(logsDir):
            raise Exception("Logs from scenario {} are missing.".format(scenarios.index(scenario)))
    
        print("Started.")
        signature = getSignature(scenario)
        utilities = analyzeLog(signature)
        if baselineUtilities != None:
            for key in baselineUtilities:
                utilities[key] = baselineUtilities[key]
        plot(utilities)
        print("Finished.")
    except ArgError as e:
        print(e.__str__())
        print("usage: python AnalyzeUtilities.py <scenario>")
    
