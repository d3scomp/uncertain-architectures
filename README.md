# Architecture Homeostatic Mechanisms

This repository contains the demo accompanying the submission of our paper "Architecture Homeostasis in Software-Intensive Cyber-Physical Systems" to ECSA'16.

## Usage Guide
### Compilation 
You first need to checkout the following Github projects:
* [meta-adaptation-manager](https://github.com/d3scomp/meta-adaptation-manager) - the library with meta-adaptations.
* [uncertain-architectures](https://github.com/d3scomp/uncertain-architectures.git) (the project featured here), and switch to the "master" branch 
* [JDEECo](https://github.com/d3scomp/JDEECo.git), and switch to the "uncertain-architectures" branch 
* [JDEECoVisualizer](https://github.com/d3scomp/JDEECoVisualizer.git), and switch to the "uncertain-architectures" branch 

Import the following Eclipse projects to a running Eclipse instance (tested with MARS.1):
* From the "meta-adaptation-manager" Github project
  * "meta-adaptation-manager"
* From the "uncertain-architectures" Github project
  * "uncertain-architectures"
* From "JDEECo" Github project
  * "cz.cuni.mff.d3s.jdeeco.core"
  * "cz.cuni.mff.d3s.jdeeco.adaptation"
  * "cz.cuni.mff.d3s.jdeeco.modes"
  * "cz.cuni.mff.d3s.jdeeco.network"
* From "JDEECoVisualizer" Github project
  * "JDEECoVisualizer"

Run maven update on all the projects (you need to install the [m2e plugin](https://marketplace.eclipse.org/content/maven-integration-eclipse-luna-and-newer) to use maven within Eclipse).

### Running the demo
Locate the files Configuration and Run inside the "cz.cuni.mff.d3s.jdeeco.ua.demo" package of the "uncertain-architectures" Eclipse project.
Configuration contains the parameters of the simulation and Run contains the main() of the demo.

### Visualization
To visualize a completed run, locate the "cz.filipekt.jdcv.Visualizer" class in the "JDEECoVisualizer" Eclipse project and run it. Click on Scenes->Import Scene, and on "Specify Configuration File" point to the "config.txt" generated in your file system at the "logs/runtime" folder of the "uncertain-architectures" Eclipse project. Click "Load!" and then "OK". You should be able now to see the robots moving around, cleaning, and charging. You can pause/resume the visualization using the buttons on the bottom of the window.

### Batch invocation and analysis of results
To ease the process of launching simulations with different settings and analyzing their results we have devised a set of Python scripts (version 3.5).
They are placed in the analysis folder of the "uncertain-architectures" Eclipse project.
The "Configuration" script contain the overall config for the simulation run, such as number of processor cores to use etc.
The "Scenarios" script contains the definition of available scenarios. You can see the list by running the script without parameters.
The "Simulate" script serves as a starting point. Run the script with the number of the selected scenario to simulate it. Once all simulation runs are finished, the "Analyze" script can be used to extract the values to be plotted, run the script with the number of the desired scenario. The final results are boxplots depicting the 90th percentile of the "cleaning duration" (time between a tile gets dirty until it gets clean) at each run. Create the plot by running "Plot" script with the scenario numbers passed as arguments (don't forget to scan the simulation results with the "Analyze" script first).
