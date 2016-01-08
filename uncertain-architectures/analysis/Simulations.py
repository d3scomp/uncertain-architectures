'''
Created on Dec 31, 2015

@author: Ilias
'''
import os
from subprocess import *

def simulate():
    root = os.path.dirname(os.path.realpath(__file__))
    classpath = os.path.join(root, '..' , 'dist' ,'*' + os.pathsep + root, '..' + os.pathsep + '.')
    
    logPath = "src\logging.properties"
    
    print('Spawning simulation processes...')
    # invoke 10 iterations with the same configuration
    for i in range(1,11):
        cmd = ['java', 
            '-classpath', classpath,
            '-Djava.util.logging.config.file=%s' % (logPath.replace('\\', '/')),
            'cz.cuni.mff.d3s.jdeeco.ua.demo.Run',
            'logs/log_' + str(i)]
        simulation = Popen(cmd)
        simulation.wait()
        
if __name__ == '__main__': 

    simulate()
    print("Simulations finished.")

