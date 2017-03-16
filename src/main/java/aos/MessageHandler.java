package aos;

import java.io.IOException;

/**
 * Message handling interface
 * @author zeqing
 *
 */
public interface MessageHandler {
    public void handleMessage(Message m, int srcId, Tag tag) throws IOException;
    public Message receiveMessage(int fromId) throws IOException;
}
