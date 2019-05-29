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

	public void insertNode(ResourceItem node) {
		theObjectQue.add(node);

	}

	public void insertEdge(AccessCall edge) {

		if (!theIndexer.keySet().contains(edge.sequenceNumber)) {
			theIndexer.put(edge.sequenceNumber, 1);
//		if (!theObjectQue.contains(edge))
			theObjectQue.add(edge);
		}

	}

	public void setEdgeForUpdate(AccessCall edge) {
//		if (!theEdgeUpdateQue.contains(edge))
		theEdgeUpdateQue.add(edge);
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

	public long getQueLenght() {
		return theObjectQue.size() + theEdgeUpdateQue.size();
	}

	public Thread getWorkerThread() {
		return this.inserterThread;
	}
}
