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
    values = []
    for i, files in enumerate(analysisResultFiles):
        values.append([])
        for file in files:
            f = open(os.path.join(CSV_DIR, file), "r")
            for line in f:
                values[i].append(float(line) / TIME_DIVISOR)
            f.close()
    
    return values


def extractValuesUMS(analysisResultFiles):
    fromTo = re.compile('.*[\d]_(\w+)-(\w+)_[\d].*')
    values = {}
    for _, files in enumerate(analysisResultFiles):
                         
        for file in files:
            match = fromTo.match(file)
            if(match != None):
                fromT = match.group(1)
                toT = match.group(2)
                t = "{}-{}".format(fromT, toT)
            else:
                raise Exception("Provided scenario {} is not UMS.".format(file))
        
            if(t not in values): #.__contains__(t)):
                values[t] = []
                
            f = open(os.path.join(CSV_DIR, file), "r")
            for line in f:
                values[t].append(float(line) / TIME_DIVISOR)
            f.close()            
    
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


def plotUMS(utilities, baseline):
    if not os.path.exists(FIGURES_DIR):
        os.makedirs(FIGURES_DIR)
    outputFile = os.path.join(FIGURES_DIR, signature)
    legendFile = open("{}.txt".format(outputFile), 'w')
        
    labels = []
    values = []
    signatures = []
    # Prepend baseline
    signatures.append("baseline")
    labels.append(StringLabel(str(1), "black"))
    values.append(baseline)
    
    print("LEGEND:")
    print("{}\t{}".format(1, "baseline"))
    legendFile.write("LEGEND:\n")
    legendFile.write("{} - {}\n".format(1, "baseline"))
    
    i = 2
    for k in utilities.keys():
        signatures.append(k)
        labels.append(StringLabel(str(i), "black"))
        print("{}\t{}".format(i, k))
        legendFile.write("{} - {}\n".format(i, k))
        i = i + 1
        values.append(utilities[k])
        
    legendFile.close()
    
    if len(values) > 20:
        plt.figure(figsize=(10,8))
        sp = plt.subplot()
        bp = sp.boxplot(values, widths=0.8)
    else:
        plt.figure()
        sp = plt.subplot()
        bp = sp.boxplot(values)
    # add the value of the medians to the diagram 
    printMedians(bp)
    
    printYLabel(plt)
    
    # Legend
#    box = sp.get_position()
#    sp.set_position([box.x0, box.y0, box.width*0.5, box.height])
#    fontP = FontProperties()
#    fontP.set_size('small')
#    sp.legend(labels, signatures, handler_map = {StringLabel:StringLabelHandler()}, loc='center left', bbox_to_anchor=(1, 0.5), prop = fontP)
    
    plt.savefig("{}.png".format(outputFile))
    
    
def plotMSP(utilities, scenarioIndices, baseline): # TODO: rewrite this method
    if not os.path.exists(FIGURES_DIR):
        os.makedirs(FIGURES_DIR)
    outputFile = os.path.join(FIGURES_DIR, signature)
    legendFile = open("{}.txt".format(outputFile), 'w')
        
    labels = []
    values = []
    # Prepend baseline
    labels.append(StringLabel(str(1), "black"))
    values.append(baseline)
    
    print("LEGEND:")
    print("{}\t{}".format(1, "baseline"))
    legendFile.write("LEGEND:\n")
    legendFile.write("{} - {}\n".format(1, "baseline"))
    
    i = 2
    for k in scenarioIndices.keys():
        labels.append(StringLabel(str(i), "black"))
        print("{}\t{}".format(i, getMSPProperty(scenarios[k])))
        legendFile.write("{} - {}\n".format(i, getMSPProperty(scenarios[k])))
        values.append(utilities[i-2])
        i = i + 1
        
    legendFile.close()
    
    plt.figure()
    sp = plt.subplot()    
    bp = sp.boxplot(values)
    # add the value of the medians to the diagram 
    printMedians(bp)
    printYLabel(plt)
    
    plt.savefig("{}.png".format(outputFile))
    

def plot(allValues, scenarioIndices):
    if not os.path.exists(FIGURES_DIR):
        os.makedirs(FIGURES_DIR)
    
    bp = plt.boxplot(allValues)
        
    # add the value of the medians to the diagram 
    printMedians(bp)
    
    plt.xlabel("Scenario number")
    printYLabel(plt)
    
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


def printMedians(bp):
    for line in bp['medians']:
        # get position data for median line
        x,y = line.get_xydata()[1] # top of median line
        # overlay median value
        annotate("{:.0f}".format(y), xy = (x - 0.03, y),
                horizontalalignment = 'right',
                verticalalignment = 'bottom',
                fontsize = 10)
        
def printYLabel(plt):
    if METHOD == Method.PERCENTILE:
        plt.ylabel("{}th percentile of \"time to clean a dirty tile\" [s]".format(PERCENTILE))
    if METHOD == Method.MEAN:
        plt.ylabel("Mean time to clean a dirty tile [s]")
    if METHOD == Method.SAMPLES:
        plt.ylabel("Time to clean a dirty tile [s]")
        
        
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
        
        signature = '-'.join(map(str, scenarioIndices.keys()))
        print("Plotting scenarios {} ...".format(signature))
        
        if prob:
            analysisResultFiles = getProbCsvFiles(scenarioIndices)
            values = extractProbValues(analysisResultFiles)
            plot(values, scenarioIndices)
        else:
            analysisResultFiles = getPhaseCsvFiles(scenarioIndices)
            ums = False
            msp = False
            for si in scenarioIndices.keys():
                scenario = scenarios[si]
                if scenario[UMS]:
                    ums = True;
                if MSP in scenario and scenario[MSP]:
                    msp = True;
            if ums and msp:
                raise Exception("Don't know how to plot UMS and MSP scenarios together")
            if ums:
                print("Plotting UMS with baseline")
                values = extractValuesUMS(analysisResultFiles)
                baseline = extractValues(getPhaseCsvFiles({UMS_BASELINE:False}))
                plotUMS(values, baseline)
            elif msp:
                print("Plotting MSP with baseline")
                values = extractValues(analysisResultFiles)
                baseline = extractValues(getPhaseCsvFiles({UMS_BASELINE:False}))
                plotMSP(values, scenarioIndices, baseline)
            else:
                print("Plotting non UMS")
                values = extractValues(analysisResultFiles)
                plot(values, scenarioIndices)
        
        print("Plot placed to {}.png".format(os.path.join(FIGURES_DIR, signature)))
    except (ArgError, ValueError) as e:
        print(e.__str__())
        printHelp()
