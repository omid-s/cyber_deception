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
import querying.tools.GraphObjectHelper;
import querying.tools.GraphQueryTools;

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
		graphHelper = new GraphObjectHelper(false, "");
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

	ArrayList<AccessCall> globalEdges = new ArrayList<AccessCall>();
	GraphObjectHelper graphHelper = null;
	/*
	 * (non-Javadoc)
	 * 
	 * @see querying.adapters.BaseAdapter#runQuery(querying.parsing.ParsedQuery)
	 */
	@Override
	public Graph<ResourceItem, AccessCall> runQuery(ParsedQuery theQuery) throws QueryFormatException {

		// create the return graph
		Graph<ResourceItem, AccessCall> ret = new DirectedOrderedSparseMultigraph<ResourceItem, AccessCall>();

		GraphQueryTools gt = new GraphQueryTools();

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

				// for the sake of proess to be over writing the filters from others
				if (pick == ResourceType.Process)
					TempCriteria += "1=1";
				else
					TempCriteria += String.format("fd_typechar='%s'", EnumTools.resourceTypeToChar(pick));

			}
			if (TempCriteria.length() > 0)
				criterias.add(TempCriteria);
			TempCriteria = "";

			for (Criteria pick : theQuery.getCriterias()) {
				if (TempCriteria.length() != 0)
					TempCriteria += " and ";

				String field = "fd_name";
				if (pick.getFieldType().contains(ResourceType.Process) && pick.getFieldName().equals("pid"))
					field = "proc_pid";
				else if (pick.getFieldType().contains(ResourceType.Process) && pick.getFieldName().equals("name"))
					field = "proc_name";
				else if (pick.getFieldName().equals("pid") && !pick.getFieldType().contains(ResourceType.Process))
					field = "fd_num";
				else
					field = "fd_name";

				TempCriteria += String.format(" %s %s '%4$s%s%4$s'  ", field, pick.getOp().equals("is") ? "=" : "like",
						pick.getValue(), pick.getOp().equals("is") ? "" : "%");

			}
			if (TempCriteria.length() > 0)
				criterias.add(TempCriteria);
			TempCriteria = "";

			String where_clause = "";
			for (String pick : criterias) {
				if (where_clause.length() != 0)
					where_clause += " and ";

				where_clause += String.format("(%s)", pick);
			}

			String Query = String.format("select %s from sysdigoutput  where %s", Fields, where_clause);

			System.out.println(Query);
			Statement st = theConnection.createStatement();
			ResultSet resutls = st.executeQuery(Query);

			SysdigObjectDAL objectDAL = new SysdigObjectDAL(true);
			

			while (resutls.next()) {
				try {
					SysdigRecordObject temp = objectDAL.LoadFromResultSet(resutls);

					graphHelper.AddRowToGraph(ret, temp);

				} catch (Exception ex) {
					ex.printStackTrace();
					continue;
				}
			}

			graphHelper.pruneByType(ret, theQuery);

		} catch (SQLException ex) {
			ex.printStackTrace();
		} catch (NoSuchFieldException ex) {
			ex.printStackTrace();
		} catch (SecurityException ex) {
			ex.printStackTrace();
		}

		// if merge is desired merge the Graphs
		if (theQuery.isDoesAppend())
			graphHelper.mergeGraphs(ret, theQuery.getOriginalGraph());

		return ret;
	}

}
