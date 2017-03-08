package snapshot;

import aos.MessageHandler;
//It allows any application that uses a camera to invoke the method globalstate, 
//which records a consistent global state of the system

public interface Camera extends MessageHandler{
	void globalState();
}
