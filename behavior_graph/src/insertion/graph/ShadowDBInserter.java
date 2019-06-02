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
import insertion.ShadowInserter;
import querying.tools.GraphObjectHelper;

/**
 * @author omid
 *
 */
public class ShadowDBInserter extends ShadowInserter {

	private static ShadowDBInserter __Instance = null; // holds the singleton instance of shadow inserter
	
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
		super();
		
		inserterThread = new Thread(new AsyncNeo4JInserter(this));
		inserterThread.start();
		
	}

	

	/**
	 * returns the first query to be processed
	 * 
	 * @return the first query to be run
	 */
	@Override
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

}
