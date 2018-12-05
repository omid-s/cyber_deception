package helpers;

import java.util.*;

import classes.*;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseMultigraph;
import exceptions.QueryFormatException;
import querying.ParsedQuery;
import querying.adapters.memory.BaseMemory;
import querying.parsing.Criteria;
import querying.tools.EnumTools;

/**
 * @author omido
 *
 */
public class GraphQueryModel {

	public Graph<ResourceItem, AccessCall> RunQuety(String input, Graph<ResourceItem, AccessCall> oldGraph)
			throws QueryFormatException {

		BaseMemory mem = BaseMemory.getSignleton();

		input = input.trim().toLowerCase();
		if (input.length() < 5)
			return oldGraph;

		boolean doesAppend = input.endsWith(";");

		if (doesAppend)
			input = input.substring(0, input.length() - 2);

		int selectIndex = input.indexOf("select") + "select".length();
		int fromIndex = input.indexOf("from") + "from".length();
		int whereIndex = input.indexOf("where");
		if (whereIndex > 0)
			whereIndex += "where".length();

		boolean isVerbose = input.contains("verbose") && input.indexOf("verbose") < selectIndex;
		boolean isBackTracked = input.contains("back") && input.indexOf("back") < selectIndex;
		boolean isForwardTracked = input.contains("forward") && input.indexOf("forward") < selectIndex;
		String types = "";

		/// ----- tokenize the vertice types

		if (whereIndex == -1) {
			types = input.substring(fromIndex);
		} else
			types = input.substring(fromIndex, whereIndex - "where".length());
		ArrayList<ResourceType> resourceTypes = new ArrayList<ResourceType>();

		if (!types.trim().equals("*"))
			for (String pick : types.split(",")) {
				if (pick.trim().toLowerCase().equals("soc")) {
					resourceTypes.add(ResourceType.NetworkIPV4);
					resourceTypes.add(ResourceType.NetworkIPV6);
				} else {
					ResourceType t = EnumTools.searchEnum(ResourceType.class, pick.trim());
					if (t != null)
						resourceTypes.add(t);
				}
			}

		//// tokenize the edge types
		types = input.substring(selectIndex, fromIndex - "from".length());

		ArrayList<String> callTypes = new ArrayList<String>();
		if (!types.trim().equals("*"))
			for (String pick : types.split(",")) {
				callTypes.add(pick.trim());
			}

		/// extract the criterias :
		ArrayList<Criteria> criterias = new ArrayList<Criteria>();
		if (whereIndex > 0) {
			String temp = input.substring(whereIndex);
			/*
			 * criterias are seperated by ",also," tokens, they need to be seerated before
			 * each token can be processed into a criteria object which then will be
			 * appplied to the filter
			 * 
			 */
			String tokens[] = temp.split(",also,");

			for (int i = 0; i < tokens.length; i++) {
				String t1[] = tokens[i].trim().split(" ");

				if (t1.length == 3)
					criterias.add(new Criteria(t1[0].trim().toLowerCase(), t1[1].trim().toLowerCase(),
							tokens[i].substring(t1[1].length() + tokens[i].indexOf(t1[1])).trim()));
				else {
					ArrayList<ResourceType> cRtypes = new ArrayList<ResourceType>();
					String type = t1[0];
					for (String pick : type.split(",")) {
						// if any is desired skip this
						if (pick.equalsIgnoreCase("any"))
							break;

						if (pick.trim().toLowerCase().equals("soc")) {
							cRtypes.add(ResourceType.NetworkIPV4);
							cRtypes.add(ResourceType.NetworkIPV6);
						} else {
							ResourceType t = EnumTools.searchEnum(ResourceType.class, pick.trim());
							if (t != null)
								cRtypes.add(t);
						}
					}

					criterias.add(new Criteria(cRtypes, t1[1].trim().toLowerCase(), t1[2].trim().toLowerCase(),
							tokens[i].substring(t1[2].length() + tokens[i].indexOf(t1[2])).trim()));
				}
			}
		}

		ParsedQuery query = new ParsedQuery(resourceTypes, callTypes, isVerbose, isBackTracked, isForwardTracked,
				criterias, doesAppend ? oldGraph : null, doesAppend);

		return mem.getSubGraph(query);
	}

}