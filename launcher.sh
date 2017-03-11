etid=jxl167130

# Root directory of your project
PROJDIR=/home/012/j/jx/jxl167130/Project

# Directory where the config file is located on your local system
CONFIGLOCAL=$HOME/launch/config.txt

# Directory your java classes are in
BINDIR=/home/012/j/jx/jxl167130/Project/bin

# Your main project class
PROG=Proj2

#Path of Config file
CONFIG=/home/012/j/jxl167130/config.txt

n=0

cat $CONFIGLOCAL | sed -e "s/#.*//" | sed -e "/^\s*$/d" |
(
    read i
    echo $i
    Node_num=$( echo $i | awk '{print $1}')
    while [ $n -lt $Node_num ]
    do
        read line
        n=$( echo $line | awk '{ print $1 }' )
        host=$( echo $line | awk '{ print $2 }' )
        port=$( echo $lin4 | awk '{ print $3 }' )

        gnome-terminal -e "ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no $netid@$host java -cp $BINDIR $PROG $n $port $CONFIG; $SHELL" &

        n=$(( n + 1 ))
    done
)
