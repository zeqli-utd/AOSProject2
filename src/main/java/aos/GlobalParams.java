/**
 * 
 */
package aos;

/**
 * @author zeqing
 *
 */
public enum GlobalParams {
    NUM_NODES,          // "Number of Nodes";
    MIN_PER_ACTIVE,     // "Minimum number of active node at each round";
    MAX_PER_ACTIVE,         // "Maximum number of active node at each round";
    MIN_SEND_DELAY,         // "Minimum number of send interval";
    SNAP_SHOT_DELAY,     // "Snapshot interval";
    MAX_NUMBER,         // "Maximum messages a node can send in its life cycle";
    LOCAL_STATE;		// "Local state: active or passive";
}
