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
		
		temp += "\r\n" + String.format(" merge ( newProc:%s ) ", tempGraph.getProc().toN4JObjectString() );
		temp += "\r\n" + String.format(" merge ( parentProc:%s ) ", tempGraph.getParentProc().toN4JObjectString() );
		temp += "\r\n" + String.format(" merge (parentProc)-[:%s]->(newProc) ", tempGraph.getExec().toN4JObjectString() );
		if( tempGraph.getItem() != null ) {
			temp += "\r\n" + String.format(" merge ( item:%s ) ", tempGraph.getItem().toN4JObjectString() );
			temp += "\r\n" + String.format(" merge (newProc)-[:%s]->(item) ", tempGraph.getSyscall().toN4JObjectString() );
		}
		
		
//		temp += "\n\r" + String.format("merge ( newProc:Process{name:\"%1s\",pid:%2s} ) ", inp.proc_apid , inp.proc_pid);
//		if (!inp.proc_ppid.toLowerCase().equals("<na>") || inp.evt_type.toLowerCase().equals("fork")) {
//			// add the parent proec
//			temp += "\n\r" + String.format("merge ( parentProc:Process{name:\"%1s\",pid:%2s} ) ", inp.proc_pname,
//					inp.proc_ppid);
//			temp += "\n\r"
//					+ String.format("merge (parentProc)<-[:IsChildOf]-(newProc) ", inp.proc_pname, inp.proc_ppid);
//
//		}
//		if (!inp.fd_num.toLowerCase().equals("<na>")) {
//			temp += "\r\n" + String.format(
//					" merge  (resource:Resource{ type:\"%1s\", number:%2s, name:\"%3s\", fileName:\"%4s\",port:\"%5s\"  } )",
//					inp.fd_type, inp.fd_num, inp.fd_name, inp.fd_filename, inp.fd_port);
//			if (SaveVerbose)
//				temp += "\r\n" + String.format(
//						" create (newProc) -[call:%1s{time:\"%2s\",info:\"%3s\", args:\"%4s\",rawtimens:\"%5s\" }]-> (resource)",
//						inp.evt_type, inp.evt_time, "-", "-", inp.evt_rawtime_ns);
//			else
//				temp += "\r\n" + String.format(
//						" merge (newProc) -[call:%1s]-> (resource) on create set call.time=\"%2s\",call.info=\"%3s\", call.args=\"%4s\",call.rawtimens=\"%5s\" ",
//						inp.evt_type, inp.evt_time, "-", "-", inp.evt_rawtime_ns);
//
//		}
		// find the type
		temp += ";";
		// System.out.println(temp);
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
			// try (Statement stmt = TheConnection.createStatement()) {
			// ResultSet rs = stmt.executeQuery(temp);
			TheStateMent.executeQuery(temp);
			/*
			 * while (rs.next()) { System.out.println(rs.getString("ID")); }
			 */
			// } catch (Exception exx) {
			// exx.printStackTrace();
			// } finally {
			// con.close();

			// }
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}
}
