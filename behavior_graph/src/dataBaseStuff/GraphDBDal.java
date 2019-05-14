package dataBaseStuff;

import java.sql.Connection;

//import org.neo4j.jdbc.*;

//import java.sql.*;
import java.sql.Statement;
import java.util.ArrayList;

import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Config;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.Transaction;

import classes.*;
import controlClasses.Configurations;
import edu.uci.ics.jung.graph.Graph;
import querying.tools.GraphObjectHelper;

public class GraphDBDal {

	public GraphDBDal() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		// to do : inittaite
//		Class.forName("org.neo4j.jdbc.Driver").newInstance();
	}

	/**
	 * closes the connections to the database
	 */
	public void closeConnections() {
		try {
			if (TheStateMent != null)
				TheStateMent.close();
			if (TheConnection != null)
				TheConnection.close();
		} catch (Exception ex) {
			System.out.println("ErrorCloseing the conection!");
			ex.printStackTrace();
		}
	}

	private static Connection TheConnection;
	private static Statement TheStateMent;
	private Driver driver = null;
	ArrayList<String> Queries = new ArrayList<String>();

	/**
	 * adds the given record to be inserted to the database
	 * @param inp the record to be inserted
	 * @param SaveVerbose if the record is being inserted in the verbose mode
	 */
	public void Save(SysdigRecordObject inp, boolean SaveVerbose) {

		SysdigRecordObjectGraph tempGraph = GraphObjectHelper.getGraphFromRecord(inp);

		String temp = "";

		temp += "\r\n" + String.format(" merge ( newProc:%s ) ", tempGraph.getProc().toN4JObjectString());
		temp += "\r\n" + String.format(" merge ( parentProc:%s ) ", tempGraph.getParentProc().toN4JObjectString());
		temp += "\r\n"
				+ String.format(" merge (parentProc)-[:%s]->(newProc) ", tempGraph.getExec().toN4JObjectString());
		if (tempGraph.getItem() != null) {

			temp += "\r\n" + String.format(" merge ( thread:%s ) ", tempGraph.getThread().toN4JObjectString());
			temp += "\r\n" + String.format(" merge ( ubsi:%s ) ", tempGraph.getUBSIUnit().toN4JObjectString());

			temp += "\r\n"
					+ String.format(" merge (newProc)-[:%s]->(thread) ", tempGraph.getSpawn().toN4JObjectString());
			temp += "\r\n"
					+ String.format(" merge (thread)-[:%s]->(ubsi) ", tempGraph.getUbsi_start().toN4JObjectString());

			temp += "\r\n" + String.format(" merge ( item:%s ) ", tempGraph.getItem().toN4JObjectString());
			temp += "\r\n" + String.format(" merge (ubsi)-[:%s]->(item) ", tempGraph.getSyscall().toN4JObjectString());
		}

		temp += ";";

		Queries.add(temp);

		if (Queries.size() % 1000 == 0)
			flushRows();

	}

	/**
	 * flushes the rows into the database
	 * this is done on a fixed record count interval
	 */
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
}
