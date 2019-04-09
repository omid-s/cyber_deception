package evaluation;

import controlClasses.RecordInterpretorFactory;
import controlClasses.RuntimeVariables;
import dataBaseStuff.DataBaseLayer;
import dataBaseStuff.GraphDBDal;
import edu.uci.ics.jung.graph.DirectedOrderedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseMultigraph;
import exceptions.HighFieldNumberException;
import exceptions.LowFieldNumberException;
import exceptions.VariableNoitFoundException;
import helpers.ColorHelpers;
import helpers.DescribeFactory;
import querying.QueryInterpreter;
import querying.adapters.BaseAdapter;
import querying.adapters.memory.InMemoryAdapter;
import querying.adapters.simpleNeo4J.SimpleNeo4JAdapter;
import querying.adapters.simplePG.SimplePGAdapter;
import querying.parsing.Criteria;
import querying.parsing.ParsedQuery;
import querying.tools.GraphObjectHelper;
import readers.CSVReader;
import readers.SysdigObjectDAL;

import java.awt.Color;
import java.awt.Container;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.time.Instant;
import java.util.Properties;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.jfree.ui.RefineryUtilities;

import classes.AccessCall;
import classes.ResourceItem;
import classes.ResourceType;
import classes.SysdigRecordObject;

public class MainEvaluationClass {
	private static final boolean IsVerbose = false;

	private static final long REPORT_ROW_COUNT = 500000;

