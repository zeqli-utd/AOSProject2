package snapshot;

import aos.MessageHandler;

public interface CamUser extends MessageHandler {
	void localState();
}
