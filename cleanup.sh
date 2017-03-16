#!/bin/bash


# Change this to your netid
netid=zxl165030

#
# Root directory of your project
PROJDIR=/people/cs/s/zxl165030/TestProj

#
# Directory where the config file is located on your local system
CONFIGLOCAL=$HOME/launch/config.txt

n=0

cat $CONFIGLOCAL | sed -e "s/#.*//" | sed -e "/^\s*$/d" |
(
    read i
    i=$( echo $i | awk '{print $1}')
    echo $i
    while [ $n -lt $i ]
    do
        read line
        host=$( echo $line | awk '{ print $2 }' )

        echo $host
        bash -c "ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no $netid@$host killall -u $netid" &
        sleep 1

        n=$(( n + 1 ))
    done

)


echo "Cleanup complete"
