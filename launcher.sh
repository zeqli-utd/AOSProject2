etid=jxl167130

# Root directory of your project
PROJDIR=/home/012/j/jx/jxl167130/Project

# Directory where the config file is located on your local system
CONFIGLOCAL=$HOME/launch/config.txt

# Directory your java classes are in
BINDIR=/home/012/j/jx/jxl167130/Project/bin

# Your main project class
PROG=Proj2

n=0

cat $CONFIGLOCAL | sed -e "s/#.*//" | sed -e "/^\s*$/d" |
(
    read i
    echo $i
    Node_num=$( echo $i | awk '{print $1}')
    minPerActive=$( echo $i | awk '{print $2}')
    maxPerActive=$( echo $i | awk '{print $3}')
    minSendDelay=$( echo $i | awk '{print $4}')
    snapshotDelay=$( echo $i | awk '{print $5}')
    maxNumber=$(echo $i | awk '{print $6}')
    echo $Node_num
    echo $minPerActive
    while [ $n -lt $Node_num ]
    do
        read line
        n=$( echo $line | awk '{ print $1 }' )
        host=$( echo $line | awk '{ print $2 }' )

        gnome-terminal -e "ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no $netid@$host java -cp $BINDIR $PROG $n  $Node_num $minPerActive $maxPerActive $minSendDelay $snapshotDelay $maxNumber $CONFIGLOCA; $SHELL" &

        n=$(( n + 1 ))
    done
)