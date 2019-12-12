/**
 * 
 */
package aiconnector;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author omid
 *
 *	This class handles the main memory for the AI connector including the variables for each query that needs to be tracked
 * 
 */
public class AIConnectorMemory {

	private static AIConnectorMemory _instance = null ;
	private HashMap<Integer, Boolean> states_map = null;
	
	
	/***
	 * returns a singleton object of the class
	 * @return
	 */
	public static AIConnectorMemory getInstance() {
		if( _instance == null) 
			_instance = new AIConnectorMemory();
		
		return _instance;
	}
	
	/**
	 * creates an instance of the object
	 */
	public AIConnectorMemory() {
		
		this.states_map = new HashMap<Integer, Boolean>();
	}
	
	/**
	 * sets an state for an observation.
	 * @param observationID identifier of the observation 
	 * @param state the state to set for the observation 
	 */
	public void setStateForObservation(int observationID, boolean state) {
		this.states_map.put(observationID, state);
	}
	
	/***
	 * returns an string representation of the observations 
	 * @return
	 */
	public String getObservationString() {
		ArrayList<String> items = new ArrayList<>();
		for (int pick : states_map.keySet()) {
			items.add( String.format("%d:%d", pick, states_map.get(pick)? 1 : 0) );
		}
		
		return String.join(";",items);
	}
	
}
