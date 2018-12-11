/**
 * 
 */
package querying.adapters.simplePG;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import classes.AccessCall;
import classes.ResourceItem;
import classes.ResourceType;
import classes.SysdigRecordObject;
import controlClasses.GraphObjectHelper;
import dataBaseStuff.DataBaseLayer;
import dataBaseStuff.SysdigObjectDAL;
import edu.uci.ics.jung.graph.DirectedOrderedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
import exceptions.QueryFormatException;
import helpers.Configurations;
import querying.adapters.BaseAdapter;
import querying.parsing.Criteria;
import querying.parsing.ParsedQuery;
import querying.tools.EnumTools;

/**
 * @author omid
 *
 *         This class implements the basic postgres connection adapoter This
 *         same class can be used with minimal changes to be used with any
 *         tabular data base
 *
 */
public class SimplePGAdapter extends BaseAdapter {

	private static SimplePGAdapter _instance = null;

	private SimplePGAdapter() {
	}

	/**
	 * returns a singleton object of the adapter object
	 * 
	 * @return
	 */
	public static SimplePGAdapter getSignleton() {

		if (_instance == null)
			_instance = new SimplePGAdapter();

		return _instance;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see querying.adapters.BaseAdapter#runQuery(querying.parsing.ParsedQuery)
	 */
	@Override
	public Graph<ResourceItem, AccessCall> runQuery(ParsedQuery theQuery) throws QueryFormatException {

		// create the return graph
		Graph<ResourceItem, AccessCall> ret = (!theQuery.isDoesAppend())
				? new DirectedOrderedSparseMultigraph<ResourceItem, AccessCall>()
				: theQuery.getOriginalGraph();

		try {
			// get the connection
			Connection theConnection = DataBaseLayer.getConnection();

			String Fields = "";

			for (String x : Configurations.getShortFieldList()) {
				if (Fields.length() > 0)
					Fields += ",";
				Fields += x;
			}

			/// create the seed node fetch
			ArrayList<String> criterias = new ArrayList<String>();

			// create criterias bvased on node types ( exckuysing process )
			String TempCriteria = "";
			for (ResourceType pick : theQuery.getVerticeTypes()) {
				if (TempCriteria.length() != 0)
					TempCriteria += " or ";

				TempCriteria += String.format("fd_typechar='%s'", EnumTools.resourceTypeToChar(pick));

			}
			criterias.add(TempCriteria);
			TempCriteria = "";


			
			//TODO : implement the process case! 
			
			
			String where_clause = "";
			for (String pick : criterias) {
				if (where_clause.length() != 0)
					where_clause += " and ";
				
				where_clause += String.format( "(%s)", pick);
			}
			
			String Query = String.format("select %s from sysdigoutput  where %s", Fields, where_clause);
			Statement st = theConnection.createStatement();
			ResultSet resutls = st.executeQuery(Query);

			SysdigObjectDAL objectDAL = new SysdigObjectDAL(true);
			GraphObjectHelper graphHelper = new GraphObjectHelper(theQuery.isVerbose(), "");

			while (resutls.next()) {
				try {
					SysdigRecordObject temp = objectDAL.LoadFromResultSet(resutls);

					graphHelper.AddRowToGraph(ret, temp);

				} catch (Exception ex) {
					ex.printStackTrace();
					continue;
				}

			}

		} catch (SQLException ex) {
			ex.printStackTrace();
		} catch (NoSuchFieldException ex) {
			ex.printStackTrace();
		} catch (SecurityException ex) {
			ex.printStackTrace();
		}

		return ret;
	}

}
