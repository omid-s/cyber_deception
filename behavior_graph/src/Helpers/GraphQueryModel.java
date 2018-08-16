package Helpers;

import java.util.*;

import com.mysql.fabric.xmlrpc.base.Array;

import Classes.*;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseMultigraph;

/**
 * @author omido
 *
 */
public class GraphQueryModel {

	public Graph<ResourceItem, AccessCall> RunQuety(String input, Graph<ResourceItem, AccessCall> oldGraph) {

		BaseMemory mem = BaseMemory.getSignleton();

		input = input.trim().toLowerCase();
		if (input.length() < 5)
			return oldGraph;

		boolean doesAppend = input.endsWith(";");

		if (doesAppend)
			input = input.substring(0, input.length() - 2);

		boolean isVerbose = input.startsWith("verbose");

		int selectIndex = input.indexOf("select") + "select".length();
		int fromIndex = input.indexOf("from") + "from".length();
		int whereIndex = input.indexOf("where"); 
		if ( whereIndex > 0  )
			whereIndex += "where".length();

		String types = "";

		/// ----- tokenize the vertice types
		System.out.print( whereIndex );
		if (whereIndex == -1) {
			types = input.substring(fromIndex);
		} else
			types = input.substring(fromIndex, whereIndex - "where".length());
		ArrayList<ResourceType> resourceTypes = new ArrayList<ResourceType>();
		
		if (!types.trim().equals("*"))
			for (String pick : types.split(",")) {
				ResourceType t = searchEnum(ResourceType.class, pick.trim());
				if (t != null)
					resourceTypes.add(t);
			}

		//// tokenize the edge types
		types = input.substring(selectIndex, fromIndex-"from".length());

		ArrayList<String> callTypes = new ArrayList<String>();
		if (!types.trim().equals("*"))
			for (String pick : types.split(",")) {
				callTypes.add(pick.trim());
			}

		/// extract the criterias :
		ArrayList<Criteria> criterias = new ArrayList<Criteria>();
		if (whereIndex > 0) {
			String temp = input.substring(whereIndex);
			String tokens[] = temp.split(",also,");

			for (int i = 0; i < tokens.length; i++) {
				String t1[] = tokens[i].trim().split(" ");

				criterias.add(new Criteria(t1[0].trim().toLowerCase(), t1[1].trim().toLowerCase(),
						tokens[i].substring( t1[1].length() + tokens[i].indexOf(t1[1]) ).trim() ));
			}
		}
		//
		// new ArrayList<Criteria>(Arrays
		// .asList(new Criteria("pid", "has", "1195"), new Criteria("pid", "is",
		// "312 / Main Process"))
		return mem.getSubGraph(resourceTypes, callTypes, isVerbose, criterias, doesAppend ? oldGraph : null);
	}

	public static <T extends Enum<?>> T searchEnum(Class<T> enumeration, String search) {
		for (T each : enumeration.getEnumConstants()) {
			if (each.name().compareToIgnoreCase(search) == 0) {
				return each;
			}
		}
		return null;
	}
}
