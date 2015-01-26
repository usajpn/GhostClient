#!/usr/bin/python

import glob, sys
import commands, os
import time

#size = ['small', 'medium']
#server_num = 3
#
#for s in size:
#expName = s + "_" + str(server_num) + 'servers_16GB_Jan23'
#cmd = 'pssh -h ccx -i "cd /home/t11125su/CNSiMac/ghostclient && /usr/bin/python mkdiror.py ' + expName + '"'
#commands.getoutput(cmd)

# 10 times
for j in range(1, 11):
	cmd = 'pssh -h Gateway -i "screen -dmSL sbt /home/usa/sbt_runner.sh"'
	commands.getoutput(cmd)
	
	time.sleep(12)

	#print hostfile + '(' + str(j) + ')...'
	cmd3 = 'java -jar ghostclient.jar 1 1 medium'
	print commands.getoutput(cmd3)


	
