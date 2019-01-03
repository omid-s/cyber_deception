package querying.tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Vector;

import javax.vecmath.GVector;

import classes.AccessCall;
import classes.ResourceItem;
import classes.ResourceType;
import classes.SysdigRecordObject;
import dataBaseStuff.SysdigObjectDAL;
import edu.uci.ics.jung.graph.Graph;
import querying.parsing.ParsedQuery;

public class GraphObjectHelper {
	private final boolean isInVerboseMode;
	private String pid;
	private HashSet<String> vectorPid;
	private HashSet<String> vectorFile;
	private HashMap<String, String> currentActivity;
	private HashMap<String, String> currentService;
	private HashSet<String> blacklist;
	private int count = 0;

	private long sequenceCounter = 0;

	private HashMap<ResourceType, HashMap<String, ResourceItem>> resourcesMap;
	private HashMap<String, AccessCall> EdgeMap;

	public GraphObjectHelper(boolean isInVerboseMode, String _pid) {
		this.isInVerboseMode = isInVerboseMode;

		resourcesMap = new HashMap<ResourceType, HashMap<String, ResourceItem>>();
		EdgeMap = new HashMap<String, AccessCall>();

		pid = _pid;
		vectorPid = new HashSet<String>();
		vectorPid.add(_pid);
		currentActivity = new HashMap<String, String>();
		currentService = new HashMap<String, String>();
		blacklist = new HashSet<String>();
		blacklist.add("/dev/pmsg0");
		blacklist.add("/dev/cpuctl/tasks");
		blacklist.add("/dev/cpuctl/bg_non_interactive/tasks");
		blacklist.add("/dev/ashmem");
		blacklist.add("1158");
	}

	public String getCurrentActivity(String pid) {
		return currentActivity.get(pid);
	}

	public void setCurrentActivity(String pid, String name) {
		currentActivity.put(pid, name);
	}

	public String getCurrentService(String pid) {
		return currentService.get(pid);
	}

	public void setCurrentService(String pid, String name) {
		currentService.put(pid, name);
	}

	/**
	 * Based on process and file descriptor fields create nodes and vertices that
	 * correspond to the call record then add them to the graph supplied
	 * 
	 * @param theGraph the graph to add nodes and edges to
	 * @param pick     the row object to be processed
	 */

