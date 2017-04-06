'''
Created on Dec 31, 2015

This script takes values computed by Analyze.py script and creates a box plot.

@author: Ilias
@author: Dominik Skoda
'''

import os
import re
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
import matplotlib.text as mpltext
from matplotlib.font_manager import FontProperties
from pylab import *
from Scenarios import *
from Configuration import *


PHASE_ARG = "phase"
PROB_ARG = "prob"


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


def getProbCsvFiles(scenarioIndices):
    csvFiles = []
    for file in os.listdir(CSV_DIR):
        if file.endswith(".csv"):
            csvFiles.append(file)
    
    relevantFiles = []
    for i, si in enumerate(scenarioIndices):
        scenarioResultsFound = False
        for file in csvFiles:
            if file.startswith(getScenarioSignature(si)) and "probabilities" in file:
                print(file)
                if not scenarioResultsFound:
                    scenarioResultsFound = True
                    relevantFiles.append([])
                relevantFiles[i].append(file);
        if not scenarioResultsFound:
            raise ArgError("No files for scenario {} found.".format(si))
    
    return relevantFiles


def getCsvFiles(csvFiles, signature, phase):
    print("Files for {} {}:".format(signature, phase))
    relevantFiles = []
    for file in csvFiles:
        if file.startswith(signature) and phase in file:
            print(file)
            relevantFiles.append(file);
    return relevantFiles
                    
def getPhaseCsvFiles(scenarioIndices):
    csvFiles = []
    for file in os.listdir(CSV_DIR):
        if file.endswith(".csv"):
            csvFiles.append(file)
    
    relevantFiles = []
    i = 0
    for si in scenarioIndices.keys():
        if scenarioIndices[si]:
            p1Files = getCsvFiles(csvFiles, getScenarioSignature(si), "phase1")
        p2Files = getCsvFiles(csvFiles, getScenarioSignature(si), "phase2")
        if len(p2Files) > 0:
            if scenarioIndices[si]:
                relevantFiles.append(p1Files)
                i = i+1
            relevantFiles.append(p2Files)
            i = i+1
        else:
            raise ArgError("No files for scenario {} found.".format(si))
    
    return relevantFiles


def extractValues(analysisResultFiles):
    fromTo = re.compile('.*_(\w+)-([^_]+)_.*')
    values = {}
    for i, files in enumerate(analysisResultFiles):
        for file in files:
            match = fromTo.match(file)
            if(match != None):
                fromT = match.group(1)
                toT = match.group(2)
                t = "{}-{}".format(fromT, toT)
            else:
                t = "baseline"
        
            f = open(os.path.join(CSV_DIR, file), "r")
            line = f.readline()
            f.close()
            if(not values.__contains__(t)):
                values[t] = []
            values[t].append(float(line) / TIME_DIVISOR)
    
    return values


def extractProbValues(analysisResultFiles):
    values = []
    for i, files in enumerate(analysisResultFiles):
        values.append([])
        for file in files:
            f = open(os.path.join(CSV_DIR, file), "r")
            for line in f:
                l = line.split("is")
                if len(l) == 2:
                    p = l[1]
                    values[i].append(float(p)) # TODO: extract probability
            f.close()
    
    return values

def plotUMS(utilities):
    
    labels = []
    values = []
    signatures = []
    i = 1
    for k in utilities.keys():
        signatures.append(k)
        labels.append(StringLabel(str(i), "black"))
        i = i + 1
        values.append(utilities[k])
    
    plt.figure()
    bp = plt.subplot()    
    bp.boxplot(values)
    box = bp.get_position()
    bp.set_position([box.x0, box.y0, box.width*0.5, box.height])
    fontP = FontProperties()
    fontP.set_size('small')
    bp.legend(labels, signatures, handler_map = {StringLabel:StringLabelHandler()}, loc='center left', bbox_to_anchor=(1, 0.5), prop = fontP)
    
    plt.savefig("{}.png".format(os.path.join(FIGURES_DIR, signature)))
    

