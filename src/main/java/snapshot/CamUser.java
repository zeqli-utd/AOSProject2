package snapshot;

import aos.MessageHandler;

//The class Camera can be used by any application that implements the interface CamUser. 
//Thus, the application is required to implement the method localState,
//which records the local state of the application whenever invoked.
public interface CamUser extends MessageHandler {
	void localState();
}
