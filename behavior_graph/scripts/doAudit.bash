#!/bin/bash

######
## audit runner 
## based on an initial script by @kyuhlee
######

#### Set up #######################

configFile="/etc/audit/auditd.conf"

echo "checking for audit configuration"
if grep -q "freq = 5500" "$configFile"; then
   echo "Configure file already ok" 
else 
   echo "writing configurations to audit config file"  
   sed -i -e 's/freq = /freq = 5500\n##freq = /g'  "$configFile"
   sed -i -e 's/flush = /flush = INCREMENTAL\n##flush = /g' "$configFile"
   echo "done setting up config"
fi

### Action ###############################################################


echo "Killing auditd..."
sudo auditctl -D
sudo killall -9 auditd

echo "Starting auditd..."
sudo auditd 

AUDITD_PID=$(pidof auditd)

echo $AUDITD_PID 
echo "Cleaning up auditd rules..."
sudo auditctl -D || fail 4

echo "Applying new set of rules to auditd..."

#sudo auditctl -a exit,always -S 62 -S 60 -S 231 -F pid!=$AUDITD_PID

sudo auditctl -a exit,always -F arch=b64 -S 0 -S 19 -S 1 -S 20 -S 44 -S 45 -S 46 -S 47 -S 86 -S 88 -S 56 -S 57 -S 58 -S 59 -S 2 -S 85 -S 257 -S 259 -S 133 -S 32 -S 33 -S 292 -S 49 -S 43 -S 288 -S 42 -S 82 -S 105 -S 113 -S 90 -S 22 -S 203 -S 76 -S 77 -S 40 -S 87 -S 263 -F success=1 -F pid!=$AUDITD_PID

##sudo auditctl -a exit,always -S all -F pid!=$AUDITD_PID

echo "List rules..."
sudo auditctl -l