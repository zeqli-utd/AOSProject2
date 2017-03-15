package snapshot;
import java.util.*;
public class Utility {
	public static void myWait(Object obj){
		System.out.println("waiting");
		try{
			obj.wait();
		}catch(InterruptedException e){
			
		}
	}
	
	public static void mySleep(int time){
		try{
			Thread.sleep(time);
		}catch(InterruptedException e){
			
		}
	}

}
