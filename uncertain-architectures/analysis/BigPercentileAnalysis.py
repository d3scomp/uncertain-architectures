'''
Created on Dec 31, 2015

@author: Ilias
'''
import os
import matplotlib.pyplot as plt
from os import listdir
from os.path import isfile, join
from pylab import *
import Simulations

class BoxPlot:
     
    def __init__(self, label, data):
        self.label = label
        self.data = data
        
    def getData(self):
        return self.data
    
    def getLabel(self):
        return self.label
    
def getLabelFromSignature(signature, probabilities_on = True):
    res = []
    parts = signature.split('-')
    if (probabilities_on) :
        res.append(parts[2].split('P')[1])
    else :
        res.append(parts[0])
        res.append(',')
        res.append(parts[1])
        res.append(',')
        res.append(parts[2])
        res.append(',')
        res.append(parts[3])
        res.append(',')
        res.append(parts[4])
        res.append(',')
        res.append(parts[5])
    return ''.join(res)

def plot(probabilities_on = False):
    
    os.makedirs(Simulations.figures_dir, exist_ok=True)

    results_file_names = []
    
    for csv_file_name in [f for f in listdir(Simulations.csv_dir) if isfile(join(Simulations.csv_dir, f))]:
        results_file_names.append(csv_file_name)
    
    boxplots = []
    
    numbering=["1","2","3"]
    i=0
    
    for csv_file_name in results_file_names : 
        
        csv_file_full_path = os.path.join(Simulations.csv_dir, csv_file_name)
        resultsFile = open(csv_file_full_path, "r")
        single_scenario_big_percentiles = []
        
        for line in resultsFile.readlines():
            if line == '\n' : 
                break;
            bigPercentile = float(line)
            single_scenario_big_percentiles.append(bigPercentile / 1000)
            
        plt.figure()
        plt.boxplot(single_scenario_big_percentiles)
        simulation_signature = csv_file_name.split('.csv')[0]
#         plt.ylabel('NinetyPercentiles of ' + simulation_signature)
#         boxplot_file_path = os.path.join(Simulations.figures_dir, simulation_signature+'.pdf')
#         plt.savefig(boxplot_file_path)
#         print("Generated boxplot " + boxplot_file_path)
        
        if (probabilities_on) :
            boxplots.append(BoxPlot(getLabelFromSignature(simulation_signature), single_scenario_big_percentiles))
        else :
            label = numbering[i]
            i = i + 1
            boxplots.append(BoxPlot(label, single_scenario_big_percentiles))
    
    fig = plt.figure()
    ax = fig.add_subplot(111)
    bp_dict = ax.boxplot([b.getData() for b in boxplots], labels=[b.getLabel() for b in boxplots])
    ax.set_ylabel('Time (sec)')
    
    if (probabilities_on) :
        ax.set_xlabel('Probabilities')
        
    # add the value of the medians to the diagram 
    for line in bp_dict['medians']:
        # get position data for median line
        x,y = line.get_xydata()[1] # top of median line
        # overlay median value
        annotate('%.0f' % (y), xy=(x - .07, y),
                horizontalalignment='right', verticalalignment='bottom',
                fontsize=10)
    
    boxplot_file_path = os.path.join(Simulations.figures_dir, 'all.png')
    plt.savefig(boxplot_file_path)
    print("Generated boxplot " + boxplot_file_path)
    
if __name__ == '__main__':   
    
    plot()
