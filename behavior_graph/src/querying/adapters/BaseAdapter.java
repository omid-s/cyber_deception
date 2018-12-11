/**
 * 
 */
package querying.adapters;

import classes.AccessCall;
import classes.ResourceItem;
import edu.uci.ics.jung.graph.Graph;
import exceptions.QueryFormatException;
import querying.parsing.ParsedQuery;

/**
 * @author omid
 *
 */
public abstract class BaseAdapter {

	/**
	 * This method should be overidden to provide the query logic in the class.
	 * 
	 * @param theQuery The parsed query object
	 * @return returns the new graph created based on the graph
	 * @throws QueryFormatException is thrown if an adapter recieves a value it does
	 *                              not undrestand
	 */
	abstract public Graph<ResourceItem, AccessCall> runQuery(ParsedQuery theQuery) throws QueryFormatException;

	/**
	 * adapter classes are supposed to be following a singleton pattern This method
	 * will be used to fetch the instance.
	 * 
	 * @return returns the active instance of the class
	 */
	public static BaseAdapter getSignleton() {
		throw new IllegalStateException("getSingleton has not been implemented in the subclass! that's wierd ... ");
	}

}
