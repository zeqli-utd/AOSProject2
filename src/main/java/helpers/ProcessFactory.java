package helpers;

import aos.MAP;
import aos.Process;
import aos.SpanTree;
import snapshot.RecvCamera;

public interface ProcessFactory {
    public Process createProcess();
    public SpanTree createSpanTree();
    public MAP createMAP();
    public RecvCamera createCamera();
}
