/**
 * 
 */
package querying.adapters.simpleNeo4J;

import java.util.ArrayList;

import classes.AccessCall;
import classes.ResourceItem;
import edu.uci.ics.jung.graph.Graph;
import exceptions.QueryFormatException;
import querying.adapters.BaseAdapter;
import querying.parsing.ParsedQuery;
import querying.tools.GraphObjectHelper;

/**
 * @author omid
 *
 */
public class SimpleNeo4JAdapter extends BaseAdapter {

	private static SimpleNeo4JAdapter _instance = null;

	private SimpleNeo4JAdapter() {
		graphHelper = new GraphObjectHelper(false, "");
	}

	/**
	 * returns a singleton object of the adapter object
	 * 
	 * @return
	 */
	public static SimpleNeo4JAdapter getSignleton() {

		if (_instance == null)
			_instance = new SimpleNeo4JAdapter();

		return _instance;
	}

	ArrayList<AccessCall> globalEdges = new ArrayList<AccessCall>();
	GraphObjectHelper graphHelper = null;
	
	

	/* (non-Javadoc)
	 * @see querying.adapters.BaseAdapter#runQuery(querying.parsing.ParsedQuery)
	 */
	@Override
	public Graph<ResourceItem, AccessCall> runQuery(ParsedQuery theQuery) throws QueryFormatException {
		// TODO Auto-generated method stub
		return null;
	}
}
