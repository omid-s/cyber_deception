/**
 * 
 */
package querying.adapters.memory;

import java.util.*;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.security.auth.x500.X500Principal;

import classes.*;
import controlClasses.RuntimeVariables;
import edu.uci.ics.jung.graph.DirectedOrderedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseMultigraph;
import exceptions.QueryFormatException;
import querying.adapters.BaseAdapter;
import querying.parsing.Criteria;
import querying.parsing.ParsedQuery;

/**
 * @author omido
 *
 */
public class InMemoryAdapter extends BaseAdapter {
	private InMemoryAdapter() {
	}

	private static InMemoryAdapter bmem = null;

	private Map<String, ResourceItem> V = new HashMap<String, ResourceItem>();
	private Map<String, AccessCall> E = new HashMap<String, AccessCall>();

	private Map<String, ArrayList<AccessCall>> fromsMap = new HashMap<String, ArrayList<AccessCall>>();
	private Map<String, ArrayList<AccessCall>> tosMap = new HashMap<String, ArrayList<AccessCall>>();

	private Map<String, ArrayList<AccessCall>> fromAndTosMap = new HashMap<String, ArrayList<AccessCall>>();

	private Map<String, ResourceItem> ProcessMap = new HashMap<String, ResourceItem>();
//	private Map<String, ArrayList<ResourceItem>> idMap = new HashMap<String, ArrayList<ResourceItem>>();
	private Map<String, ArrayList<ResourceItem>> FDMap = new HashMap<String, ArrayList<ResourceItem>>();

	public static InMemoryAdapter getSignleton() {
		if (bmem == null) {
			bmem = new InMemoryAdapter();
		}
		return bmem;
	}

	public void ClearAll() {
		V.clear();
		E.clear();
		fromsMap.clear();
		tosMap.clear();
		fromAndTosMap.clear();
		ProcessMap.clear();
		FDMap.clear();
	}

	public boolean hasAccessCall(String key) {
		return E.containsKey(key);
	}

	public boolean hasResourceItem(String Key) {
		return V.containsKey(Key);
	}

	public void addResourceItem(ResourceItem inp) {
		V.put(inp.id.toLowerCase(), inp);
		if (inp.Type == ResourceType.Process) {
			ProcessMap.put(inp.id.toLowerCase(), inp);

			if (!FDMap.containsKey(inp.Title.toLowerCase()))
				FDMap.put(inp.Title.toLowerCase(), new ArrayList<ResourceItem>());
			FDMap.get(inp.Title.toLowerCase()).add(inp);
		}
//		if (inp.Type == ResourceType.Thread) {
//			if (!idMap.containsKey(inp.Number.toLowerCase()))
//				idMap.put(inp.Number.toLowerCase(), new ArrayList<ResourceItem>());
//			idMap.get(inp.Number.toLowerCase()).add(inp);
//		}
		if (inp.Type == ResourceType.File || inp.Type == ResourceType.Pipe || inp.Type == ResourceType.NetworkIPV4
				|| inp.Type == ResourceType.NetworkIPV6 || inp.Type == ResourceType.Unix) {

			inp.Title = String.valueOf(inp.Title);
			if (!FDMap.containsKey(inp.Title.toLowerCase()))
				FDMap.put(inp.Title.toLowerCase(), new ArrayList<ResourceItem>());
			FDMap.get(inp.Title.toLowerCase()).add(inp);

//			if (!idMap.containsKey(inp.Number.toLowerCase()))
//				idMap.put(inp.Number.toLowerCase(), new ArrayList<ResourceItem>());
//			idMap.get(inp.Number.toLowerCase()).add(inp);
		}
	}

	public void addAccessCall(AccessCall inp) {
		// add to access calls
		E.put(inp.id, inp);

		// add the vertices of the end to the structure for the random access
		if (!fromsMap.containsKey(inp.From.id.toLowerCase())) {
			fromsMap.put(inp.From.id.toLowerCase(), new ArrayList<AccessCall>());
		}
		fromsMap.get(inp.From.id.toLowerCase()).add(inp);

		if (!tosMap.containsKey(inp.To.id.toLowerCase()))
			tosMap.put(inp.To.id.toLowerCase(), new ArrayList<AccessCall>());
		tosMap.get(inp.To.id.toLowerCase()).add(inp);

//		String keys = inp.From.id.toLowerCase() + "||" + inp.To.id.toLowerCase();
//		if (!fromAndTosMap.containsKey(keys))
//			fromAndTosMap.put(keys, new ArrayList<AccessCall>());
//		fromAndTosMap.get(keys).add(inp);

	}

