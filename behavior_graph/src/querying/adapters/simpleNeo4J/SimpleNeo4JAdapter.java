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

//				System.out.println(resutls.getObject("a.title"));
//
//				System.out.println();

//					String row = "";
//				for (int i = 1; i <= columnCount; i++) {
//					row += resutls.getString(i) + ", ";
//				}
//				System.out.println(row);

				
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
		// create criterias based on node types ( exckuysing process )
//		String TempCriteria = "";
//		for (ResourceType pick : theQuery.getVerticeTypes()) {
//
//			if (TempCriteria.length() != 0)
//				TempCriteria += " or ";
//
//			// for the sake of proess to be over writing the filters from others
//			if (pick == ResourceType.Process)
//				TempCriteria += "1=1";
//			else
//				TempCriteria += String.format("fd_typechar='%s'", EnumTools.resourceTypeToChar(pick));
//
//		}

		return ret;
	}

	private SysdigRecordObjectGraph getSysdigObjectGraphFromResultSet(ResultSet inp) throws Exception {

	
		Map<String, Object> _r1 =(Map<String, Object> ) inp.getObject("a");

		ResourceItem r1 = new ResourceItem();
		r1.id = (String)_r1.get("id");
		r1.Description = (String)_r1.get("description");
		r1.Number = (String)_r1.get("number");
		r1.Path = (String)_r1.get("path");
		r1.Title =(String)_r1.get("title");
		r1.Type = EnumTools.searchEnum(ResourceType.class, (String)_r1.get("type"));

		Map<String, Object> _r2 =(Map<String, Object> ) inp.getObject("c");

		ResourceItem r2 = new ResourceItem();
		r2.id = (String)_r2.get("id");
		r2.Description = (String)_r2.get("description");
		r2.Number = (String)_r2.get("number");
		r2.Path = (String)_r2.get("path");
		r2.Title =(String)_r2.get("title");
		r2.Type = EnumTools.searchEnum(ResourceType.class, (String)_r2.get("type"));

		
//		 
		Map<String, Object> _call =(Map<String, Object> ) inp.getObject("b");
		
		AccessCall call = new AccessCall();
		call.args = (String)_call.get("args");
		call.Command = (String)_call.get("command");
		call.DateTime = (String)_call.get("date");
		call.Description = (String)_call.get("description");
		call.From = r1;
		call.To = r2;
		call.id = (String)_call.get("id");
		call.Info = (String)_call.get("info");
		call.OccuranceFactor = 1;
		call.sequenceNumber= 1;///1/inp.getLong("b.id");
		call.user_id = (String)_call.get("user_id");
		call.user_name= (String)_call.get("user_name");
		
		SysdigRecordObjectGraph ret= new SysdigRecordObjectGraph();
		ret.setProc(r1);
		ret.setItem(r2);
		ret.setSyscall(call);

		
//		ResourceItem r1 = new ResourceItem();
//		r1.id = inp.getString("a.number") + "|"+inp.getString("a.title"); //  inp.getString("a.id");
//		r1.Description = inp.getString("a.description");
//		r1.Number = inp.getString("a.number");
//		r1.Path = inp.getString("a.path");
//		r1.Title = inp.getString("a.title");
//		r1.Type = EnumTools.searchEnum(ResourceType.class, inp.getString("a.type"));
//
//		ResourceItem r2 = new ResourceItem();
//		r2.id =  inp.getString("c.number") + "|"+inp.getString("c.title"); //inp.getString("c.id");
//		r2.Description = inp.getString("c.description");
//		r2.Number = inp.getString("c.number");
//		r2.Path = inp.getString("c.path");
//		r2.Title = inp.getString("c.title");
//		r2.Type = EnumTools.searchEnum(ResourceType.class, inp.getString("c.type"));
//
//		AccessCall call = new AccessCall();
//		call.args = inp.getString("b.args");
//		call.Command = inp.getString("b.command");
//		call.DateTime = inp.getString("b.date");
//		call.Description = inp.getString("b.description");
//		call.From = r1;
//		call.To = r2;
//		call.id = inp.getString("b.id");
//		call.Info = inp.getString("b.info");
//		call.OccuranceFactor = 1;
//		call.sequenceNumber= inp.getLong("b.id");
//		call.user_id = inp.getString("b.user_id");
//		call.user_name= inp.getString("b.user_name");
//		
//		SysdigRecordObjectGraph ret= new SysdigRecordObjectGraph();
//		ret.setProc(r1);
//		ret.setItem(r2);
//		ret.setSyscall(call);
		
		return ret;
	}

}