	public void AddRowToGraph(Graph<ResourceItem, AccessCall> theGraph, SysdigRecordObject pick) {
		ResourceItem FromItem = null;
		ResourceItem ToItem = null;
		ResourceItem TheProc = null;

		// is process new ?

		if (!resourcesMap.containsKey(ResourceType.Process)) {
			resourcesMap.put(ResourceType.Process, new HashMap<String, ResourceItem>());
		}

		if (!resourcesMap.get(ResourceType.Process).containsKey(pick.getProcPID())) {
			// if (!theGraph.getVertices().stream()
			// .anyMatch(x -> x.Type == ResourceType.Process &&
			// x.id.equals(pick.getProcPID()))) {
			// add the process
			ResourceItem tempItem = new ResourceItem();

			tempItem.Type = ResourceType.Process;
			tempItem.Number = pick.proc_pid;
			tempItem.id = pick.getProcPID();
			tempItem.Title = pick.proc_name;
			tempItem.Description = pick.proc_args;

			TheProc = tempItem;
			theGraph.addVertex(tempItem);
			// resourcesMap.get(ResourceType.Process).put(pick.getProcPID(),
			// TheProc);
			resourcesMap.get(ResourceType.Process).put(pick.getProcPID(), TheProc);
		} else {
			// TheProc = theGraph.getVertices().stream()
			// .filter(x -> x.Type == ResourceType.Process &&
			// x.id.equals(pick.getProcPID())).findFirst().get();
			TheProc = resourcesMap.get(ResourceType.Process).get(pick.getProcPID());

		}

		theGraph.addVertex(TheProc);

		ResourceItem parentP = null;

		if (resourcesMap.get(ResourceType.Process).containsKey(pick.getParentProcID()))
		// if (theGraph.getVertices().stream()
		// .anyMatch(x -> x.Type == ResourceType.Process &&
		// x.id.equals(pick.getParentProcID())))
		{
			parentP = resourcesMap.get(ResourceType.Process).get(pick.getParentProcID());
		} else {

			parentP = new ResourceItem();

			parentP.Type = ResourceType.Process;
			parentP.Number = pick.proc_ppid;
			parentP.id = pick.getParentProcID();
			parentP.Title = pick.proc_pname;
			parentP.Description = "";

		}
		
		theGraph.addVertex(parentP);

		resourcesMap.get(ResourceType.Process).put(parentP.getID(), parentP);
		// theGraph.getVertices().stream()
		// .filter(x -> x.Type == ResourceType.Process &&
		// x.id.equals(pick.getParentProcID())).findFirst()
		// .get();
		ResourceItem tp = TheProc;
		// if (!theGraph.getEdges().stream().anyMatch(x ->
		// x.From.isEqual(parentP) && x.To.isEqual(tp))) {
		if (!EdgeMap.containsKey(parentP.getID() + tp.getID() + "exec")) {

			// add the connection to the process
			AccessCall tempCallItem = new AccessCall();
			tempCallItem.From = parentP;
			tempCallItem.To = TheProc;
			tempCallItem.Command = "exec";
			tempCallItem.user_id = pick.user_uid;
			tempCallItem.user_name = pick.user_name;

			tempCallItem.sequenceNumber = sequenceCounter++;

			theGraph.addEdge(tempCallItem, tempCallItem.From, tempCallItem.To);
			EdgeMap.put(parentP.getID() + tp.getID() + tempCallItem.Command, tempCallItem);
		} else {
			AccessCall t = EdgeMap.get(parentP.getID() + tp.getID() + "exec");
			theGraph.addVertex(t.From);
			theGraph.addEdge(t, t.From, t.To);
		}

		ResourceType ItemType = ResourceType.File;
		switch (pick.fd_typechar.toLowerCase()) {
		case "f":
			ItemType = ResourceType.File;
			break;
		case "4":
			ItemType = ResourceType.NetworkIPV4;
			break;
		case "6":
			ItemType = ResourceType.NetworkIPV6;
			break;
		case "u":
			ItemType = ResourceType.Unix;
			break;
		case "s":
			ItemType = ResourceType.SignalFDs;
			break;
		case "e":
			ItemType = ResourceType.EventFDs;
			break;
		case "i":
			ItemType = ResourceType.iNotifyFDS;
			break;
		case "t":
			ItemType = ResourceType.TimerFDs;
			break;
		case "p":
			ItemType = ResourceType.Pipe;
			break;
		}

		if (!resourcesMap.containsKey(ItemType)) {
			resourcesMap.put(ItemType, new HashMap<String, ResourceItem>());
		}

		// is there an fd resource ?
		if (pick.fd_num != "<NA>" && !resourcesMap.get(ItemType).containsKey(pick.getFD_ID())) {
			ResourceItem tempItem = new ResourceItem();
			// / find type, field types come from SYSDIG fd type definition

			// /end of find type

			tempItem.Type = ItemType;
			tempItem.Number = pick.fd_num;
			tempItem.id = pick.getFD_ID();
			tempItem.Path = pick.fd_directory;
			tempItem.Title = pick.fd_name;
			// tempItem.Description = pick.fd_

			theGraph.addVertex(tempItem);

			resourcesMap.get(ItemType).put(pick.getFD_ID(), tempItem);

			ToItem = tempItem;
		}

		// assert pick.thread_tid != "<NA>";
		if (!pick.fd_num.equals("<NA>")) {
			FromItem = TheProc;

			if (ToItem == null)
				ToItem = resourcesMap.get(ItemType).get(pick.getFD_ID());
			// ToItem = theGraph.getVertices().stream().filter(x ->
			// x.id.equals(pick.getFD_ID())).findFirst().get();

			// create the link item :
			final ResourceItem FF = FromItem;
			final ResourceItem TT = ToItem;

			/*
			 * if there already is an instance of this edge, look to VERBOSE flag, if
			 * verbose flag is set, create a new edge anyways, other wise check if it exists
			 * raise the occirance factor otherwisde insert it
			 */
			if (!isInVerboseMode && EdgeMap.containsKey(FF.getID() + TT.getID() + pick.evt_type))
			// if (!isInVerboseMode &&
			//
			// theGraph.getEdges().stream()
			// .anyMatch(x -> x.Command.equals(pick.evt_type) &&
			// x.From.equals(FF) && x.To.equals(TT))) {

			{

//				theGraph.getEdges().stream()
//						.filter(x -> x.Command.equals(pick.evt_type) && x.From.equals(FF) && x.To.equals(TT))
//						.findFirst().get().OccuranceFactor++;
//
//				

				AccessCall t = EdgeMap.get(FF.getID() + TT.getID() + pick.evt_type);
				t.OccuranceFactor++;

				theGraph.addVertex(t.From);
				theGraph.addVertex(t.To);
				theGraph.addEdge(t, t.From, t.To);

				int a = 12;
			} else {
				// create the edge between resources of start and end
				AccessCall theCall = new AccessCall();
				theCall.From = FromItem;
				theCall.To = ToItem;
				theCall.Command = pick.evt_type;
				theCall.DateTime = pick.evt_time_s;
				theCall.Description = pick.evt_rawres;
				theCall.Info = pick.evt_args;
				theCall.user_id = pick.user_uid;
				theCall.user_name = pick.user_name;
				theCall.sequenceNumber = sequenceCounter++;

				theGraph.addVertex(theCall.From);
				theGraph.addVertex(theCall.To);
				theGraph.addEdge(theCall, theCall.From, theCall.To);
//
				EdgeMap.put(theCall.From.getID() + theCall.To.getID() + theCall.Command, theCall);

			}
		}

	}

