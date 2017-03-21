package aos;

import java.io.Serializable;

import clock.VectorClock;

/**
 * 
 * @author Zeqing Li, zxl165030, The University of Texas at Dallas
 *
 */
public class Message implements Serializable{
    
    private static final long serialVersionUID = 1L;
    
    private int srcId;
    private int dstId;
    private String content;
    
    private Tag tag;
    private VectorClock vector = null;
    private int[] mapState = null;
    
    /**
     * Constructor for application message
     * 
     * @param srcId
     * @param dst
     * @param content
     */
    public Message(int srcId, int dst, String content){
        this.srcId = srcId;
        this.dstId = dst;
        this.tag = Tag.DEFAULT;
        this.content = content;
    }
    
    /**
     * Constructor for special message, for example, marker, vector, handshake etc
     * @param src
     * @param dst
     * @param tag
     * @param content
     */
    public Message(int src, int dst, Tag tag, String content){
        this(src, dst, content);
        setTag(tag);
    }
    
    public Message(int src, int dst, Tag tag, String content, VectorClock vector){
        this(src, dst, content);
        setTag(tag);
        setVectorClock(vector);
    }
    
    
    public int getSrcId() {
        return srcId;
    }

    public void setSrcId(int srcId) {
        this.srcId = srcId;
    }
    

    public int getDstId() {
        return dstId;
    }

    public void setDstId(int dstId) {
        this.dstId = dstId;
    }
    

    public Tag getTag() {
        return tag;
    }

    public void setTag(Tag tag) {
        this.tag = tag;
    }
    
    public VectorClock getVectorClock(){
        return this.vector;
    }
    
    public void setVectorClock(VectorClock vector) {
        this.vector = vector;
    }
    
    
    public int[] getMapState() {
        return mapState;
    }

    public void setMapState(int[] mapState) {
        this.mapState = mapState;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean containsVectorClock(){
        return (vector != null);
    }

    @Override 
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("[%s] SOURCE = %d DST = %d CONTENT = \"%s\" ", 
                tag, this.srcId, this.dstId, this.content));
        if (vector != null){
            sb.append(String.format("VECTOR %s", vector.toString()));
        }
        return sb.toString();
    }


}