Author: Zeqing Li, zxl165030, The University of Texas at Dallas


# Compile Instruction
    $> javac -d bin src/main/java/aos/*.java src/main/java/clock/*.java src/main/java/snapshot/*.java 

    
    This should have no warnings or errors.

# Execute Instruction

    $> ./aos.Server <port> <node ID> $HOME/launch/config.txt
    
    This tells the program which node it is.
    
    
# Change shell script format
    :update
    :e ++ff=dos
    :setlocal ff=unix
    :w
    
    Reference: http://vim.wikia.com/wiki/Change_end-of-line_format_for_dos-mac-unix

# Configuration File Format

    config.txt is formatted as:

    <Size of network>

    <node id 0> <host 0> <port 0>
    <node id 0> <host 0> <port 0>
    <node id 0> <host 0> <port 0>
    <node id 0> <host 0> <port 0>
    <node id 0> <host 0> <port 0>

    <node id 0> <neighbor1 neighbor2 ...>
    <node id 0> <neighbor1 neighbor2 ...>
    <node id 0> <neighbor1 neighbor2 ...>
    <node id 0> <neighbor1 neighbor2 ...>
    <node id 0> <neighbor1 neighbor2 ...>
