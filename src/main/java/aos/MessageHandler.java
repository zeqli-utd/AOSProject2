package aos;

import java.io.IOException;

public interface MessageHandler {
	public void handleMessage(Message m, int srcId, Tag tag);
	public Message receiveMessage(int fromId) throws IOException;
}
