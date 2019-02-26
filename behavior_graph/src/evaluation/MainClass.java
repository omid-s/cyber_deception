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

public class MainClass {
	private static final boolean IsVerbose = false;

	private static final long REPORT_ROW_COUNT = 10000;

	
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

		InputStreamReader isReader = new InputStreamReader(System.in);

		GraphDBDal GraphActionFactory = new GraphDBDal();

		GraphObjectHelper VerboseHelper = new GraphObjectHelper(true, pid);
		GraphObjectHelper ClearHelper = new GraphObjectHelper(false, pid);

		int counter = 0;

		FileWriter stats_file = null;

		stats_file = new FileWriter(new File(output_file));

// if json is desired, create the array
		if (SaveJSON)
			stats_file.write("[\n");
		Instant start2 = Instant.now();
		if (ReadFromFile) {
			try {
				System.out.println("in read File");
				Scanner test = new Scanner(new File(fileAdr));
				// int counter = 0;
				GraphDBDal db = new GraphDBDal();
				String multipleRecords = "";

				while (test.hasNextLine()) {
					try {
						SysdigRecordObject tempObj;
						try {
							int theL = multipleRecords.length();

							multipleRecords += test.nextLine();
							tempObj = objectDAL.GetObjectFromTextLine(multipleRecords);

							if (SaveFormated)
								stats_file.write(tempObj.toString() + "\n");
							if (SaveJSON)
								stats_file.write(tempObj.toJSONString() + ",");
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

						if (ShowVerbose) {
							VerboseHelper.AddRowToGraph(theGraph, tempObj);

						}
						if (ShowGraph) {
							ClearHelper.AddRowToGraph(theGraph, tempObj);

						}

						if (counter % 1000 == 0) {
							System.out.println(counter);
							if (counter % 10000 == 0) {
//								Duration.between(start2, end).toMillis() 
							}
							// break;
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

		Instant end2 = Instant.now();

		ColorHelpers.PrintBlue("in : " + Duration.between(start2, end2).toMillis() + "  Milli Seconds \n");
		/// clsoe the output file
		if (stats_file != null) {
			// is json is desired, close the array
			if (SaveJSON)
				stats_file.write("\n]");
			stats_file.flush();
			stats_file.close();
		}

		int num_edges = theGraph.getEdgeCount();
		int num_vertex = theGraph.getVertexCount();

		theGraph = null;
		ClearHelper.release_maps();
		VerboseHelper.release_maps();

		VerboseHelper = null;
		ClearHelper = null;

		System.gc();

		BaseAdapter queryMachine = null;

		/// set the query adapter
		if (MemQuery)
			queryMachine = InMemoryAdapter.getSignleton();
		else if (SimplePGQuery)
			queryMachine = SimplePGAdapter.getSignleton();
		else if (SimpleNeo4JQuery)
			queryMachine = SimpleNeo4JAdapter.getSignleton();

		/// setup GUI window

		Scanner reader = new Scanner(System.in);
		while (true) {
			try {
				ColorHelpers.PrintBlue("$$>>");
				String command = reader.nextLine();
				if (command.equalsIgnoreCase("exit") || command.equalsIgnoreCase("quit"))
					break;

				else if (command.trim().equalsIgnoreCase("info")) {
					ColorHelpers.PrintGreen(
							String.format("Total Edges : %d \n Total Vertices : %d \r\n", num_edges, num_vertex));
					continue;
				} else if (command.trim().toLowerCase().startsWith("set ")) { // process
																				// runtime
																				// variablse
																				// settings

					RuntimeVariables.getInstance().setValue(command.trim().split(" ")[1], command.trim().split(" ")[2]);
					continue;
				} else if (command.trim().toLowerCase().startsWith("get ")) {// process
																				// runtime
																				// variablse
																				// settings
					ColorHelpers
							.PrintGreen(RuntimeVariables.getInstance().getValue(command.trim().split(" ")[1]) + "\r\n");
					continue;
				} else if (command.trim().toLowerCase().startsWith("describe")) {

					boolean isAggregated = !(command.contains(" verbose"));
					boolean hasPath = command.indexOf("path=") > 0;
					boolean hasSort = command.indexOf("orderby=") > 0;
					String thePath = hasPath ? command.substring(command.indexOf("path=") + "path=".length()) : null;
					String SortBy = hasSort ? command.substring(command.indexOf("orderby=") + "orderby=".length(),
							command.indexOf(" ", command.indexOf("orderby=")) > 0
									? command.indexOf(" ", command.indexOf("orderby="))
									: command.length())
							: null;

					DescribeFactory.doDescribe(thePath, isAggregated, SortBy);
					continue;
				}

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
					continue;
				}

				Instant end = Instant.now();

				ColorHelpers.PrintBlue("in : " + Duration.between(start, end).toMillis() + "  Milli Seconds \n");

				System.out.flush();
				System.gc();

			} catch (VariableNoitFoundException ex) {
				ColorHelpers.PrintRed(ex.getMessage());
			} catch (Exception ex) {
				throw (ex);
				// ColorHelpers.PrintRed("query Problem!please try agin...
				// \r\n");
			}
		}
		GraphActionFactory.closeConnections();
		// System.out.print("\033[H\033[2J");
		ColorHelpers.PrintGreen("\nGood Luck from SSFC Lab @UGA Team!\r\n");

	}
}
