#!/usr/bin/python
import subprocess, os

top_dir = '5servers_opt'

#for queen_num in range(10, 15):
queen_num = 12
#for client_num in range(1, 11):
client_num = 3
d = top_dir + '/' + str(queen_num) + 'queens/' + str(client_num*10) + 'clients/'

if not os.path.exists(d):
	os.makedirs(d)

for i in range(1, 11):
	print 'executing ' + str(client_num*10) + 'clients(' + str(i) + ')...'
	cmd = 'java -jar ghostclient.jar ' + str(queen_num) + ' ' + str(client_num*10) + ' > ' + d + str(i) + '.dat'
	subprocess.call(cmd, shell=True)

