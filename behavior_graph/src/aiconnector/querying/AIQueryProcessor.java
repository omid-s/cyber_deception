/**
 * this file contains implementatino for the async query processor for the AI connector
 * 
 * This class is very similar  to querying.QueryProcessor 
 */
package aiconnector.querying;

import java.util.HashMap;

import org.junit.experimental.theories.Theories;

import classes.AccessCall;
import classes.ResourceItem;
import controlClasses.Configurations;
import edu.uci.ics.jung.graph.Graph;
import helpers.ColorHelpers;
import querying.AsyncQueryRunner;

public class AIQueryProcessor implements Runnable {

	public Long num_edges, num_vertex;
	Graph<ResourceItem, AccessCall> theGraph;
	boolean ShowGraph;
	boolean ShowVerbose;

	public AIQueryProcessor(boolean MemQuery, Long num_edges, Long num_vertex,
			Graph<ResourceItem, AccessCall> theGraph) {

		this.num_edges = num_edges;
		this.num_vertex = num_vertex;
		this.theGraph = theGraph;
	}

	public AIQueryProcessor() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void run() {

		HashMap<Integer, String> queries = Configurations.getInstance().getQueries();

		for (Integer query_id : queries.keySet()) {
			{
				String command = queries.get(query_id);

				try {

					AsyncQueryRunner asqr = new AsyncQueryRunner(query_id, command + " ", false);
					asqr.setAIConnector(true);

					Thread T = new Thread(asqr);
					T.start();
					System.out.println("Async query started with id :  " + query_id + " -- " + command);

					Thread.sleep(1300);

				} catch (Exception ex) {

					System.out.println(ex.getMessage());
					ex.printStackTrace();

					ColorHelpers.PrintRed("Error evaluating the query! please check the query and run again.\n");
					continue;
				}

			}
		}

	}
}