	public void AddRowToGraph2(Graph<ResourceItem, AccessCall> theGraph, SysdigRecordObject pick) {
		ResourceItem FromItem = null;
		ResourceItem ToItem = null;
		ResourceItem TheProc = null;

		// is process new ?

		if (!resourcesMap.containsKey(ResourceType.Process)) {
			resourcesMap.put(ResourceType.Process, new HashMap<String, ResourceItem>());
		}

		if (!resourcesMap.get(ResourceType.Process).containsKey(pick.getProcPID())) {
			// if (!theGraph.getVertices().stream()
			// .anyMatch(x -> x.Type == ResourceType.Process &&
			// x.id.equals(pick.getProcPID()))) {
			// add the process
			ResourceItem tempItem = new ResourceItem();

			tempItem.Type = ResourceType.Process;
			tempItem.Number = pick.proc_pid;
			tempItem.id = pick.getProcPID();
			tempItem.Title = pick.proc_name;
			tempItem.Description = pick.proc_args;

			TheProc = tempItem;
			theGraph.addVertex(tempItem);
			// resourcesMap.get(ResourceType.Process).put(pick.getProcPID(),
			// TheProc);
			resourcesMap.get(ResourceType.Process).put(pick.getProcPID(), TheProc);
		} else {
			// TheProc = theGraph.getVertices().stream()
			// .filter(x -> x.Type == ResourceType.Process &&
			// x.id.equals(pick.getProcPID())).findFirst().get();
			TheProc = resourcesMap.get(ResourceType.Process).get(pick.getProcPID());

		}

		if (resourcesMap.get(ResourceType.Process).containsKey(pick.getParentProcID()))
		// if (theGraph.getVertices().stream()
		// .anyMatch(x -> x.Type == ResourceType.Process &&
		// x.id.equals(pick.getParentProcID())))
		{
			ResourceItem parentP = resourcesMap.get(ResourceType.Process).get(pick.getParentProcID());

			// theGraph.getVertices().stream()
			// .filter(x -> x.Type == ResourceType.Process &&
			// x.id.equals(pick.getParentProcID())).findFirst()
			// .get();
			ResourceItem tp = TheProc;
			// if (!theGraph.getEdges().stream().anyMatch(x ->
			// x.From.isEqual(parentP) && x.To.isEqual(tp))) {
			if (!EdgeMap.containsKey(parentP.getID() + tp.getID() + "exec")) {

				// add the connection to the process
				AccessCall tempCallItem = new AccessCall();
				tempCallItem.From = parentP;
				tempCallItem.To = TheProc;
				tempCallItem.Command = "exec";
				tempCallItem.user_id = pick.user_uid;
				tempCallItem.user_name = pick.user_name;

				tempCallItem.sequenceNumber = sequenceCounter++;

				theGraph.addEdge(tempCallItem, tempCallItem.From, tempCallItem.To);
				EdgeMap.put(parentP.getID() + tp.getID() + tempCallItem.Command, tempCallItem);
			}

		}

		ResourceType ItemType = ResourceType.File;
		switch (pick.fd_typechar.toLowerCase()) {
		case "f":
			ItemType = ResourceType.File;
			break;
		case "4":
			ItemType = ResourceType.NetworkIPV4;
			break;
		case "6":
			ItemType = ResourceType.NetworkIPV6;
			break;
		case "u":
			ItemType = ResourceType.Unix;
			break;
		case "s":
			ItemType = ResourceType.SignalFDs;
			break;
		case "e":
			ItemType = ResourceType.EventFDs;
			break;
		case "i":
			ItemType = ResourceType.iNotifyFDS;
			break;
		case "t":
			ItemType = ResourceType.TimerFDs;
			break;
		case "p":
			ItemType = ResourceType.Pipe;
			break;
		}

		if (!resourcesMap.containsKey(ItemType)) {
			resourcesMap.put(ItemType, new HashMap<String, ResourceItem>());
		}

		// is there an fd resource ?
		if (pick.fd_num != "<NA>" && !resourcesMap.get(ItemType).containsKey(pick.getFD_ID())) {
			ResourceItem tempItem = new ResourceItem();
			// / find type, field types come from SYSDIG fd type definition

			// /end of find type

			tempItem.Type = ItemType;
			tempItem.Number = pick.fd_num;
			tempItem.id = pick.getFD_ID();
			tempItem.Path = pick.fd_directory;
			tempItem.Title = pick.fd_name;
			// tempItem.Description = pick.fd_

			theGraph.addVertex(tempItem);

			resourcesMap.get(ItemType).put(pick.getFD_ID(), tempItem);

			ToItem = tempItem;
		}

		// assert pick.thread_tid != "<NA>";
		if (!pick.fd_num.equals("<NA>")) {
			FromItem = TheProc;

			if (ToItem == null)
				ToItem = resourcesMap.get(ItemType).get(pick.getFD_ID());
			// ToItem = theGraph.getVertices().stream().filter(x ->
			// x.id.equals(pick.getFD_ID())).findFirst().get();

			// create the link item :
			final ResourceItem FF = FromItem;
			final ResourceItem TT = ToItem;

			/*
			 * if there already is an instance of this edge, look to VERBOSE flag, if
			 * verbose flag is set, create a new edge anyways, other wise check if it exists
			 * raise the occirance factor otherwisde insert it
			 */
			if (!isInVerboseMode && EdgeMap.containsKey(FF.getID() + TT.getID() + pick.evt_type))
			// if (!isInVerboseMode &&
			//
			// theGraph.getEdges().stream()
			// .anyMatch(x -> x.Command.equals(pick.evt_type) &&
			// x.From.equals(FF) && x.To.equals(TT))) {

			{

//				theGraph.getEdges().stream()
//						.filter(x -> x.Command.equals(pick.evt_type) && x.From.equals(FF) && x.To.equals(TT))
//						.findFirst().get().OccuranceFactor++;
//
//				

				EdgeMap.get(FF.getID() + TT.getID() + pick.evt_type).OccuranceFactor++;

				int a = 12;
			} else {
				// create the edge between resources of start and end
				AccessCall theCall = new AccessCall();
				theCall.From = FromItem;
				theCall.To = ToItem;
				theCall.Command = pick.evt_type;
				theCall.DateTime = pick.evt_time_s;
				theCall.Description = pick.evt_rawres;
				theCall.Info = pick.evt_args;
				theCall.user_id = pick.user_uid;
				theCall.user_name = pick.user_name;
				theCall.sequenceNumber = sequenceCounter++;

				theGraph.addEdge(theCall, theCall.From, theCall.To);
//
				EdgeMap.put(theCall.From.getID() + theCall.To.getID() + theCall.Command, theCall);

			}
		}

	}

