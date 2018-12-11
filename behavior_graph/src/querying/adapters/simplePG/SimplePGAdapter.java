/**
 * 
 */
package querying.adapters.simplePG;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import classes.AccessCall;
import classes.ResourceItem;
import controlClasses.GraphObjectHelper;
import dataBaseStuff.DataBaseLayer;
import dataBaseStuff.SysdigObjectDAL;
import edu.uci.ics.jung.graph.DirectedOrderedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
import exceptions.QueryFormatException;
import querying.adapters.BaseAdapter;
import querying.parsing.ParsedQuery;

/**
 * @author omid
 *
 *         This class implements the basic postgres connection adapoter This
 *         same class can be used with minimal changes to be used with any
 *         tabular data base
 *
 */
public class SimplePGAdapter extends BaseAdapter {

	/*
	 * (non-Javadoc)
	 * 
	 * @see querying.adapters.BaseAdapter#runQuery(querying.parsing.ParsedQuery)
	 */
	@Override
	public Graph<ResourceItem, AccessCall> runQuery(ParsedQuery theQuery) throws QueryFormatException {

		// create the return graph
		Graph<ResourceItem, AccessCall> ret = (!theQuery.isDoesAppend())
				? new DirectedOrderedSparseMultigraph<ResourceItem, AccessCall>() : theQuery.getOriginalGraph();

		try {
			// get the connection
			Connection theConnection = DataBaseLayer.getConnection();
			
			String Query = "select * from sysdigoutput limit 5";
			Statement st  = theConnection.createStatement();
			ResultSet resutls =  st.executeQuery(Query);
			
			
			SysdigObjectDAL objectDAL = new SysdigObjectDAL(false);
			GraphObjectHelper graphHelper = new GraphObjectHelper(theQuery.isVerbose(), "");
			
			
			
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
		catch ( NoSuchFieldException ex ){
			ex.printStackTrace();
		}
		catch( SecurityException ex ){
			ex.printStackTrace();
		}

		return ret;
	}

}
