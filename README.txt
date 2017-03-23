Authors:
    Zeqing Li, The University of Texas at Dallas
    Ming Sun, The University of Texas at Dallas
    Jingyi Liu, The University of Texas at Dallas


# Compile Instruction

# Clear /bin directory 
    $> rm -rf bin/*
    
# Compile project to ./bin folder
    $> javac -d bin src/main/java/aos/*.java\
                    src/main/java/clock/*.java \
                    src/main/java/snapshot/*.java \
                    src/main/java/helpers/*.java \
                    src/main/java/socket/*.java
    or simply run compile
    
    This should have no warnings or errors.
    
    

# The default configuration path is ~/launcher/<config-file-name>.txt    
    
# Execute Instruction

    $> ./launch.sh
    
    This tells the program which node it is.

# Cleanup processes
    $> ./cleanup.sh
    
    
# The program will output files in $HOME directory ~
# Caveat! Make sure cleanup or move the output to another places before doing next test.
    
    
# The shell script might contains doc encoding instead of unix, to convert the format run the below command in Vim.    
# Change shell script format
    :update
    :e ++ff=dos
    :setlocal ff=unix
    :w
    
    Reference: http://vim.wikia.com/wiki/Change_end-of-line_format_for_dos-mac-unix

    
# Status Code
    00 - PASSIVE, NON-EMPTY
    01 - PASSIVE, EMPTY
    10 - ACTIVE , NON-EMPTY
    11 - ACTIVE , EMPTY    