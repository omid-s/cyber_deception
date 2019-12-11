package evaluation;

import controlClasses.Configurations;
import dataBaseStuff.GraphDBDal;
import edu.uci.ics.jung.graph.DirectedOrderedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
import exceptions.HighFieldNumberException;
import exceptions.LowFieldNumberException;
import helpers.ColorHelpers;
import insertion.ShadowInserter;
import insertion.graph.ShadowNeo4JInserter;
import insertion.pg.ShadowPGInserter;
import querying.QueryInterpreter;
import querying.adapters.BaseAdapter;
import querying.adapters.memory.InMemoryAdapter;
import querying.adapters.simpleNeo4J.SimpleNeo4JAdapter;
import querying.adapters.simplePG.SimplePGAdapter;
import querying.parsing.ParsedQuery;
import querying.tools.GraphObjectHelper;
import readers.CSVReader;
import readers.SysdigObjectDAL;

import java.io.File;
import java.io.FileWriter;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.time.Duration;
import java.time.Instant;
import classes.AccessCall;
import classes.ResourceItem;
import classes.SysdigRecordObject;

public class MainEvaluationClass {
	private static final boolean IsVerbose = false;

	private static final long REPORT_ROW_COUNT = 100000;

	public static void main(String args[]) throws Exception {
		Graph<ResourceItem, AccessCall> theGraph = new DirectedOrderedSparseMultigraph<ResourceItem, AccessCall>();
		boolean ReadFromFile = false;
		String pid = "";

		boolean SaveToDB = false, SaveToGraph = false, ShowVerbose = false, ShowGraph = false, Neo4JVerbose = false,
				InShortFormat = false, SaveFormated = false, MemQuery = false, SimplePGQuery = false,
				ReadStream = false, SimpleNeo4JQuery = false, ReadCSV = false, SaveJSON = false,  
				ClearDB = false,ShadowInsertion = false;
		String fileAdr = "", output_file = "";
		String computerID = "1";
		String backEnd = "pg";
		int compression = -1;

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
			if (pick.startsWith("cid=")) {
				computerID = pick.split("=")[1];
			}
			if (pick.startsWith("be=")) {
				backEnd = pick.split("=")[1];
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
			if (pick.equals("si") || pick.equals("shadow_insert"))
				ShadowInsertion = true;
			if (pick.equals("c0"))
				compression = 0;
			if (pick.equals("c1"))
				compression = 1;
			if (pick.equals("c2"))
				compression = 2;
			if (pick.equals("c3"))
				compression = 3;
			if (pick.equals("clr"))
				ClearDB = true;

			Configurations.getInstance().setSetting(Configurations.COMPRESSSION_LEVEL, String.valueOf(compression));
			Configurations.getInstance().setSetting(Configurations.SHADOW_INSERTER, String.valueOf(ShadowInsertion));
			Configurations.getInstance().setSetting(Configurations.EVAL_CLEAR_DB, String.valueOf(ClearDB));
			Configurations.getInstance().setSetting(Configurations.COMPUTER_ID, computerID);

			if(ShadowInsertion) {
				if( backEnd.equals("neo4j")) {
					ShadowInserter.theInserter = ShadowNeo4JInserter.getInstance();
				}
				else {
					ShadowInserter.theInserter = ShadowPGInserter.getInstance();
				}
			}
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

		String[] keys = { "counter", "clock_time", "date_time", "last_rows_time", "total_time", "select_time",
				"select_edge", "select_vertex", "bt_time", "bt_edge", "bt_vertex", "ft_time", "ft_edge", "ft_vertex",
				"mem_used", "buffer_size" };

		String row1 = "";
		for (int i = 0; i < keys.length; i++) {
			row1 += keys[i] + ",";
		}
		stats_file.write(row1 + "\n");
		stats_file.flush();

		long total_time = 0;

		Instant start2 = Instant.now();
		Instant lastStep = Instant.now();
		Runtime runtime = Runtime.getRuntime();
		double init_time = -1;
		double previous_time = 0;
		double time_drag = 0;
		GraphObjectHelper ClearHelper = new GraphObjectHelper(false, pid);
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

						double the_time = Double
								.parseDouble(tempObj.evt_datetime.substring(0, tempObj.evt_datetime.indexOf('(')));
						if (init_time == -1 || Math.abs(previous_time - the_time) > 100) {
							time_drag += Math.round(previous_time - init_time);
							init_time = the_time;
						}
//
//						if (Math.round(the_time - previous_time) > 0) {
//							Thread.sleep(Math.round(the_time-previous_time));	
//						}

						previous_time = the_time;

						counter++;
						if (SaveFormated)
							stats_file.write(tempObj.toString() + "\n");
						if (SaveJSON)
							stats_file.write(tempObj.toJSONString() + ",");
						if (SaveToDB)
							objectDAL.Insert(tempObj);
						if (ShowGraph) {
							ClearHelper.AddRowToGraph(theGraph, tempObj);
						}
						if (SaveToGraph)
							GraphActionFactory.Save(tempObj, Neo4JVerbose);

						if (counter % 100000 == 0) {
//							System.out.println(counter);

							if (counter % 5000000 == 0) {

								if (runtime.freeMemory() <= runtime.totalMemory() * 0.30) {

									System.out.print("#");
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

						if (counter % (REPORT_ROW_COUNT / 10) == 0) {
							System.out.print("*");
							if (counter % REPORT_ROW_COUNT == 0) {
								System.out.println(counter);
								Map<String, Long> stats = new HashMap<String, Long>();

								Instant temp_end = Instant.now();
								Long last_rows_time = Duration.between(lastStep, temp_end).toMillis();
								total_time += last_rows_time;

								System.out.println(tempObj.evt_datetime);

								double clock_time = time_drag + Math.round(the_time - init_time);

								lastStep = Instant.now();
								System.out.println(Math.abs(Math.round(clock_time - (total_time / 1000))));
								
//								if (Math.round(clock_time - (total_time / 1000)) > 0)
//									Thread.sleep(1000 * Math.round(clock_time - (total_time / 1000)));

								stats.put("counter", counter);
								stats.put("clock_time", Math.round(clock_time));
								stats.put("date_time", (new Date()).getTime());
								stats.put("last_rows_time", last_rows_time);
								stats.put("total_time", total_time);

//								runQuery("select * from file where name has iou897iou ", queryMachine, stats, "select_");
//								runQuery("select * from file where name has iou897iou ", queryMachine, stats, "bt_");
//								runQuery("select * from file where name has iou897iou ", queryMachine, stats, "ft_");
//								runQuery("back select * from * where name has .txt", queryMachine, stats, "bt_");
//								runQuery("forward select * from * where name has gmain ", queryMachine, stats, "ft_");

								stats.put("bt_" + "time", 0l);
								stats.put("bt_" + "edge", 0l);
								stats.put("bt_" + "vertex", 0l);
								stats.put("ft_" + "time", 0l);
								stats.put("ft_" + "edge", 0l);
								stats.put("ft_" + "vertex", 0l);
								stats.put("mem_used", runtime.totalMemory() - runtime.freeMemory());
								stats.put("buffer_size", ShadowNeo4JInserter.getInstance().getQueLenght());

								String row = "";
								for (int i = 0; i < keys.length; i++) {
									row += stats.get(keys[i]) + ",";
								}
								stats_file.write(row + "\n");
								stats_file.flush();
								// lastStep = Instant.now();
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
		System.exit(0);
//		ShadowNeo4JInserter.getInstance().getWorkerThread().wait();

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
