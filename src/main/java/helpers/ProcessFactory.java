package helpers;

import aos.Process;
import aos.MAP;
import aos.SpanTree;
import snapshot.Camera;

public interface ProcessFactory {
    public abstract Process createProcess();
    public abstract SpanTree createSpanTree();
    public abstract MAP createMAP();
    public abstract Camera createCamera();
}
