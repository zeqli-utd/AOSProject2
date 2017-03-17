package aos;

import java.io.IOException;

/**
 * Message handling interface
 * @author zeqing
 *
 */
public interface MessageHandler {
    void handleMessage(Message m, int srcId, Tag tag) throws IOException;
    Message receiveMessage(int fromId) throws IOException;
}