	public static void main(String args[]) throws Exception {
		Graph<ResourceItem, AccessCall> theGraph = new DirectedOrderedSparseMultigraph<ResourceItem, AccessCall>();
		boolean ReadFromFile = false;
		String pid = "";

		boolean SaveToDB = false, SaveToGraph = false, ShowVerbose = false, ShowGraph = false, Neo4JVerbose = false,
				InShortFormat = false, SaveFormated = false, MemQuery = false, SimplePGQuery = false,
				ReadStream = false, SimpleNeo4JQuery = false, ReadCSV = false, SaveJSON = false;
		String fileAdr = "", output_file = "";
		for (String pick : args) {
			if (pick.equals("file"))
				ReadFromFile = true;
			if (pick.startsWith("\"path") || pick.startsWith("path")) {
				fileAdr = pick.split("=")[1].replace("\"", "");
			}
			if (pick.startsWith("\"outpath") || pick.startsWith("outpath")) {
				output_file = pick.split("=")[1].replace("\"", "");
			}
			if (pick.startsWith("pid")) {
				pid = pick.split("=")[1];
				System.out.println("pid = " + pid);
			}

			if (pick.equals("gv")) {
				ShowVerbose = true;
				// ShowGraph = true;
			}
			if (pick.equals("g"))
				ShowGraph = true;
			if (pick.equals("ssql") || pick.equals("save_sql"))
				SaveToDB = true;
			if (pick.equals("sneo4j") || pick.equals("save_neo4j"))
				SaveToGraph = true;
			if (pick.equals("neo4jv") || pick.equals("save_neo4j_verbose"))
				Neo4JVerbose = true;
			if (pick.equals("short"))
				InShortFormat = true;
			if (pick.equals("csv") || pick.equals("read_csv"))
				ReadCSV = true;
			if (pick.equals("sf") || pick.equals("save_formatted"))
				SaveFormated = true;
			if (pick.equals("rm") || pick.equals("query_memory"))
				MemQuery = true;
			if (pick.equals("rspg") || pick.equals("query_postgres"))
				SimplePGQuery = true;
			if (pick.equals("rsn4j") || pick.equals("query_neo4j"))
				SimpleNeo4JQuery = true;
			if (pick.equals("sj") || pick.equals("save_json"))
				SaveJSON = true;

			if (pick.equals("-h")) {
				System.out.println(" gv: Show Graph in verbose mode \r\n " + " g : show graph in minimized mode \r\n"
						+ "smsql: save to my sql \r\n" + "sneo4j: save to neo4 j data base"
						+ "neo4jv : save neo4j data in verbose  \r\nsv: save in a key value pair format");
				return;
			}
		}

		if (SaveFormated && output_file.isEmpty()) {
			ColorHelpers.PrintRed(
					"to save formated output the outoutfuile has to be supplied! use outpath= key to set the path");
			return;
		}

		SysdigObjectDAL objectDAL = null;
		if (ReadCSV)
			objectDAL = new CSVReader();
		else
			objectDAL = new SysdigObjectDAL(InShortFormat);

		GraphDBDal GraphActionFactory = new GraphDBDal();

		long counter = 0;

		FileWriter stats_file = null;

		stats_file = new FileWriter(new File(output_file));

		BaseAdapter queryMachine = null;

		/// set the query adapter
		if (MemQuery)
			queryMachine = InMemoryAdapter.getSignleton();
		else if (SimplePGQuery)
			queryMachine = SimplePGAdapter.getSignleton();
		else if (SimpleNeo4JQuery)
			queryMachine = SimpleNeo4JAdapter.getSignleton();

		String[] keys = { "counter", "date_time", "last_rows_time", "total_time", "select_time", "select_edge",
				"select_vertex", "bt_time", "bt_edge", "bt_vertex", "ft_time", "ft_edge", "ft_vertex" };

		String row1 = "";
		for (int i = 0; i < keys.length; i++) {
			row1 += keys[i] + ",";
		}
		stats_file.write(row1 + "\n");
		stats_file.flush();

		long total_time = 0;

		Instant start2 = Instant.now();
		Instant lastStep = Instant.now();
		if (ReadFromFile) {
			try {
				System.out.println("in read File");
				Scanner test = new Scanner(new File(fileAdr));
				// int counter = 0;
				GraphDBDal db = new GraphDBDal();
				String multipleRecords = "";

				while (test.hasNextLine()) {
					try {

//						if (SimplePGQuery && counter < 8999999) {
//							counter++;
//							continue;
//						}
//						if (SimpleNeo4JQuery && counter < 7800000) {
//							counter++;
//							continue;
//						}

						SysdigRecordObject tempObj;
						try {
							int theL = multipleRecords.length();

							multipleRecords += test.nextLine();
							tempObj = objectDAL.GetObjectFromTextLine(multipleRecords);

							if (SaveFormated)
								stats_file.write(tempObj.toString() + "\n");
							if (SaveJSON)
								stats_file.write(tempObj.toJSONString() + ",");
							if (SaveToDB)
								objectDAL.Insert(tempObj);
							if (theL > 1)
								System.out.println("---------------------------------------------");
						} catch (LowFieldNumberException ex) {
							System.out.println(multipleRecords);
							continue;
						} catch (HighFieldNumberException ex) {
							multipleRecords = "";
							continue;
						}

						multipleRecords = "";

						counter++;

						if (SaveToDB)
							objectDAL.Insert(tempObj);

						if (SaveToGraph)
							GraphActionFactory.Save(tempObj, Neo4JVerbose);

						if (counter % (REPORT_ROW_COUNT / 10) == 0) {
							System.out.print("*");
							if (counter % REPORT_ROW_COUNT == 0) {
								System.out.println(counter);
								Map<String, Long> stats = new HashMap<String, Long>();

								Instant temp_end = Instant.now();
								Long last_rows_time = Duration.between(lastStep, temp_end).toMillis();
								total_time += last_rows_time;

								stats.put("counter", counter);
								stats.put("date_time", (new Date()).getTime());
								stats.put("last_rows_time", last_rows_time);
								stats.put("total_time", total_time);

								runQuery("select * from file where name has .txt ", queryMachine, stats, "select_");
								runQuery("back select * from * where name has .txt", queryMachine, stats, "bt_");
								runQuery("forward select * from * where name has gmain ", queryMachine, stats, "ft_");

								String row = "";
								for (int i = 0; i < keys.length; i++) {
									row += stats.get(keys[i]) + ",";
								}
								stats_file.write(row + "\n");
								stats_file.flush();
								lastStep = Instant.now();
							}
						}

					} catch (Exception ex) {
						System.out.println(ex.getMessage());
					}

				}
				test.close();
				db.closeConnections();
			} catch (Exception ex) {
				ex.printStackTrace();
			}

		}
		objectDAL.flushRows();
		GraphActionFactory.flushRows();
		
		Instant end2 = Instant.now();

		ColorHelpers.PrintBlue("in : " + Duration.between(start2, end2).toMillis() + "  Milli Seconds \n");
		/// clsoe the output file
		if (stats_file != null) {
			stats_file.flush();
			stats_file.close();
		}

		/// setup GUI window

		GraphActionFactory.closeConnections();
		// System.out.print("\033[H\033[2J");
		ColorHelpers.PrintGreen("\nGood Luck from SSFC Lab @UGA Team!\r\n");

	}

	private static void runQuery(String command, BaseAdapter queryMachine, Map<String, Long> stats,
			String stat_prefix) {

		try {

			Graph<ResourceItem, AccessCall> theGraph = null;

			Instant start = Instant.now();

			try {

				ParsedQuery query = null;
				try {
					query = QueryInterpreter.interpret(command, theGraph);
				} catch (Exception ex) {
					ex.printStackTrace();
				}

				theGraph = queryMachine.runQuery(query);
			} catch (Exception ex) {
				ColorHelpers.PrintRed("Error evaluating the query! please check the query and run again.\n");

			}

			Instant end = Instant.now();

			stats.put(stat_prefix + "time", Duration.between(start, end).toMillis());
			stats.put(stat_prefix + "edge", (long) theGraph.getEdgeCount());
			stats.put(stat_prefix + "vertex", (long) theGraph.getVertexCount());

		} catch (Exception ex) {
			throw (ex);
			// ColorHelpers.PrintRed("query Problem!please try agin...
			// \r\n");
		}
	}

}