	public void release_maps() {
		this.resourcesMap.clear();
		this.EdgeMap.clear();
		this.resourcesMap = null;
		this.EdgeMap = null;
		System.gc();
		System.runFinalization();
	}

	/**
	 * 
	 * @param theGraph
	 * @param theQuery
	 * @return
	 */
	public Graph<ResourceItem, AccessCall> pruneByType(Graph<ResourceItem, AccessCall> theGraph, ParsedQuery theQuery) {

		if (theQuery.getVerticeTypes().size() != 0) {
			// TODO : filter by node type
			pruneVertexType(theGraph, theQuery);
		}
		if (theQuery.getEdgeTypes().size() != 0) {
			// TODO : filter by edge type
			pruneEdgeType(theGraph, theQuery);
		}

		return theGraph;
	}

	/**
	 * removes the nodes that do not cokmply with fiokterd types
	 * 
	 * @param theGraph the graph to run the filter on
	 * @param theQuery the Query from which the filters are comming
	 * @return the graph with pruned types
	 */
	public void pruneVertexType(Graph<ResourceItem, AccessCall> theGraph, ParsedQuery theQuery) {

		ArrayList<ResourceItem> toRemove = new ArrayList<ResourceItem>();
		for (ResourceItem pick : theGraph.getVertices()) {
			if (!theQuery.getVerticeTypes().contains(pick.Type))
				toRemove.add(pick);
		}
		for (ResourceItem pick : toRemove)
			theGraph.removeVertex(pick);

//		return theGraph;
	}

	/**
	 * removes the edges which do not comply to the givenh query
	 * 
	 * @param theGraph the graph on which the matching happens
	 * @param theQuery the query where filters are comming from
	 */
	public void pruneEdgeType(Graph<ResourceItem, AccessCall> theGraph, ParsedQuery theQuery) {

		ArrayList<AccessCall> toRemove = new ArrayList<AccessCall>();
		for (AccessCall pick : theGraph.getEdges()) {
			if (!theQuery.getEdgeTypes().contains(pick.Command))
				toRemove.add(pick);
		}
		for (AccessCall pick : toRemove)
			theGraph.removeEdge(pick);

//		return theGraph;
	}

	/**
	 * merges the second graph into the first one
	 * 
	 * @param to   the graph in which the other one will be merged
	 * @param from the graph which will be merged into the other one
	 */
	public void mergeGraphs(Graph<ResourceItem, AccessCall> to, Graph<ResourceItem, AccessCall> from) {

		for (ResourceItem pick : from.getVertices()) {
			to.addVertex(pick);
		}
		for (AccessCall pick : EdgeMap.values()) {
			if (to.containsVertex(pick.From) && to.containsVertex(pick.To))
				to.addEdge(pick, pick.From, pick.To);
		}

	}

}