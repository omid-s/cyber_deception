package helpers;

import java.util.*;

import classes.*;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseMultigraph;
import exceptions.QueryFormatException;
import querying.ParsedQuery;
import querying.QueryInterpreter;
import querying.adapters.memory.InMemoryAdapter;
import querying.parsing.Criteria;
import querying.tools.EnumTools;

/**
 * @author omido
 *
 */
public class GraphQueryModel {

	public Graph<ResourceItem, AccessCall> RunQuety(String input, Graph<ResourceItem, AccessCall> oldGraph)
			throws QueryFormatException {

		ParsedQuery query = null;
		try {
			query = QueryInterpreter.interpret(input, oldGraph);
		} catch (Exception ex) {
		}

		InMemoryAdapter mem = InMemoryAdapter.getSignleton();

		return mem.getSubGraph(query);
	}

}
