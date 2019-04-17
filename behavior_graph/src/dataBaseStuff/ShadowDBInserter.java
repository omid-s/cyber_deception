/**
 * 
 */
package dataBaseStuff;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

import classes.AccessCall;
import classes.ResourceItem;

/**
 * @author omid
 *
 */
public class ShadowDBInserter {

	private static ShadowDBInserter __Instance= null; // holds the singleton instance of shadow inserter
	private ConcurrentLinkedQueue<ResourceItem> theResourceQue;  //
	private ConcurrentLinkedQueue<AccessCall> theCallQue;
	
	/**
	 * returns the singleton inserter object. 
	 * @return the inserter object
	 */
	public static ShadowDBInserter getInstance() {
		if( __Instance == null )
			__Instance = new ShadowDBInserter();
		
		return __Instance;
	}
	
	
	/**
	 * creates an instance of the shadow inserter object 
	 */
	private  ShadowDBInserter() {
		theResourceQue= new ConcurrentLinkedQueue<ResourceItem>();
		theCallQue= new ConcurrentLinkedQueue<AccessCall>();
		
		//TODO : implenment the threaded inserter 
		
	}
	
}
