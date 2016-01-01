'''
Created on Dec 31, 2015

@author: Ilias
'''
import os
from subprocess import *

root = os.path.dirname(os.path.realpath(__file__))
classpath = os.path.join(root, '..' , 'dist' ,'*' + os.pathsep + root, '..' + os.pathsep + '.')

print('Spawning simulation processes...')
# invoke 10 iterations of with the same configuration
for i in range(1,11):
    cmd = ['java', 
        '-classpath', classpath,
        'cz.cuni.mff.d3s.jdeeco.ua.demo.Run',
        'logs/log_' + str(i)]
    simulation = Popen(cmd)
print('Spawning done.')