	public ArrayList<AccessCall> getEdgesByVertice(ResourceItem key) {
		ArrayList<AccessCall> ret = new ArrayList<AccessCall>();
		ret.addAll(fromsMap.get(key.id));
		ret.addAll(tosMap.get(key.id));

		return ret;
	}

	public Collection<AccessCall> getAllCalls() {
		return E.values();
	}

	public Collection<ResourceItem> getAllResources() {
		return V.values();
	}

	public AccessCall getAccessCall(String Key) {
		return E.get(Key);
	}

	public ResourceItem getResourceItem(String Key) {
		return V.get(Key.toLowerCase());
	}

	/// query by type and return the vertices
	public ArrayList<ResourceItem> getResourceItemsByType(ArrayList<ResourceType> types) {
		ArrayList<ResourceItem> ret = new ArrayList<ResourceItem>();

		for (ResourceItem pick : V.values()) {
			if (types.contains(pick.Type))
				ret.add(pick);
		}
		return ret;
	}

	public static ArrayList<AccessCall> edges_for_describe = new ArrayList<AccessCall>();

	/**
	 * This method runs the query and returns the filtered query from the in memory
	 * object
	 */
	@Override
	public Graph<ResourceItem, AccessCall> runQuery(ParsedQuery theQuery) throws QueryFormatException {

		return this.getSubGraph(theQuery.getVerticeTypes(), theQuery.getEdgeTypes(), theQuery.isVerbose(),
				theQuery.isBackTracked(), theQuery.isForwardTracked(), theQuery.getCriterias(),
				theQuery.getOriginalGraph(), theQuery.isDoesAppend());
	}

