/**
 * 
 */
package querying;

import classes.AccessCall;
import classes.ResourceItem;
import edu.uci.ics.jung.graph.Graph;
import exceptions.QueryFormatException;

/**
 * @author omid
 *
 */
public abstract class BaseAdapter {

	/**
	 * This method should be overidden to provide the query logic in the class. 
	 * @param theQuery The parsed query object 
	 * @return returns the new graph created based on the graph
	 * @throws QueryFormatException is thrown if an adapter recieves a value it does not undrestand 
	 */
	abstract public Graph<ResourceItem, AccessCall> getSubGraph( ParsedQuery theQuery ) throws QueryFormatException;
		
	
}
