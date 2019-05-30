package mainPackage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.time.Duration;
import java.time.Instant;
import java.util.Scanner;
import javax.swing.JFrame;
import classes.AccessCall;
import classes.ResourceItem;
import classes.SysdigRecordObject;
import controlClasses.Configurations;
import controlClasses.RuntimeVariables;
import dataBaseStuff.GraphDBDal;
import edu.uci.ics.jung.graph.DirectedOrderedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
import exceptions.HighFieldNumberException;
import exceptions.LowFieldNumberException;
import exceptions.VariableNoitFoundException;
import helpers.ColorHelpers;
import helpers.DescribeFactory;
import insertion.graph.ShadowDBInserter;
import querying.QueryInterpreter;
import querying.QueryProcessor;
import querying.adapters.BaseAdapter;
import querying.adapters.memory.InMemoryAdapter;
import querying.adapters.simpleNeo4J.SimpleNeo4JAdapter;
import querying.adapters.simplePG.SimplePGAdapter;
import querying.parsing.ParsedQuery;
import querying.tools.GraphObjectHelper;
import readers.CSVReader;
import readers.SysdigObjectDAL;

public class MainClass {

	public static void main(String args[]) throws Exception {
		Graph<ResourceItem, AccessCall> theGraph = new DirectedOrderedSparseMultigraph<ResourceItem, AccessCall>();
		boolean ReadFromFile = false;
		String pid = "";

		boolean SaveToDB = false, SaveToGraph = false, ShowVerbose = false, ShowGraph = false, Neo4JVerbose = false,
				InShortFormat = false, SaveFormated = false, MemQuery = false, SimplePGQuery = false,
				ReadStream = false, SimpleNeo4JQuery = false, ReadCSV = false, SaveJSON = false, LegacyMode = false,
				ShadowInserter = false;
		int compression = -1;
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
			if (pick.equals("lg") || pick.equals("legacy_mode"))
				LegacyMode = true;
			if (pick.equals("si") || pick.equals("shadow_insert"))
				ShadowInserter = true;
			if (pick.equals("c0"))
				compression = 0;
			if (pick.equals("c1"))
				compression = 1;
			if (pick.equals("c2"))
				compression = 2;
			if (pick.equals("c3"))
				compression = 3;

			if (pick.equals("-h")) {
				System.out.println(" gv: Show Graph in verbose mode \r\n " + " g : show graph in minimized mode \r\n"
						+ "smsql: save to my sql \r\n" + "sneo4j: save to neo4 j data base"
						+ "neo4jv : save neo4j data in verbose  \r\nsv: save in a key value pair format");
				return;
			}
		}

		Configurations.getInstance().setSetting(Configurations.COMPRESSSION_LEVEL, String.valueOf(compression));
		Configurations.getInstance().setSetting(Configurations.LEGACY_MODE, String.valueOf(LegacyMode));
		Configurations.getInstance().setSetting(Configurations.SHADOW_INSERTER, String.valueOf(ShadowInserter));

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
		BufferedReader bufReader = new BufferedReader(isReader);

		GraphDBDal GraphActionFactory = new GraphDBDal();

		GraphObjectHelper VerboseHelper = new GraphObjectHelper(true, pid);
		GraphObjectHelper ClearHelper = new GraphObjectHelper(false, pid);

		int counter = 0;
		int inError = 0;
		long counterr = 0;
		int skipped = 0;

		FileWriter output_file_writer = null;
		if (SaveFormated || SaveJSON) {
			output_file_writer = new FileWriter(new File(output_file));
		}
		Long num_edges = (long) theGraph.getEdgeCount();
		Long num_vertex = (long) theGraph.getVertexCount();
		QueryProcessor q_processor = new QueryProcessor(MemQuery, SimplePGQuery, SimpleNeo4JQuery, num_edges,
				num_vertex, theGraph, ShowGraph, ShowVerbose, fileAdr, GraphActionFactory);

		Thread queryThread = new Thread(q_processor);
		queryThread.start();

