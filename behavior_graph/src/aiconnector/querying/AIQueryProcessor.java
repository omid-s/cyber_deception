/**
 * this file contains implementatino for the async query processor for the AI connector
 * 
 * This class is very similar  to querying.QueryProcessor 
 */
package aiconnector.querying;

import org.junit.experimental.theories.Theories;

import classes.AccessCall;
import classes.ResourceItem;
import edu.uci.ics.jung.graph.Graph;
import helpers.ColorHelpers;
import querying.AsyncQueryRunner;


public class AIQueryProcessor implements Runnable {

	public Long num_edges, num_vertex;
	Graph<ResourceItem, AccessCall> theGraph;
	boolean ShowGraph;
	boolean ShowVerbose;

	public AIQueryProcessor(boolean MemQuery, Long num_edges,
			Long num_vertex, Graph<ResourceItem, AccessCall> theGraph) {
		
		this.num_edges = num_edges;
		this.num_vertex = num_vertex;
		this.theGraph = theGraph;
	}

	public AIQueryProcessor() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void run() {

		Integer asyncID = 0;

		String[] commands = { "back select * from * where name has .txt ", "back select * from * where name has uname ", "back select * from * where name has groups ",  "back select * from * where name has .log "  };

		for (String command : commands) {
			{

				try {

					AsyncQueryRunner asqr = new AsyncQueryRunner(asyncID, command, false);
					asqr.setAIConnector(true);

					Thread T = new Thread(asqr);
					T.start();
					System.out.println("Async query started with id :  " + asyncID + " -- "+ command);
					asyncID += 1;

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
