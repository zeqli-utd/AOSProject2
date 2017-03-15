package snapshot;
import aos.MessageHandler;
public interface Lock extends MessageHandler{
	public void requestCS();
	public void releaseCS();

}
