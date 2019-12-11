package querying.tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Vector;

import javax.vecmath.GVector;

import org.neo4j.driver.internal.shaded.io.netty.channel.ThreadPerChannelEventLoop;

import classes.AccessCall;
import classes.ResourceItem;
import classes.ResourceType;
import classes.SysdigRecordObject;
import classes.SysdigRecordObjectGraph;
import controlClasses.Configurations;
import edu.uci.ics.jung.graph.DirectedOrderedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
import insertion.ShadowInserter;
import insertion.graph.ShadowNeo4JInserter;
import querying.adapters.memory.InMemoryAdapter;
import querying.parsing.ParsedQuery;
import readers.SysdigObjectDAL;

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

			ResourceItem tempItem = new ResourceItem();

			tempItem.Type = ResourceType.Process;
			tempItem.Number = pick.proc_pid;
			tempItem.id = pick.getProcPID();
			tempItem.Title = pick.proc_name;
			tempItem.Description = pick.proc_args;
			tempItem.computer_id = pick.Computer_id;

			TheProc = tempItem;
			theGraph.addVertex(tempItem);

			InMemoryAdapter.getSignleton().addResourceItem(tempItem);
			
			if (Boolean.valueOf(Configurations.getInstance().getSetting(Configurations.SHADOW_INSERTER))) {
				ShadowInserter.theInserter.insertNode(tempItem);
			}

			resourcesMap.get(ResourceType.Process).put(pick.getProcPID(), TheProc);
		} else {
			TheProc = resourcesMap.get(ResourceType.Process).get(pick.getProcPID());

			if ((TheProc.Title.isEmpty() || TheProc.Title.equals("<NA>")) && pick.proc_name != null
					&& !pick.proc_name.equals("<NA>") && !pick.proc_name.isEmpty()) {
				TheProc.Title = pick.proc_name;
				System.out.println("*");
			}
		}

		theGraph.addVertex(TheProc);
		InMemoryAdapter.getSignleton().addResourceItem(TheProc);
		// is thread new?
		if (!resourcesMap.containsKey(ResourceType.Thread)) {
			resourcesMap.put(ResourceType.Thread, new HashMap<String, ResourceItem>());
		}

		ResourceItem TheThread = null;

		if (!resourcesMap.get(ResourceType.Thread).containsKey(pick.getTID())) {

			ResourceItem tempItem = new ResourceItem();

			tempItem.Type = ResourceType.Thread;
			tempItem.Number = pick.thread_tid;
			tempItem.id = pick.getTID();
			tempItem.Title = "-";
			tempItem.Description = "-";
			tempItem.computer_id = pick.Computer_id;

			TheThread = tempItem;
			theGraph.addVertex(tempItem);

			if (Boolean.valueOf(Configurations.getInstance().getSetting(Configurations.SHADOW_INSERTER))) {
				ShadowInserter.theInserter.insertNode(tempItem);
			}

			resourcesMap.get(ResourceType.Thread).put(pick.getTID(), TheThread);
		} else {
			TheThread = resourcesMap.get(ResourceType.Thread).get(pick.getTID());
		}

		// add the thread edge if it does not exist already
		if (!EdgeMap.containsKey(TheProc.getHashID() + TheThread.id + "spawn")) {

			// add the connection to the process
			AccessCall tempCallItem = new AccessCall();
			tempCallItem.From = TheProc;
			tempCallItem.To = TheThread;
			tempCallItem.Command = "spawn";
			tempCallItem.user_id = pick.user_uid;
			tempCallItem.user_name = pick.user_name;
			tempCallItem.computer_id = pick.Computer_id;
			tempCallItem.sequenceNumber = sequenceCounter++;

			theGraph.addEdge(tempCallItem, tempCallItem.From, tempCallItem.To);

			if (Boolean.valueOf(Configurations.getInstance().getSetting(Configurations.SHADOW_INSERTER))) {
				ShadowInserter.theInserter.insertEdge(tempCallItem);
			}

			EdgeMap.put(TheProc.getHashID() + TheThread.id + "spawn", tempCallItem);
		} else {
			AccessCall t = EdgeMap.get(TheProc.getHashID() + TheThread.id + "spawn");
			theGraph.addVertex(t.From);
			 
			theGraph.addEdge(t, t.From, t.To);
			
			InMemoryAdapter.getSignleton().addResourceItem(t.To);
			InMemoryAdapter.getSignleton().addResourceItem(t.From);
			InMemoryAdapter.getSignleton().addAccessCall(t);
		}

		// is UBSI unit new?
		if (!resourcesMap.containsKey(ResourceType.EXEUnit)) {
			resourcesMap.put(ResourceType.EXEUnit, new HashMap<String, ResourceItem>());
		}

		ResourceItem TheUBSI = null;

		if (!resourcesMap.get(ResourceType.EXEUnit).containsKey(pick.getUBSIID())) {

			ResourceItem tempItem = new ResourceItem();

			tempItem.Type = ResourceType.EXEUnit;
			tempItem.Number = pick.ubsi_unit_id;
			tempItem.id = pick.getUBSIID();
			tempItem.Title = "-";
			tempItem.Description = "-";
			tempItem.computer_id = pick.Computer_id;

			TheUBSI = tempItem;
			theGraph.addVertex(tempItem);
			InMemoryAdapter.getSignleton().addResourceItem(tempItem);
			
			if (Boolean.valueOf(Configurations.getInstance().getSetting(Configurations.SHADOW_INSERTER))) {
				ShadowInserter.theInserter.insertNode(tempItem);
			}

			resourcesMap.get(ResourceType.EXEUnit).put(pick.getUBSIID(), TheUBSI);
		} else {
			TheUBSI = resourcesMap.get(ResourceType.EXEUnit).get(pick.getUBSIID());
		}

		// add the thread edge if it does not exist already
		if (!EdgeMap.containsKey(TheThread.id + TheUBSI.id + "started")) {

			// add the connection to the process
			AccessCall tempCallItem = new AccessCall();
			tempCallItem.From = TheThread;
			tempCallItem.To = TheUBSI;
			tempCallItem.Command = "started";
			tempCallItem.user_id = pick.user_uid;
			tempCallItem.user_name = pick.user_name;
			tempCallItem.computer_id = pick.Computer_id;
			tempCallItem.sequenceNumber = sequenceCounter++;

			theGraph.addEdge(tempCallItem, tempCallItem.From, tempCallItem.To);
			
			InMemoryAdapter.getSignleton().addResourceItem(tempCallItem.To);
			InMemoryAdapter.getSignleton().addResourceItem(tempCallItem.From);
			InMemoryAdapter.getSignleton().addAccessCall(tempCallItem);
			
			if (Boolean.valueOf(Configurations.getInstance().getSetting(Configurations.SHADOW_INSERTER))) {
				ShadowInserter.theInserter.insertEdge(tempCallItem);
			}

			EdgeMap.put(TheThread.id + TheUBSI.id + "started", tempCallItem);
		} else {
			AccessCall t = EdgeMap.get(TheThread.id + TheUBSI.id + "started");
			theGraph.addVertex(t.From);
			
			theGraph.addEdge(t, t.From, t.To);
			
			InMemoryAdapter.getSignleton().addResourceItem(t.To);
			InMemoryAdapter.getSignleton().addResourceItem(t.From);
			InMemoryAdapter.getSignleton().addAccessCall(t);
		}

		ResourceItem parentP = null;

		if (resourcesMap.get(ResourceType.Process).containsKey(pick.getParentProcID())) {
			parentP = resourcesMap.get(ResourceType.Process).get(pick.getParentProcID());
		} else {

			parentP = new ResourceItem();
			parentP.Type = ResourceType.Process;
			parentP.Number = pick.proc_ppid;
			parentP.id = pick.getParentProcID();
			parentP.Title = pick.proc_pname;
			parentP.Description = "";
			parentP.computer_id = pick.Computer_id;
		}

		theGraph.addVertex(parentP);
		InMemoryAdapter.getSignleton().addResourceItem(parentP);
		
		resourcesMap.get(ResourceType.Process).put(parentP.id, parentP);

		ResourceItem tp = TheProc;

		if (!EdgeMap.containsKey(parentP.getID() + tp.getID() + "exec")) {

			// add the connection to the process
			AccessCall tempCallItem = new AccessCall();
			tempCallItem.From = parentP;
			tempCallItem.To = TheProc;
			tempCallItem.Command = "exec";
			tempCallItem.user_id = pick.user_uid;
			tempCallItem.user_name = pick.user_name;
			tempCallItem.computer_id = pick.Computer_id;
			tempCallItem.sequenceNumber = sequenceCounter++;

			theGraph.addEdge(tempCallItem, tempCallItem.From, tempCallItem.To);

			InMemoryAdapter.getSignleton().addResourceItem(tempCallItem.To);
			InMemoryAdapter.getSignleton().addResourceItem(tempCallItem.From);
			InMemoryAdapter.getSignleton().addAccessCall(tempCallItem);
			
			if (Boolean.valueOf(Configurations.getInstance().getSetting(Configurations.SHADOW_INSERTER))) {
				ShadowInserter.theInserter.insertEdge(tempCallItem);
			}

			EdgeMap.put(parentP.getID() + tp.getID() + tempCallItem.Command, tempCallItem);
		} else {
			AccessCall t = EdgeMap.get(parentP.getID() + tp.getID() + "exec");
			theGraph.addVertex(t.From);
			 
			theGraph.addEdge(t, t.From, t.To);
			
			InMemoryAdapter.getSignleton().addResourceItem(t.To);
			InMemoryAdapter.getSignleton().addResourceItem(t.From);
			InMemoryAdapter.getSignleton().addAccessCall(t);
		}

		ResourceType ItemType = ResourceType.File;
		switch (pick.fd_typechar.toLowerCase().trim()) {
		case "f":
			ItemType = ResourceType.File;
			break;
		case "4":
		case "ipv4":
			ItemType = ResourceType.NetworkIPV4;
			break;
		case "ipv6":
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

		
		// to handle system calls like chmod 
//		if(!pick.fd_num.equals("<NA>") ) {
//			pick.fd_name= pick.fd_num;
//			
//			pick.fd_directory = pick.fd_name;
////			ItemType= ResourceType.File;
//		}
//		else {
//			pick.fd_num ="1" ;
//		}
		
		// is there an fd resource ?
		if (!pick.fd_num.equals( "<NA>") && !resourcesMap.get(ItemType).containsKey(pick.getFD_ID())) {
			ResourceItem tempItem = new ResourceItem();

			tempItem.Type = ItemType;
			tempItem.Number = pick.fd_num;
			tempItem.id = pick.getFD_ID();
			tempItem.Path = pick.fd_directory;
			tempItem.Title = pick.fd_name;
			tempItem.computer_id = pick.Computer_id;
			// tempItem.Description = pick.fd_

			theGraph.addVertex(tempItem);
			InMemoryAdapter.getSignleton().addResourceItem(tempItem);

			if (Boolean.valueOf(Configurations.getInstance().getSetting(Configurations.SHADOW_INSERTER))) {
				ShadowInserter.theInserter.insertNode(tempItem);
			}

			resourcesMap.get(ItemType).put(pick.getFD_ID(), tempItem);
			ToItem = tempItem;
		}

		// assert pick.thread_tid != "<NA>";
		if (!pick.fd_num.equals("<NA>")) {
			FromItem = TheUBSI;

			if (ToItem == null)
				ToItem = resourcesMap.get(ItemType).get(pick.getFD_ID());

			// create the link item :
			final ResourceItem FF = FromItem;
			final ResourceItem TT = ToItem;

			/*
			 * if there already is an instance of this edge, look to VERBOSE flag, if
			 * verbose flag is set, create a new edge anyways, other wise check if it exists
			 * raise the occirance factor otherwisde insert it
			 */
			if (!isInVerboseMode && EdgeMap.containsKey(FF.getID() + TT.getID() + pick.evt_type)) {

				AccessCall t = EdgeMap.get(FF.getID() + TT.getID() + pick.evt_type);
				t.OccuranceFactor++;
				sequenceCounter++;
				boolean shouldUpdate = t.addTime(sequenceCounter,
						Integer.parseInt(Configurations.getInstance().getSetting(Configurations.COMPRESSSION_LEVEL)));

				if (shouldUpdate
						&& Boolean.valueOf(Configurations.getInstance().getSetting(Configurations.SHADOW_INSERTER))) {
					ShadowInserter.theInserter.setEdgeForUpdate(t);
				}

				theGraph.addVertex(t.From);
				theGraph.addVertex(t.To);
					theGraph.addEdge(t, t.From, t.To);
					
				InMemoryAdapter.getSignleton().addResourceItem(t.To);
				InMemoryAdapter.getSignleton().addResourceItem(t.From);
				InMemoryAdapter.getSignleton().addAccessCall(t);
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
				theCall.addTime(sequenceCounter,
						Integer.parseInt(Configurations.getInstance().getSetting(Configurations.COMPRESSSION_LEVEL)));
				theCall.computer_id = pick.Computer_id;
				theGraph.addVertex(theCall.From);
				theGraph.addVertex(theCall.To);
				
				InMemoryAdapter.getSignleton().addResourceItem(theCall.From);
				InMemoryAdapter.getSignleton().addResourceItem(theCall.To);
				
				theGraph.addEdge(theCall, theCall.From, theCall.To);
				InMemoryAdapter.getSignleton().addAccessCall(theCall);
//

				if (Boolean.valueOf(Configurations.getInstance().getSetting(Configurations.SHADOW_INSERTER))) {
					ShadowInserter.theInserter.insertEdge(theCall);
				}

				EdgeMap.put(theCall.From.getID() + theCall.To.getID() + theCall.Command, theCall);

			}
		}

	}

	/**
	 * Based on process and file descriptor fields create nodes and vertices that
	 * correspond to the call record then add them to the graph supplied
	 * 
	 * @param theGraph the graph to add nodes and edges to
	 * @param pick     the row object to be processed
	 */

	public void AddRowToGraph(Graph<ResourceItem, AccessCall> theGraph, SysdigRecordObjectGraph pick) {
		ResourceItem FromItem = null;
		ResourceItem ToItem = null;
		ResourceItem TheProc = null;
		ResourceItem TheThread = null;
		ResourceItem TheUBSI = null;

		// is process new ?
		if (!resourcesMap.containsKey(ResourceType.Process)) {
			resourcesMap.put(ResourceType.Process, new HashMap<String, ResourceItem>());
		}

		if (!resourcesMap.get(ResourceType.Process).containsKey(pick.getProc().id)) {
			TheProc = pick.getProc();
			theGraph.addVertex(TheProc);
			resourcesMap.get(ResourceType.Process).put(pick.getProc().id, TheProc);
		} else {
			TheProc = resourcesMap.get(ResourceType.Process).get(pick.getProc().id);
		}

		theGraph.addVertex(TheProc);

		if (pick.getParentProc() != null) {
			ResourceItem parentP = null;
			if (resourcesMap.get(ResourceType.Process).containsKey(pick.getParentProc().id)) {
				parentP = resourcesMap.get(ResourceType.Process).get(pick.getParentProc().id);
			} else {

				parentP = pick.getParentProc();
			}
			theGraph.addVertex(parentP);
			resourcesMap.get(ResourceType.Process).put(parentP.id, parentP);

			if (!EdgeMap.containsKey(parentP.getID() + TheProc.getID() + "exec")) {
				pick.getExec().From = parentP;
				pick.getExec().To = TheProc;
				theGraph.addEdge(pick.getExec(), parentP, TheProc);
				InMemoryAdapter.getSignleton().addAccessCall(pick.getExec());
				
				EdgeMap.put(parentP.getID() + TheProc.getID() + pick.getExec().Command, pick.getExec());
			} else {
				AccessCall t = EdgeMap.get(parentP.getID() + TheProc.getID() + "exec");
				theGraph.addVertex(t.From);
				theGraph.addEdge(t, t.From, t.To);
				
				InMemoryAdapter.getSignleton().addAccessCall(t);
				InMemoryAdapter.getSignleton().addResourceItem(t.From);
				InMemoryAdapter.getSignleton().addResourceItem(t.To);
				
			}
		}

		if (!resourcesMap.containsKey(pick.getItem().Type)) {
			resourcesMap.put(pick.getItem().Type, new HashMap<String, ResourceItem>());
		}

		// is there an fd resource ?
		if (pick.getItem() != null && !resourcesMap.get(pick.getItem().Type).containsKey(pick.getItem().id)) {
			theGraph.addVertex(pick.getItem());

			resourcesMap.get(pick.getItem().Type).put(pick.getItem().id, pick.getItem());

			ToItem = pick.getItem();
		}

		// assert pick.thread_tid != "<NA>";
		if (pick.getItem() != null) {
			FromItem = TheProc;

			if (ToItem == null)
				ToItem = resourcesMap.get(pick.getItem().Type).get(pick.getItem().id);

			/*
			 * if there already is an instance of this edge, look to VERBOSE flag, if
			 * verbose flag is set, create a new edge anyways, other wise check if it exists
			 * raise the occirance factor otherwisde insert it
			 */
			if (!isInVerboseMode
					&& EdgeMap.containsKey(FromItem.getID() + ToItem.getID() + pick.getSyscall().Command)) {

				AccessCall t = EdgeMap.get(FromItem.getID() + ToItem.getID() + pick.getSyscall().Command);
				t.OccuranceFactor++;

				theGraph.addVertex(t.From);
				theGraph.addVertex(t.To);
				theGraph.addEdge(t, t.From, t.To);

			} else {
				// create the edge between resources of start and end
				AccessCall theCall = pick.getSyscall();

				theCall.From = TheProc;
				theCall.To = ToItem;

				theGraph.addVertex(theCall.From);
				theGraph.addVertex(theCall.To);
				theGraph.addEdge(theCall, theCall.From, theCall.To);

				
				InMemoryAdapter.getSignleton().addResourceItem(theCall.From);
				InMemoryAdapter.getSignleton().addResourceItem(theCall.To);
				InMemoryAdapter.getSignleton().addAccessCall(theCall);
				
				EdgeMap.put(theCall.From.getID() + theCall.To.getID() + theCall.Command, theCall);

			}
		}

	}

	/**
	 * adds the input triple to the graph
	 * 
	 * @param theGraph the grapoh to add nodes to
	 * @param From     starting node
	 * @param To       ending node
	 * @param call     the edge between them
	 */
	public void AddRowToGraph(Graph<ResourceItem, AccessCall> theGraph, ResourceItem From, ResourceItem To,
			AccessCall call) {

		// is from type new?
		if (!resourcesMap.containsKey(From.Type)) {
			resourcesMap.put(From.Type, new HashMap<String, ResourceItem>());
		}

		if (!resourcesMap.get(From.Type).containsKey(From.getHashID())) {
			theGraph.addVertex(From);
			resourcesMap.get(From.Type).put(From.getHashID(), From);
		} else {
			From = resourcesMap.get(From.Type).get(From.getHashID());
		}

		theGraph.addVertex(From);

		// is to type new ?
		if (!resourcesMap.containsKey(To.Type)) {
			resourcesMap.put(To.Type, new HashMap<String, ResourceItem>());
		}

		if (!resourcesMap.get(To.Type).containsKey(To.getHashID())) {
			theGraph.addVertex(To);
			resourcesMap.get(To.Type).put(To.getHashID(), To);
		} else {
			To = resourcesMap.get(To.Type).get(To.getHashID());
		}

		theGraph.addVertex(To);

		/*
		 * if there already is an instance of this edge, look to VERBOSE flag, if
		 * verbose flag is set, create a new edge anyways, other wise check if it exists
		 * raise the occirance factor otherwisde insert it
		 */
		if (!isInVerboseMode && EdgeMap.containsKey(From.getHashID() + To.getHashID() + call.Command)) {

			AccessCall t = EdgeMap.get(From.getHashID() + To.getHashID() + call.Command);
			t.OccuranceFactor++;

			theGraph.addVertex(t.From);
			theGraph.addVertex(t.To);
			theGraph.addEdge(t, t.From, t.To);
			
			InMemoryAdapter.getSignleton().addResourceItem(t.To);
			InMemoryAdapter.getSignleton().addResourceItem(t.From);
			InMemoryAdapter.getSignleton().addAccessCall(t);
			

		} else {
			// create the edge between resources of start and end
			AccessCall theCall = call;

			theCall.From = From;
			theCall.To = To;
			theGraph.addVertex(theCall.From);
			theGraph.addVertex(theCall.To);
			theGraph.addEdge(theCall, theCall.From, theCall.To);

			InMemoryAdapter.getSignleton().addResourceItem(theCall.To);
			InMemoryAdapter.getSignleton().addResourceItem(theCall.From);
			InMemoryAdapter.getSignleton().addAccessCall(theCall);
			
			
			EdgeMap.put(theCall.From.getHashID() + theCall.To.getHashID() + theCall.Command, theCall);

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

	public Graph<ResourceItem, AccessCall> mergeGraphs2(Graph<ResourceItem, AccessCall> to,
			Graph<ResourceItem, AccessCall> from) {
		// to.getEdges().clear();
		Graph<ResourceItem, AccessCall> ret = new DirectedOrderedSparseMultigraph<ResourceItem, AccessCall>();

		for (ResourceItem pick : from.getVertices()) {
			ret.addVertex(pick);
		}
		for (ResourceItem pick : to.getVertices()) {
			ret.addVertex(pick);
		}

		for (AccessCall pick : EdgeMap.values()) {

			if (ret.containsVertex(pick.From) && ret.containsVertex(pick.To))
				ret.addEdge(pick, pick.From, pick.To);

		}
		return ret;

	}

	/**
	 * This method turns a sysdig record object into a small graph represnting the
	 * ojecrt this graph can have from 2 to 3 nodes and one or two edges
	 * 
	 * @param input the sysdig record objec tto create nodes or
	 * @return the graph containng the graph represntation of the object
	 */
	public static SysdigRecordObjectGraph getGraphFromRecord(SysdigRecordObject pick) {
//TODO : fix the seqCounters
		ResourceItem ToItem = null;
		AccessCall theCall = null;

		// create the main process
		ResourceItem tempItem = new ResourceItem();

		tempItem.Type = ResourceType.Process;
		tempItem.Number = pick.proc_pid;
		tempItem.id = pick.getProcPID();
		tempItem.Title = pick.proc_name;
		tempItem.Description = pick.proc_args;
		tempItem.computer_id = pick.Computer_id;

		ResourceItem TheProc = tempItem;
//		ret.addVertex(tempItem);

		ResourceItem parentP = new ResourceItem();
		parentP.Number = pick.proc_ppid;
		parentP.id = pick.getParentProcID();
		parentP.Title = pick.proc_pname;
		parentP.Type = ResourceType.Process;
		parentP.computer_id = pick.Computer_id;
//		ret.addVertex(parentP);

		// add the call between process and process parent
		AccessCall tempCallItem = new AccessCall();
		tempCallItem.From = parentP;
		tempCallItem.To = TheProc;
		tempCallItem.Command = "exec";
		tempCallItem.user_id = pick.user_uid;
		tempCallItem.user_name = pick.user_name;
		tempCallItem.computer_id = pick.Computer_id;
		tempCallItem.sequenceNumber = pick.evt_num != null ? Long.valueOf(pick.evt_num) : 0;

		ResourceItem TheThread = new ResourceItem();

		TheThread.Type = ResourceType.Thread;
		TheThread.Number = pick.thread_tid;
		TheThread.id = pick.getTID();
		TheThread.Title = "-";
		TheThread.Description = "-";
		TheThread.computer_id = pick.Computer_id;
		ResourceItem TheUBSI = new ResourceItem();

		TheUBSI.Type = ResourceType.EXEUnit;
		TheUBSI.Number = pick.ubsi_unit_id;
		TheUBSI.id = pick.getUBSIID();
		TheUBSI.Title = "-";
		TheUBSI.Description = "-";
		TheUBSI.computer_id = pick.Computer_id;
		AccessCall TheSpawn = new AccessCall();
		TheSpawn.From = TheProc;
		TheSpawn.To = TheThread;
		TheSpawn.Command = "spawn";
		TheSpawn.user_id = pick.user_uid;
		TheSpawn.user_name = pick.user_name;
		TheSpawn.computer_id = pick.Computer_id;
		TheSpawn.sequenceNumber = pick.evt_num != null ? Long.valueOf(pick.evt_num) : 0;

		AccessCall TheUBSIStart = new AccessCall();
		TheUBSIStart.From = TheThread;
		TheUBSIStart.To = TheUBSI;
		TheUBSIStart.Command = "started";
		TheUBSIStart.user_id = pick.user_uid;
		TheUBSIStart.user_name = pick.user_name;
		TheUBSIStart.computer_id = pick.Computer_id;
		TheUBSIStart.sequenceNumber = pick.evt_num != null ? Long.valueOf(pick.evt_num) : 0;

//		ret.addEdge(tempCallItem, tempCallItem.From, tempCallItem.To);

		// is there an fd resource ? if so add it the graph, other wise skip it
		if (!pick.fd_num.equals("<NA>")) {

			// find out the fd type
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

			// add the fd item
			ToItem = new ResourceItem();

			ToItem.Type = ItemType;
			ToItem.Number = pick.fd_num;
			ToItem.id = pick.getFD_ID();
			ToItem.Path = pick.fd_directory;
			ToItem.Title = pick.fd_name;
			ToItem.computer_id = pick.Computer_id;
//			ret.addVertex(ToItem);

			// add the edge connecting the FD and the process
			theCall = new AccessCall();
			theCall.From = TheUBSI;
			theCall.To = ToItem;
			theCall.Command = pick.evt_type;
			theCall.DateTime = pick.evt_time_s;
			theCall.Description = pick.evt_rawres;
			theCall.Info = pick.evt_args;
			theCall.user_id = pick.user_uid;
			theCall.user_name = pick.user_name;
			theCall.sequenceNumber = pick.evt_num != null ? Long.valueOf(pick.evt_num) : 0;
			theCall.computer_id = pick.Computer_id;
//			ret.addEdge(theCall, theCall.From, theCall.To);

		}

		return new SysdigRecordObjectGraph(TheProc, parentP, tempCallItem, theCall, ToItem, TheThread, TheSpawn,
				TheUBSI, TheUBSIStart);

	}

}
