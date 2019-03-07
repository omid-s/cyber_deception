/**
 * 
 */
package querying.adapters.simplePG;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.apache.commons.collections15.iterators.ArrayListIterator;

import classes.AccessCall;
import classes.ResourceItem;
import classes.ResourceType;
import classes.SysdigRecordObject;
import controlClasses.RuntimeVariables;
import dataBaseStuff.DataBaseLayer;
import edu.uci.ics.jung.graph.DirectedOrderedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
import exceptions.QueryFormatException;
import helpers.Configurations;
import querying.adapters.BaseAdapter;
import querying.adapters.memory.InMemoryAdapter;
import querying.parsing.Criteria;
import querying.parsing.ParsedQuery;
import querying.tools.EnumTools;
import querying.tools.GraphObjectHelper;
import querying.tools.GraphQueryTools;
import readers.SysdigObjectDAL;

//TODO : deal with per query verbosity

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

		// free up memory if adding to previous graph is not desired
		if (!theQuery.isDoesAppend()) {
			graphHelper.release_maps();
			graphHelper = new GraphObjectHelper(theQuery.isVerbose(), "");
		}

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

				if (!pick.getFieldType().contains(ResourceType.Process))
					TempCriteria += String.format(" %s %s '%4$s%s%4$s'  ", field,
							pick.getOp().equals("is") ? "=" : "like", pick.getValue(),
							pick.getOp().equals("is") ? "" : "%");
				else {

					TempCriteria += String.format(" %1$s %2$s '%4$s%3$s%4$s' or %5$s %2$s '%4$s%3$s%4$s'  ", field,
							pick.getOp().equals("is") ? "=" : "like", pick.getValue(),
							pick.getOp().equals("is") ? "" : "%", field.equals("proc_pid") ? "proc_ppid" : "proc_pname"

					);
				}

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

			if (RuntimeVariables.getInstance().getPrint_query())
				System.out.println(Query);
			Statement st = theConnection.createStatement();
			ResultSet resutls = st.executeQuery(Query);

			SysdigObjectDAL objectDAL = new SysdigObjectDAL(true);

			while (resutls.next()) {
				try {
					if (resutls == null)
						break;
					SysdigRecordObject temp = objectDAL.LoadFromResultSet(resutls);

					graphHelper.AddRowToGraph(ret, temp);

				} catch (Exception ex) {
					ex.printStackTrace();
					continue;
				}
			}

			st.close();

//			graphHelper.pruneByType(ret, theQuery);

			// TODO :Handle Backtrack
			int depthCounter = 0;
			if (theQuery.isBackTracked()) {

				ArrayList<String> dones = new ArrayList<String>();
				ArrayList<ResourceItem> stack = new ArrayList<ResourceItem>();

				// in each iteration find the heads and fetch from there
				for (ResourceItem pick : ret.getVertices()) {
					if (ret.inDegree(pick) == 0 && !dones.contains(pick.getID()))
						stack.add(pick);
				}

				// while there is a back tracable item, back trace it
				do {
					criterias.clear();

					for (ResourceItem pick : stack) {
						dones.add(pick.getID());
						if (pick.Type == ResourceType.Process) {
							criterias.add(String.format("(select %s from sysdigoutput  where proc_pid='%s' limit 1 )",
									Fields, pick.Number));
						} else {
							criterias.add(String.format("(select %s from sysdigoutput  where fd_name='%s' )", Fields,
									pick.Title));
						}
					}
					stack.clear();

					where_clause = "";
					for (String pick : criterias) {
						if (where_clause.length() != 0)
							where_clause += "\nunion all\n";
						where_clause += pick;
					}

					Query = where_clause;

					st = theConnection.createStatement();
					resutls = st.executeQuery(Query);

					// add the newly added items to the graph
					while (resutls.next()) {
						try {
							SysdigRecordObject temp = objectDAL.LoadFromResultSet(resutls);

							graphHelper.AddRowToGraph(ret, temp);

						} catch (Exception ex) {
//							System.out.println(ex.getMessage());
//							ex.printStackTrace();
							continue;
						}
					}

					st.close();

					for (ResourceItem pick : ret.getVertices()) {
						if (ret.inDegree(pick) == 0 && !dones.contains(pick.getID()))
							stack.add(pick);
					}
					depthCounter++;
				} while (stack.size() > 0 && depthCounter <= Integer
						.parseInt(Configurations.getInstance().getSetting(Configurations.BACKWARD_STEPS)));

			}

			// TODO : handle forward track

			if (theQuery.isForwardTracked()) {
				ArrayList<String> dones = new ArrayList<String>();
				ArrayList<ResourceItem> stack = new ArrayList<ResourceItem>();

				// in each iteration find the heads and fetch from there
				for (ResourceItem pick : ret.getVertices()) {
					if (ret.outDegree(pick) == 0 && !dones.contains(pick.getID()))
						stack.add(pick);
				}

				// while there is a back tracable item, back trace it
				do {
					criterias.clear();

					for (ResourceItem pick : stack) {
						dones.add(pick.getID());
						if (pick.Type == ResourceType.Process) {
							criterias.add(String.format(
									"(select %s from sysdigoutput  where proc_pid='%s' or proc_ppid='%s'  )", Fields,
									pick.Number, pick.Number));
						} else {
//							criterias.add(String.format("(select %s from sysdigoutput  where fd_name='%s' )", Fields,
//									pick.Title));
						}
					}
					stack.clear();

					where_clause = "";
					for (String pick : criterias) {
						if (where_clause.length() != 0)
							where_clause += "\nunion all\n";
						where_clause += pick;
					}
					try {
						Query = where_clause;
						st = theConnection.createStatement();
						resutls = st.executeQuery(Query);

						// add the newly added items to the graph
						while (resutls.next()) {
							try {
								SysdigRecordObject temp = objectDAL.LoadFromResultSet(resutls);

								graphHelper.AddRowToGraph(ret, temp);

							} catch (Exception ex) {
								System.out.println(ex.getMessage());
								ex.printStackTrace();
								continue;
							}
						}

						st.close();

					} catch (SQLException ex) {
						// ignore if there is a sql error (most commonly, no result found!)
						// TODO : arrange better error handling here!
						continue;
					}

					for (ResourceItem pick : ret.getVertices()) {
						if (ret.outDegree(pick) == 0 && !dones.contains(pick.getID()))
							stack.add(pick);
					}

					depthCounter++;

				} while (stack.size() > 0 && depthCounter <= Integer
						.parseInt(Configurations.getInstance().getSetting(Configurations.FORWARD_STEPS)));
			}

		} catch (SQLException ex) {
			System.out.println(ex.getMessage());
			ex.printStackTrace();
		} catch (NoSuchFieldException ex) {
			System.out.println(ex.getMessage());
			ex.printStackTrace();
		} catch (SecurityException ex) {
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

}
