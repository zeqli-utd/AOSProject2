# Change this to your netid
netid=zxl165030

# Root directory of your project
PROJDIR=/home/011/z/zx/zxl165030/TestProj

# Directory where the config file is located on your local system
CONFIGLOCAL=$HOME/launch/config.txt

# Directory your java classes are in
BINDIR=$PROJDIR/bin

# Your main project class
PROG=aos.Server

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
        port=$( echo $line | awk '{ print $3 }' )

        bash -c "ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no $netid@$host java -cp $BINDIR $PROG $port $n $CONFIGLOCAL; $SHELL" &

        n=$(( n + 1 ))
    done
)