		if (ReadStream)
			while (true) {

				try {
					String inputStr = null;
					if ((inputStr = bufReader.readLine()) != null) {
						// System.out.print("+");
						counterr++;
						if (counterr == 1 || counterr == 100000)
							System.out.println(inputStr);
						// if(true) continue;
						SysdigRecordObject tempObj = objectDAL.GetObjectFromTextLine(inputStr);

						/// if desired write the formated file
						if (SaveFormated)
							output_file_writer.write(tempObj.toString() + "\n");

						if (Thread.currentThread().getId() == Long.parseLong(tempObj.thread_tid)) {
							skipped++;
							System.out.println(".");
							continue;
						}

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

					} else {
						break;
					}
					counter++;
					System.out.print("\033[H\033[2J");
					System.out.print(String.format("%c[%d;%df Total : %d", 0x1B, 15, 25, counter));
					System.out.print(String.format("%c[%d;%df With Error : %d %d", 0x1B, 16, 25, inError,
							Thread.currentThread().getId()));
					System.out.flush();

				} catch (NumberFormatException ex) {
					inError++;
				} catch (Exception e) {
					System.out.println("Error");
				}
			}
		// if json is desired, create the array
		// if (SaveJSON)
		// output_file_writer.write("[\n");
		Instant start2 = Instant.now();
		Runtime runtime = Runtime.getRuntime();
		int cleaner_ctr = 0;

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
								output_file_writer.write(tempObj.toString() + "\n");
							if (SaveJSON)
								output_file_writer
										.write("{\"index\":{\"_index\":\"test\"}}\n" + tempObj.toJSONString() + "\n");
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

						if (counter % 10000 == 0) {

							if (MemQuery) {
								InMemoryAdapter mem = InMemoryAdapter.getSignleton();
								for (ResourceItem pick : theGraph.getVertices()) {
									mem.addResourceItem(pick);
								}
								for (AccessCall pick : theGraph.getEdges()) {
									mem.addAccessCall(pick);
								}
							}

//							System.out.println(counter + "(Q :" + ShadowDBInserter.getInstance().getQueLenght() + ")");

							if (counter % 500000 == 0) {

								if (runtime.freeMemory() <= runtime.totalMemory() * 0.30) {
									cleaner_ctr++;
									System.out.print("*");
									Thread t1 = new Thread(new Runnable() {
										@Override
										public void run() {
											System.gc();
										}
									});
									t1.start();

								}
							}
						}

					} catch (Exception ex) {
						ColorHelpers.PrintRed("\nError Happened: " + ex.getMessage() + "\n");

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
		if (output_file_writer != null) {
			// is json is desired, close the array
			if (SaveJSON)
				// output_file_writer.write("\n]");
				output_file_writer.write("\n");
			output_file_writer.flush();
			output_file_writer.close();
		}

//		if (MemQuery) {
//			InMemoryAdapter mem = InMemoryAdapter.getSignleton();
//			for (ResourceItem pick : theGraph.getVertices()) {
//				mem.addResourceItem(pick);
//			}
//			for (AccessCall pick : theGraph.getEdges()) {
//				mem.addAccessCall(pick);
//			}
//		}

		theGraph = null;
		ClearHelper.release_maps();
		VerboseHelper.release_maps();

		VerboseHelper = null;
		ClearHelper = null;
		System.out.println("Cleaner was run :" + cleaner_ctr);
		System.gc();

	}

	/**
	 * Runs the main command entry loop
	 * 
	 * @param MemQuery
	 * @param SimplePGQuery
	 * @param SimpleNeo4JQuery
	 * @param num_edges
	 * @param num_vertex
	 * @param theGraph
	 * @param ShowGraph
	 * @param ShowVerbose
	 * @param fileAdr
	 * @param GraphActionFactory
	 */
	private static void command_loop(boolean MemQuery, boolean SimplePGQuery, boolean SimpleNeo4JQuery, int num_edges,
			int num_vertex, Graph<ResourceItem, AccessCall> theGraph, boolean ShowGraph, boolean ShowVerbose,
			String fileAdr, GraphDBDal GraphActionFactory) {
		BaseAdapter queryMachine = null;

		/// set the query adapter
		if (MemQuery)
			queryMachine = InMemoryAdapter.getSignleton();
		else if (SimplePGQuery)
			queryMachine = SimplePGAdapter.getSignleton();
		else if (SimpleNeo4JQuery)
			queryMachine = SimpleNeo4JAdapter.getSignleton();

		/// setup GUI window
		GraphPanel theGraphWindow = null;
		JFrame frame1 = new JFrame();

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

				theGraphWindow = new GraphPanel(theGraph);
				if (frame1.isVisible()) {
					frame1.setVisible(false);
					frame1.dispose();
				}

				frame1 = new JFrame();
				frame1.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				frame1.setSize(400, 400);
				if (ShowGraph || ShowVerbose) {
					theGraphWindow.vv.repaint();
					frame1.add(theGraphWindow);
					frame1.setVisible(true);
					frame1.setExtendedState(JFrame.MAXIMIZED_BOTH);
					frame1.setTitle(fileAdr);
				}

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
