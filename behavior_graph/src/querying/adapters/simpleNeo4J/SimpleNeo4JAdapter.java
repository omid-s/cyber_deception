/**
 * 
 */
package querying.adapters.simpleNeo4J;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;

import classes.AccessCall;
import classes.ResourceItem;
import classes.ResourceType;
import dataBaseStuff.DataBaseLayer;
import edu.uci.ics.jung.graph.DirectedOrderedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
import exceptions.QueryFormatException;
import querying.adapters.BaseAdapter;
import querying.parsing.ParsedQuery;
import querying.tools.EnumTools;
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

	private static Connection TheConnection;
	private static Statement TheStateMent;

	/*
	 * (non-Javadoc)
	 * 
	 * @see querying.adapters.BaseAdapter#runQuery(querying.parsing.ParsedQuery)
	 */
	@Override
	public Graph<ResourceItem, AccessCall> runQuery(ParsedQuery theQuery) throws QueryFormatException {

		// create the return graph
		Graph<ResourceItem, AccessCall> ret = new DirectedOrderedSparseMultigraph<ResourceItem, AccessCall>();

		// free up memory if adding to previous graph is not desired
		if (!theQuery.isDoesAppend()) {
			graphHelper.release_maps();
			graphHelper = new GraphObjectHelper(theQuery.isVerbose(), "");
		}

		/// create the seed node fetch
		ArrayList<String> criterias = new ArrayList<String>();

//		String Query = "match (a)-[b]->(c) wheren";

		String Query = "match (a)-[b]->(c) where (a:File or a:Process) and  a.title =~ \".*nano.*\" return a,b,c";

		try {
			Connection theConnection = DataBaseLayer.getNeo4JConnection();
			Statement st = theConnection.createStatement();

			ResultSet resutls = st.executeQuery(Query);
			ResultSetMetaData metadata = resutls.getMetaData();
			int columnCount = metadata.getColumnCount();
			for (int i = 1; i <= columnCount; i++) {
				System.out.println(metadata.getColumnName(i) + "|| ");
			}
			while (resutls.next()) {

				int a = 12;
//				System.out.println(resutls.getObject("a"));
//
//				System.out.println();

				String row = "";
				for (int i = 1; i <= columnCount; i++) {
					row += resutls.getString(i) + ", ";
				}
				System.out.println(row);

			}

		} catch (Exception ex) {
			System.out.println(ex.getMessage());
			ex.printStackTrace();

		}
		// create criterias based on node types ( exckuysing process )
		String TempCriteria = "";
		for (ResourceType pick : theQuery.getVerticeTypes()) {

			if (TempCriteria.length() != 0)
				TempCriteria += " or ";

			// for the sake of proess to be over writing the filters from others
			if (pick == ResourceType.Process)
				TempCriteria += "1=1";
			else
				TempCriteria += String.format("fd_typechar='%s'", EnumTools.resourceTypeToChar(pick));

		}

		return null;
	}
}