	public Graph<ResourceItem, AccessCall> getSubGraph(ArrayList<ResourceType> verticeTypes, ArrayList<String> edgeType,
			boolean isVerbose, boolean isBackTracked, boolean isForwardTracked, ArrayList<Criteria> criterias,
			Graph<ResourceItem, AccessCall> originalGraph, boolean doesAppend) throws QueryFormatException {

		Graph<ResourceItem, AccessCall> ret = (!doesAppend)
				? new DirectedOrderedSparseMultigraph<ResourceItem, AccessCall>()
				: originalGraph;

		// clear describe history if new instance is requested

		if (originalGraph == null)
			edges_for_describe = new ArrayList<AccessCall>();

		/**
		 * seperate edge based criterias
		 */
		ArrayList<Criteria> edge_criteria = new ArrayList<Criteria>();
		edge_criteria.addAll(criterias.stream().filter(
				x -> (x.getFieldName().equalsIgnoreCase("user_name") || x.getFieldName().equalsIgnoreCase("user_id")))
				.collect(Collectors.toList()));

		criterias.removeAll(edge_criteria);

		ArrayList<ResourceItem> temp = new ArrayList<ResourceItem>();
		ArrayList<ResourceItem> done = new ArrayList<ResourceItem>();
		if (criterias == null || criterias.size() == 0) {
			temp.addAll(V.values());
		} else
			for (Criteria pick : criterias) {

				switch (pick.getFieldName()) {
				case "pid":
					switch (pick.getOp()) {
					case "is":
						if (ProcessMap.containsKey(pick.getValue()))
							temp.add(ProcessMap.get(pick.getValue()));
						break;
					case "has":
						for (String x : ProcessMap.keySet()) {
							if (x.contains(pick.getValue()))
								temp.add(ProcessMap.get(x));
						}
						break;
					}
					break;
				case "name":
					switch (pick.getOp()) {
					case "is":
						if (FDMap.containsKey(pick.getValue()))
							for (ResourceItem x : FDMap.get(pick.getValue())) {
								if (pick.getFieldType().contains(x.Type) || pick.getFieldType().size() == 0)// cehck for
																											// filter
																											// type
									temp.add(x);
							}
						break;
					case "has":
						for (String x : FDMap.keySet()) {
							if (x.contains(pick.getValue()))
								for (ResourceItem y : FDMap.get(x)) {
									if (pick.getFieldType().contains(y.Type) || pick.getFieldType().size() == 0) // cehck
																													// for
																													// filter
																													// type
										temp.add(y);
								}
						}
						break;
					}
					break;
//				case "id":
//					switch (pick.getOp()) {
//					case "is":
//						if (idMap.containsKey(pick.getValue()))
//							for (ResourceItem x : idMap.get(pick.getValue())) {
//								if (pick.getFieldType().contains(x.Type) || pick.getFieldType().size() == 0)// cehck for filter type
//									temp.add(x);
//							}
//						break;
//					case "has":
//						for (String x : idMap.keySet()) {
//							if (x.contains(pick.getValue()))
//								for (ResourceItem y : idMap.get(x)) {
//									if (pick.getFieldType().contains(y.Type) || pick.getFieldType().size() == 0)// cehck for filter type
//										temp.add(y);
//								}
//						}
//						break;
//					}
//					break;
				case "user_name":
				case "user_id":
					break;
				default:
					throw (new QueryFormatException("Wrong field supplied for the query " + pick.getFieldName()
							+ " field does not exsit or can not be queried!"));
				}
			}

		ArrayList<ResourceItem> temp2 = new ArrayList<ResourceItem>();
		temp2.addAll(temp.stream().filter(x -> verticeTypes.size() == 0 || verticeTypes.contains(x.Type))
				.collect(Collectors.toList()));
		temp = temp2;

		ArrayList<Integer> depthMap = new ArrayList<Integer>();
		for (ResourceItem pick : temp) {
			depthMap.add(0);
			if (verticeTypes.size() == 0 || verticeTypes.contains(pick.Type))
				ret.addVertex(pick);
		}

		/**
		 * propcess all the edges that initiate from the chosen nodes, if a new noew is
		 * encountered, add it to the list ( this is a part of forwards analysis )
		 */
		while (temp.size() > 0) {

			/// pick a node
			ResourceItem v = temp.get(0);
			int depth = depthMap.get(0);
			depthMap.remove(0);
			temp.remove(0);
			done.add(v);

			/// if the node is not going anywhere then ignore it!
			// if (!fromsMap.containsKey(v.id.toLowerCase()))
			// continue;

			/// iterate over all the edges that go out of the picked node and
			/// add them as appropriate
			if (isForwardTracked && fromsMap.containsKey(v.id.toLowerCase())
					&& depth < RuntimeVariables.getInstance().getForwardDepth())
				for (AccessCall pick : fromsMap.get(v.id.toLowerCase())) {

					/**
					 * check if there is any criteria for user names and ids, apply it. ( if there
					 * is no user based criteria, add the edge, otherwise apply the sorter )
					 */
					if ((edge_criteria.size() > 0 && edge_criteria.stream()
							.anyMatch(x -> ((x.getOp().equalsIgnoreCase("has") && pick.user_name.contains(x.getValue())
									&& x.getFieldName().equalsIgnoreCase("user_name"))
									|| (x.getOp().equalsIgnoreCase("is") && pick.user_name.equals(x.getValue())
											&& x.getFieldName().equalsIgnoreCase("user_name"))
									|| (x.getOp().equalsIgnoreCase("has") && pick.user_id.contains(x.getValue())
											&& x.getFieldName().equalsIgnoreCase("user_id"))
									|| (x.getOp().equalsIgnoreCase("is") && pick.user_id.equals(x.getValue())
											&& x.getFieldName().equalsIgnoreCase("user_id")))))
							|| edge_criteria.size() == 0) {

						// add to list for describe
						edges_for_describe.add(pick);

						if (isVerbose) {
							if (!temp.contains(pick.To) && !done.contains(pick.To))
								temp.add(pick.To);

							if (edgeType.size() == 0 || (edgeType.size() != 0
									&& ((edgeType.contains("syscall") && isSysCall(pick.Command))
											|| edgeType.contains(pick.Command)))) {
								if (verticeTypes.size() == 0
										|| (verticeTypes.size() != 0 && (verticeTypes.contains(pick.To.Type)))) {
									// ret.addVertex(pick.From);
									ret.addVertex(pick.To);
									ret.addEdge(pick, pick.From, pick.To);
								}
							}

						} else {
							boolean edge_added_flag = false;
							for (AccessCall x : ret.getEdges()) {
								if (x.Command.equals(pick.Command) && x.From.id.equals(pick.From.id)
										&& x.To.id.equals(pick.To.id)) {
									x.OccuranceFactor++;
									edge_added_flag = true;
									break;
								}
							}
							if (!edge_added_flag) {

								AccessCall tempCall = new AccessCall();
								tempCall.Command = pick.Command;
								tempCall.Info = pick.Info;
								tempCall.Description = pick.Description;
								tempCall.From = pick.From;
								tempCall.To = pick.To;
								tempCall.user_id = pick.user_id;
								tempCall.user_name = pick.user_name;
								tempCall.OccuranceFactor = 1;
								tempCall.sequenceNumber = pick.sequenceNumber;
								// fromAndTosMap.get(pick.From.id.toLowerCase()
								// + "||" +
								// pick.To.id.toLowerCase()).size();
								if (edgeType.size() == 0 || (edgeType.size() != 0
										&& ((edgeType.contains("syscall") && isSysCall(pick.Command))
												|| edgeType.contains(pick.Command)))) {
									if (verticeTypes.size() == 0
											|| (verticeTypes.size() != 0 && (verticeTypes.contains(pick.To.Type)))) {
										ret.addVertex(pick.To);
										ret.addEdge(tempCall, tempCall.From, tempCall.To);
									}
								}
							}
							if (!temp.contains(pick.To) && !done.contains(pick.To)) {
								temp.add(pick.To);
								depthMap.add(depth + 1);
							}
						}
					}
				}

			if (isBackTracked && tosMap.containsKey(v.id.toLowerCase())
					&& depth < RuntimeVariables.getInstance().getBackDepth())
				for (AccessCall pick : tosMap.get(v.id.toLowerCase())) {

					/**
					 * check if there is any criteria for user names and ids, apply it. ( if there
					 * is no user based criteria, add the edge, otherwise apply the cirterai )
					 */
					if ((edge_criteria.size() > 0 && edge_criteria.stream()
							.anyMatch(x -> ((x.getOp().equalsIgnoreCase("has") && pick.user_name.contains(x.getValue())
									&& x.getFieldName().equalsIgnoreCase("user_name"))
									|| (x.getOp().equalsIgnoreCase("is") && pick.user_name.equals(x.getValue())
											&& x.getFieldName().equalsIgnoreCase("user_name"))
									|| (x.getOp().equalsIgnoreCase("has") && pick.user_id.contains(x.getValue())
											&& x.getFieldName().equalsIgnoreCase("user_id"))
									|| (x.getOp().equalsIgnoreCase("is") && pick.user_id.equals(x.getValue())
											&& x.getFieldName().equalsIgnoreCase("user_id")))))
							|| edge_criteria.size() == 0) {

						// add to list for describe
						edges_for_describe.add(pick);

						if (isVerbose) {
							if (!temp.contains(pick.From) && !done.contains(pick.From))
								temp.add(pick.From);

							if (edgeType.size() == 0 || (edgeType.size() != 0
									&& ((edgeType.contains("syscall") && isSysCall(pick.Command))
											|| edgeType.contains(pick.Command)))) {
								if (verticeTypes.size() == 0
										|| (verticeTypes.size() != 0 && (verticeTypes.contains(pick.From.Type)))) {
									// ret.addVertex(pick.From);
									ret.addVertex(pick.From);
									ret.addEdge(pick, pick.From, pick.To);
								}
							}

						} else {
							boolean edge_added_flag = false;
							for (AccessCall x : ret.getEdges()) {
								if (x.Command.equals(pick.Command) && x.From.id.equals(pick.From.id)
										&& x.To.id.equals(pick.To.id)) {
									x.OccuranceFactor++;
									edge_added_flag = true;
									break;
								}
							}
							if (!edge_added_flag) {

								AccessCall tempCall = new AccessCall();
								tempCall.Command = pick.Command;
								tempCall.Info = pick.Info;
								tempCall.Description = pick.Description;
								tempCall.From = pick.From;
								tempCall.To = pick.To;
								tempCall.user_id = pick.user_id;
								tempCall.user_name = pick.user_name;
								tempCall.OccuranceFactor = 1;
								tempCall.sequenceNumber = pick.sequenceNumber;
								// fromAndTosMap.get(pick.From.id.toLowerCase()
								// + "||" +
								// pick.To.id.toLowerCase()).size();
								if (edgeType.size() == 0 || (edgeType.size() != 0
										&& ((edgeType.contains("syscall") && isSysCall(pick.Command))
												|| edgeType.contains(pick.Command)))) {
									if (verticeTypes.size() == 0
											|| (verticeTypes.size() != 0 && (verticeTypes.contains(pick.To.Type)))) {
										ret.addVertex(pick.To);
										ret.addEdge(tempCall, tempCall.From, tempCall.To);
									}
								}
							}
						}
						if (!temp.contains(pick.From) && !done.contains(pick.From)) {
							temp.add(pick.From);
							depthMap.add(depth + 1);
						}
					}
				}
		}

		return ret;
	}

	private boolean isSysCall(String inp) {
		inp = inp.toLowerCase();
		ArrayList<String> types = new ArrayList<String>();
		types.add("service");
		types.add("api");
		types.add("thread");
		types.add("activity");
		types.add("execve");
		types.add("binder");
		return !types.contains(inp);
	}
}
