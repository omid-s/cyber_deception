/**
 * 
 */
package insertion.graph;

import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.Transaction;

import classes.AccessCall;
import classes.ResourceItem;
import classes.SysdigRecordObjectGraph;
import controlClasses.Configurations;
import querying.tools.GraphObjectHelper;

/**
 * @author omid
 *
 */
public class ShadowDBInserter {

	private static ShadowDBInserter __Instance = null; // holds the singleton instance of shadow inserter
	private ConcurrentLinkedQueue<Object> theObjectQue;
	private ConcurrentLinkedQueue<AccessCall> theEdgeUpdateQue;
	private ConcurrentHashMap<Long, Integer> theIndexer;

	private Thread inserterThread;

	/**
	 * returns the singleton inserter object.
	 * 
	 * @return the inserter object
	 */
	public static ShadowDBInserter getInstance() {
		if (__Instance == null)
			__Instance = new ShadowDBInserter();

		return __Instance;
	}

	/**
	 * creates an instance of the shadow inserter object
	 */
	private ShadowDBInserter() {

		theObjectQue = new ConcurrentLinkedQueue<Object>();
		theEdgeUpdateQue = new ConcurrentLinkedQueue<AccessCall>();

		theIndexer = new ConcurrentHashMap<Long, Integer>();

		inserterThread = new Thread(new AsyncNeo4JInserter(this));
		inserterThread.start();
	}

	/**
	 * inserts the graph nodes/ resources in the queue to be added to the graph 
	 * @param node the resource item to be inserted
	 */
	public void insertNode(ResourceItem node) {
		theObjectQue.add(node);
	}

	/**
	 * adds the edge to be inserted this is to be used for new edges
	 * @param edge edge to be inseted
	 */
	public void insertEdge(AccessCall edge) {

		if (!theIndexer.keySet().contains(edge.sequenceNumber)) {
			theIndexer.put(edge.sequenceNumber, 1);
			theObjectQue.add(edge);
		}

	}

	/**
	 * adds the edge to the queue for update. THis should only be used for edges that are set for update not inserts
	 * @param edge the edge to be updated
	 */
	public void setEdgeForUpdate(AccessCall edge) {

		if (!theIndexer.keySet().contains(edge.sequenceNumber)) {
			theIndexer.put(edge.sequenceNumber, 2);
			theObjectQue.add(edge);
		}
		else {
			// TODO : this case should be ignored in this model if other methods are desired this should change
		}
	}

	/**
	 * returns true if there are queris to be run
	 * 
	 * @return true if there are queris to be processed
	 */
	public boolean hasNext() {
		return theObjectQue.size() + theEdgeUpdateQue.size() > 0;
	}

	/**
	 * returns the first query to be processed
	 * 
	 * @return the first query to be run
	 */
	public String getQuery() {
		Object ret = theObjectQue.poll();
		if (ret instanceof String)
			return (String) ret;
		else if (ret instanceof AccessCall) {
			AccessCall edge = (AccessCall) ret;
			String temp = "";

			temp += "\r\n" + String.format(" merge ( f:%s ) ", edge.From.toN4JObjectString());
			temp += "\r\n" + String.format(" merge ( t:%s ) ", edge.To.toN4JObjectString());
			temp += "\r\n" + String.format(" merge (f)-[:%s]->(t) ", edge.toN4JObjectString());

			temp += ";";
			
			//remove the item from indexer list
			theIndexer.remove(edge.sequenceNumber);
			
			return temp;
		} else if (ret instanceof ResourceItem) {
			ResourceItem node = (ResourceItem) ret;
			String temp = "";

			temp += "\r\n" + String.format(" merge ( f:%s ) ", node.toN4JObjectString());

			temp += ";";
			return temp;
		}
		return "";
	}

	/**
	 * returns the total length of the queues 
	 * @return the length of the queues
	 */
	public long getQueLenght() {
		return theObjectQue.size() + theEdgeUpdateQue.size();
	}

	public Thread getWorkerThread() {
		return this.inserterThread;
	}
}
