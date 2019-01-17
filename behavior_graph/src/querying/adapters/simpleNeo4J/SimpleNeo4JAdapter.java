/**
 * 
 */
package querying.adapters.simpleNeo4J;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Map;

import classes.AccessCall;
import classes.ResourceItem;
import classes.ResourceType;
import classes.SysdigRecordObject;
import classes.SysdigRecordObjectGraph;
import dataBaseStuff.DataBaseLayer;
import edu.uci.ics.jung.graph.DirectedOrderedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
import exceptions.QueryFormatException;
import querying.adapters.BaseAdapter;
import querying.adapters.memory.InMemoryAdapter;
import querying.parsing.Criteria;
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

		// create criterias based on node types ( exckuysing process )
		String TempCriteria = "";

		for (ResourceType pick : theQuery.getVerticeTypes()) {

			if (TempCriteria.length() != 0)
				TempCriteria += " or ";

			TempCriteria += String.format("(a:%s or c:%s)", pick.toString(), pick.toString());
		}
		if (TempCriteria.length() > 0)
			criterias.add(TempCriteria);

		TempCriteria = "";
		for (String pick : theQuery.getEdgeTypes()) {

			if (TempCriteria.length() != 0)
				TempCriteria += " or ";

			TempCriteria += String.format("(b.command=\"%s\")", pick);
		}
		if (TempCriteria.length() > 0)
			criterias.add(TempCriteria);

		TempCriteria = "";
		for (Criteria pick : theQuery.getCriterias()) {
			if (TempCriteria.length() != 0)
				TempCriteria += " and ";

			String field = "fd_name";
			if (pick.getFieldName().equals("pid"))
				field = "id";
			else if (pick.getFieldName().equals("name"))
				field = "title";

			String typeCriteria = "";
			for (ResourceType p : pick.getFieldType()) {

				if (typeCriteria.length() > 0)
					typeCriteria += " or ";

				typeCriteria += String.format(" (a:%1$s or c:%1$ ) ", p.toString());
			}

			if (!theQuery.isBackTracked() && !theQuery.isForwardTracked())
				TempCriteria = String.format("( a.%1$s%2$s\"%3$s%4$s%3$s\" or c.%1$s%2$s\"%3$s%4$s%3$s\" )", field,
						pick.getOp().equals("is") ? " = " : " =~ ", pick.getOp().equals("is") ? "" : ".*",
						pick.getValue());
			else if (theQuery.isBackTracked())
				TempCriteria = String.format("( y.%1$s%2$s\"%3$s%4$s%3$s\")", field,
						pick.getOp().equals("is") ? " = " : " =~ ", pick.getOp().equals("is") ? "" : ".*",
						pick.getValue());
			else if (theQuery.isForwardTracked())
				TempCriteria = String.format("( x.%1$s%2$s\"%3$s%4$s%3$s\")", field,
						pick.getOp().equals("is") ? " = " : " =~ ", pick.getOp().equals("is") ? "" : ".*",
						pick.getValue());

			if (typeCriteria.length() > 0)
				TempCriteria += " and " + typeCriteria;

		}
		if (TempCriteria.length() > 0)
			criterias.add(TempCriteria);

		String where_clause = "";
		for (String pick : criterias) {
			if (where_clause.length() != 0)
				where_clause += " and ";

			where_clause += String.format("(%s)", pick);
		}

		String Query;

		if (!theQuery.isBackTracked() && !theQuery.isForwardTracked())
			Query = String.format("match (a)-[b]->(c) where %s return a,b,c ", where_clause);
		else if (theQuery.isBackTracked())
			Query = String.format(
					"match (x)-[*]->(y) match (a)-[b]->(c) where %s and  (c.id=y.id or c.id=x.id)  return a,b,c",
					where_clause);
		else
			Query = String.format(
					"match (x)-[*]->(y) match (a)-[b]->(c) where %s and  (a.id=y.id or a.id=x.id)  return a,b,c",
					where_clause);

		try {
			Connection theConnection = DataBaseLayer.getNeo4JConnection();
			Statement st = theConnection.createStatement();

			ResultSet resutls = st.executeQuery(Query);

			while (resutls.next()) {

				try {
					SysdigRecordObjectGraph temp = getSysdigObjectGraphFromResultSet(resutls);

					graphHelper.AddRowToGraph(ret, temp);

				} catch (Exception ex) {
					ex.printStackTrace();
					continue;
				}
			}
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
			ex.printStackTrace();
		}

		// create and clean the memory object
		InMemoryAdapter mem = InMemoryAdapter.getSignleton();
		mem.ClearAll();

		try {
			// add nodes and edges :
			for (ResourceItem pick : ret.getVertices())
				mem.addResourceItem(pick);
			for (AccessCall pick : ret.getEdges())
				mem.addAccessCall(pick);

			ret = mem.runQuery(theQuery);
		} catch (Exception ex) {
			System.out.println(ex.getCause() + "\n" + ex.getMessage());

			ex.printStackTrace();
		}
		// if merge is desired merge the Graphs
		if (theQuery.isDoesAppend())
			ret = graphHelper.mergeGraphs2(ret, theQuery.getOriginalGraph());

		return ret;
	}

	/**
	 * turns a neo4j record to a sysdig objectRecord Graph
	 * 
	 * @param inp
	 * @return
	 * @throws Exception
	 */
	private SysdigRecordObjectGraph getSysdigObjectGraphFromResultSet(ResultSet inp) throws Exception {

		Map<String, Object> _r1 = (Map<String, Object>) inp.getObject("a");

		ResourceItem r1 = new ResourceItem();
		r1.id = (String) _r1.get("id");
		r1.Description = (String) _r1.get("description");
		r1.Number = (String) _r1.get("number");
		r1.Path = (String) _r1.get("path");
		r1.Title = (String) _r1.get("title");
		r1.Type = EnumTools.searchEnum(ResourceType.class, (String) _r1.get("type"));

		Map<String, Object> _r2 = (Map<String, Object>) inp.getObject("c");

		ResourceItem r2 = new ResourceItem();
		r2.id = (String) _r2.get("id");
		r2.Description = (String) _r2.get("description");
		r2.Number = (String) _r2.get("number");
		r2.Path = (String) _r2.get("path");
		r2.Title = (String) _r2.get("title");
		r2.Type = EnumTools.searchEnum(ResourceType.class, (String) _r2.get("type"));

		Map<String, Object> _call = (Map<String, Object>) inp.getObject("b");

		AccessCall call = new AccessCall();
		call.args = (String) _call.get("args");
		call.Command = (String) _call.get("command");
		call.DateTime = (String) _call.get("date");
		call.Description = (String) _call.get("description");
		call.From = r1;
		call.To = r2;
		call.id = (String) _call.get("id");
		call.Info = (String) _call.get("info");
		call.OccuranceFactor = 1;
		call.sequenceNumber = 1;/// 1/inp.getLong("b.id");
		call.user_id = (String) _call.get("user_id");
		call.user_name = (String) _call.get("user_name");

		SysdigRecordObjectGraph ret = new SysdigRecordObjectGraph();
		ret.setProc(r1);
		ret.setItem(r2);
		ret.setSyscall(call);

		return ret;
	}

}
