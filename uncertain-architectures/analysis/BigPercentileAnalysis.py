'''
Created on Dec 31, 2015

@author: Ilias
'''
import os
import matplotlib.pyplot as plt
from os import listdir
from os.path import isfile, join
import Simulations

class BoxPlot:
    
    def __init__(self, signature, data):
        self.signature = signature
        self.data = data
        
    def getData(self):
        return self.data
    
    def getSignature(self):
        return self.signature
    
def plot():
    
    os.makedirs(Simulations.figures_dir, exist_ok=True)

    results_file_names = []
    
    for cvs_file_name in [f for f in listdir(Simulations.cvs_dir) if isfile(join(Simulations.cvs_dir, f))]:
        results_file_names.append(cvs_file_name)
    
    boxplots = []
    
    for cvs_file_name in results_file_names : 
        
        cvs_file_full_path = os.path.join(Simulations.cvs_dir, cvs_file_name)
        resultsFile = open(cvs_file_full_path, "r")
        single_scenario_big_percentiles = []
         
        for line in resultsFile.readlines():
            if line == '\n' : 
                break;
            bigPercentile = float(line)
            single_scenario_big_percentiles.append(bigPercentile)
            
        plt.figure()
        plt.boxplot(single_scenario_big_percentiles)
        simulation_signature = cvs_file_name.split('.cvs')[0]
        plt.ylabel('NinetyPercentiles of ' + simulation_signature)
        boxplot_file_path = os.path.join(Simulations.figures_dir, simulation_signature+'.pdf')
        plt.savefig(boxplot_file_path)
        print("Generated boxplot " + boxplot_file_path)
        
        boxplots.append(BoxPlot(simulation_signature, single_scenario_big_percentiles))
    
    plt.figure()
    plt.boxplot([b.getData() for b in boxplots], labels=[b.getSignature() for b in boxplots])
    boxplot_file_path = os.path.join(Simulations.figures_dir, 'all.pdf')
    plt.savefig(boxplot_file_path)
    print("Generated boxplot " + boxplot_file_path)
    
if __name__ == '__main__':   
    
    plot()