def plot(allValues, scenarioIndices):
    if not os.path.exists(FIGURES_DIR):
        os.makedirs(FIGURES_DIR)
    
    bp = plt.boxplot(allValues)
        
    # add the value of the medians to the diagram 
    for line in bp['medians']:
        # get position data for median line
        x,y = line.get_xydata()[1] # top of median line
        # overlay median value
        annotate("{:.0f}".format(y), xy = (x - 0.03, y),
                horizontalalignment = 'right',
                verticalalignment = 'bottom',
                fontsize = 10)
    
    plt.xlabel("Scenario number")
    plt.ylabel("{}th percentile of \"time to clean a dirty tile\" [s]".format(PERCENTILE))
    
    # X ticks
    indices = []
    values = []
    i = 1
    j = 1
    for si in scenarioIndices.keys():
        if scenarioIndices[si]:
            indices.append(i)
            indices.append(i+1)
            values.append("{}a".format(j))
            values.append("{}b".format(j))
            i = i+2
            j = j+1
        else:
            indices.append(i)
            values.append("{}".format(j))
            i = i+1
            j = j+1
    plt.xticks(indices, values)
    
    if PLOT_LABELS:
        signatures = []
        labels = []
        i = 1
        for si in scenarioIndices.keys():
            if scenarioIndices[si]:
                signatures.append("{} A".format(getScenarioSignature(si)))
                signatures.append("{} B".format(getScenarioSignature(si)))
                labels.append(StringLabel(str(i), "black"))
                labels.append(StringLabel(str(i+1), "black"))
                i = i+2
            else:
                signatures.append(getScenarioSignature(si))
                labels.append(StringLabel(str(i), "black"))
                i = i+1
        plt.legend(labels, signatures, handler_map = {StringLabel:StringLabelHandler()})
    
    plt.savefig("{}.png".format(os.path.join(FIGURES_DIR, signature)))


def printHelp():
    print("\nUsage:")
    print("\tpython Plot.py scenario1 [scenario2 [phase] [...]] ")
    print("\nArguments:")
    print("\tscenarios - indices of the required scenarios to plot")
    print("\tphase - indicates to split phases of the scenario")
    print("\nDescription:")
    print("\tThe scenarios to plot has to be already analyzed and the csv files "
          "produced by the analyzes has to be available."
          "\n\tThe available scenarios to simulate and analyze can be found by running:"
          "\n\t\tpython Scenarios.py")


def extractArgs(args):
    # Check argument count (1st argument is this script name)
    if len(args) < 2:
        raise ArgError("At least one scenario argument is required")
    
    scenarioIndices = {}
    lastIndex = -1;
    for i in range(1, len(args)):
        if str(args[i]) == PHASE_ARG:
            if lastIndex == -1:
                raise ArgError("{} arg has to follow scenario index.".format(PHASE_ARG))
            scenarioIndices[lastIndex] = True
            lastIndex = -1
            continue
        
        scenarioIndex = int(args[i])
        if len(scenarios) <= scenarioIndex or 0 > scenarioIndex:
            raise ArgError("Scenario index value {} is out of range.".format(scenarioIndex))
        scenarioIndices[scenarioIndex] = False
        lastIndex = scenarioIndex
    
    return scenarioIndices


if __name__ == '__main__': 
    try:
        if PROB_ARG in sys.argv:
            prob = True
            sys.argv.remove(PROB_ARG)
        else:
            prob = False
            
        scenarioIndices = extractArgs(sys.argv)
        # add baseline
        scenarioIndices[UMS_BASELINE] = False;
        
        signature = '-'.join(map(str, scenarioIndices.keys()))
        print("Plotting scenarios {} ...".format(signature))
        
        if prob:
            analysisResultFiles = getProbCsvFiles(scenarioIndices)
            values = extractProbValues(analysisResultFiles)
        else:
            analysisResultFiles = getPhaseCsvFiles(scenarioIndices)
            values = extractValues(analysisResultFiles)
        plotUMS(values)
        
        print("Plot placed to {}.png".format(os.path.join(FIGURES_DIR, signature)))
    except (ArgError, ValueError) as e:
        print(e.__str__())
        printHelp()
