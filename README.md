# uncertain-architectures
This repository contains demo of uncertain architectures.

## Tutorial
To run the simulation you need to checkout the [DEECo](https://github.com/d3scomp/JDEECo.git) first, and switch to the branch uncertain-architectures. And checkout [jDEECo Visualizer](https://github.com/d3scomp/JDEECoVisualizer).

Import into Eclipse DEECo projects (jdeeco, adaptation, core, network), visualizer and uncertain-architectures projects.
After importing you may need to run maven update on all the projects.

In the uncertain-architectures project in the cz.cuni.mff.d3s.jdeeco.ua.demo package there are the classes Configuration and Run.
Configuration contains the parameters of the simulation and Run contains the main().

To ease the process of launching simulations with different setting we have devised a set of scripts that are placed in the analysis folder. To use these scripts you will need python version 3 as a Eclipse plugin. In the OverallAnalysis you can uncomment a scenario you want to run and set the number of simulations i and run the script. After the simulations are done an analysis will proceed automatically. The result of the analysis is a plot of times that it takes to clean a dirt after it appears.
