package aos;

import java.io.Serializable;
import java.util.Arrays;

/**
 * 
 * @author Zeqing Li, zxl165030, The University of Texas at Dallas
 *
 */
public class Message implements Serializable{
    


    private static final long serialVersionUID = 1L;
    
    private int srcId;
    private int dstId;
    private Tag tag;
    private int[] v = new int[0];
    private String content;
    
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
        this.tag = Tag.APP;
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

    public int[] getVector() {
        return v;
    }

    public void setVector(int[] v) {
        this.v = v;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override 
    public String toString(){
//        if(tag.equals("vector"))
        return String.format("[%s] SOURCE = %d DST = %d CONTENT = \"%s\" VECTOR = %s", 
                tag, this.srcId, this.dstId, this.content, Arrays.toString(v));
    }


}