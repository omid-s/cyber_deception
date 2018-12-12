package querying.tools;

import java.util.ArrayList;

import classes.AccessCall;
import classes.ResourceItem;
import edu.uci.ics.jung.graph.Graph;
import querying.parsing.ParsedQuery;

public class GraphQueryTools {

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

}
