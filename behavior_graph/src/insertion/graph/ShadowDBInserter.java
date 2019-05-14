/**
 * 
 */
package insertion.graph;

import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
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
	private ConcurrentLinkedQueue<ResourceItem> theResourceQue; //
	private ConcurrentLinkedQueue<AccessCall> theCallQue;
	private ConcurrentLinkedQueue<String> theQueryQue;
	private ConcurrentLinkedQueue<Object> theObjectQue;

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
		theResourceQue = new ConcurrentLinkedQueue<ResourceItem>();
		theCallQue = new ConcurrentLinkedQueue<AccessCall>();
		theQueryQue = new ConcurrentLinkedQueue<String>();
		theObjectQue = new ConcurrentLinkedQueue<Object>();

		Thread inserter = new Thread(new AsyncNeo4JInserter(this));
		inserter.start();

	}

	public void insertNode(ResourceItem node) {
		String temp = "";

		temp += "\r\n" + String.format(" merge ( f:%s ) ", node.toN4JObjectString());

		temp += ";";

//		theQueryQue.add(temp);
		theObjectQue.add(node);

//		if (Queries.size() % 10000 == 0)
//		{
//			System.out.print(".");
//			flushRows();
////			Queries.clear();
//		}
	}

	public void insertEdge(AccessCall edge) {

//		String temp = "";
//
//		temp += "\r\n" + String.format(" merge ( f:%s ) ", edge.From.toN4JObjectString());
//		temp += "\r\n" + String.format(" merge ( t:%s ) ", edge.To.toN4JObjectString());
//		temp += "\r\n" + String.format(" merge (f)-[:%s]->(t) ", edge.toN4JObjectString());
//
//		temp += ";";

		theObjectQue.add(edge);

//	theQueryQue.add(temp);

////	Queries.add(temp);
//		if (theQueryQue.size() % 10000 == 0)
//		{
//			System.out.print(".");
//			flushRows();
////			Queries.clear();
//		}
//			
	}

	private static Connection TheConnection;
	private static Statement TheStateMent;
	private Driver driver = null;
	ArrayList<String> Queries = new ArrayList<String>();

	public void flushRows() {

		try {

//			try {
//				if (TheConnection == null) {
//					TheConnection = java.sql.DriverManager.getConnection(
//							String.format("jdbc:neo4j:bolt://%s/",
//									Configurations.getInstance().getSetting(Configurations.NEO4J_SERVER)),
//							Configurations.getInstance().getSetting(Configurations.NEO4J_USERNAME),
//							Configurations.getInstance().getSetting(Configurations.NEO4J_PASSWORD)
//
//					);
//					TheStateMent = TheConnection.createStatement();
//				}
//				for (String temp : Queries)
//					TheStateMent.executeUpdate(temp);
////				System.out.print(";");
//			} catch (Exception ex) {
//				ex.printStackTrace();
//			}

			// Connect
//			if (TheConnection == null) {
//				TheConnection = DriverManager.getConnection(
//						String.format("jdbc:neo4j:bolt://%s/",
//								Configurations.getInstance().getSetting(Configurations.NEO4J_SERVER)),
//						Configurations.getInstance().getSetting(Configurations.NEO4J_USERNAME),
//						Configurations.getInstance().getSetting(Configurations.NEO4J_PASSWORD)
//
//				);
//				TheStateMent = TheConnection.createStatement();
//			}

			if (driver == null)
				driver = GraphDatabase.driver(
						"bolt://" + Configurations.getInstance().getSetting(Configurations.NEO4J_SERVER),
						AuthTokens.basic(Configurations.getInstance().getSetting(Configurations.NEO4J_USERNAME),
								Configurations.getInstance().getSetting(Configurations.NEO4J_PASSWORD)));
			Session session = driver.session();

			Transaction trnx = session.beginTransaction();
			for (String pick : Queries) {
				trnx.run(pick);
			}

			trnx.success();
			trnx.close();

			Queries.clear();

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * returns true if there are queris to be run
	 * 
	 * @return true if there are queris to be processed
	 */
	public boolean hasNext() {
		return theObjectQue.size() > 0;
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
		return theObjectQue.size();
	}
}