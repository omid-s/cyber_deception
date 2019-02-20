package dataBaseStuff;

//import org.neo4j.jdbc.*;

import java.sql.*;

import org.neo4j.driver.v1.Config;

import classes.*;
import edu.uci.ics.jung.graph.Graph;
import helpers.Configurations;
import querying.tools.GraphObjectHelper;

public class GraphDBDal {

	public GraphDBDal() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		// to do : inittaite
		Class.forName("org.neo4j.jdbc.Driver").newInstance();
	}

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
			
			temp += "\r\n" + String.format(" merge (newProc)-[:%s]->(thread) ", tempGraph.getSpawn().toN4JObjectString());
			temp += "\r\n" + String.format(" merge (thread)-[:%s]->(ubsi) ", tempGraph.getUbsi_start().toN4JObjectString());
			
			temp += "\r\n" + String.format(" merge ( item:%s ) ", tempGraph.getItem().toN4JObjectString());
			temp += "\r\n" + String.format(" merge (ubsi)-[:%s]->(item) ", tempGraph.getSyscall().toN4JObjectString());
		}

		temp += ";";

		try {

			// Connect
			if (TheConnection == null) {
				TheConnection = DriverManager.getConnection(
						String.format("jdbc:neo4j:bolt://%s/",
								Configurations.getInstance().getSetting(Configurations.NEO4J_SERVER)),
						Configurations.getInstance().getSetting(Configurations.NEO4J_USERNAME),
						Configurations.getInstance().getSetting(Configurations.NEO4J_PASSWORD)

				);
				TheStateMent = TheConnection.createStatement();
			}
			// Querying
			TheStateMent.executeQuery(temp);

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}
}
