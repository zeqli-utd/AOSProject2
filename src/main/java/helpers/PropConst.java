package helpers;

public abstract class PropConst {
    public static final String NEIGHBORS = "NEIGHBORS";
    public static final String CONFIG_FILE_NAME = "CONFIG_FILE_NAME";
    public static final String CONFIG_FILE_DIRECTORY = "CONFIG_FILE_DIRECTORY";
    
    
    public static final String NUM_NODES = "NUM_NODES";                 // "Number of Nodes";
    public static final String MIN_PER_ACTIVE = "MIN_PER_ACTIVE";       // "Minimum number of active node at each round";
    public static final String MAX_PER_ACTIVE = "MAX_PER_ACTIVE";       // "Maximum number of active node at each round";
    public static final String MIN_SEND_DELAY = "MIN_SEND_DELAY";       // "Minimum number of send interval";
    public static final String SNAP_SHOT_DELAY = "SNAP_SHOT_DELAY";     // "Snapshot interval";
    public static final String MAX_NUMBER = "MAX_NUMBER";               // "Maximum messages a node can send in its life cycle";
    public static final String LOCAL_STATE = "LOCAL_STATE";             // "Local state: active or passive";
}
