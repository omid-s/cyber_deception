package mainPackage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.time.Duration;
import java.time.Instant;
import java.util.Scanner;

import classes.AccessCall;
import classes.ResourceItem;
import classes.SysdigRecordObject;
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
import querying.QueryProcessor;
import querying.tools.GraphObjectHelper;
import readers.CSVReader;
import readers.SysdigObjectDAL;
import aiconnector.AIConnectorGraalfAgent;
import aiconnector.querying.*;
import querying.adapters.memory.InMemoryAdapter;

public class AIConnectorMain {

	public AIConnectorMain() {
		//
	}

	public static void main(String args[]) throws Exception {
		Graph<ResourceItem, AccessCall> theGraph = new DirectedOrderedSparseMultigraph<ResourceItem, AccessCall>();
		boolean ReadFromFile = false;
		String pid = "";

		boolean SaveToDB = false, SaveToGraph = false, ShowVerbose = false, ShowGraph = false, Neo4JVerbose = false,
				InShortFormat = false, SaveFormated = false, MemQuery = false, SimplePGQuery = false,
				ReadStream = false, SimpleNeo4JQuery = false, ReadCSV = false, SaveJSON = false, LegacyMode = false,
				ShadowInsertion = false;
		int compression = -1;
		String fileAdr = "", output_file = "";
		String computerID = "1";
		String backEnd = "pg";

		for (String pick : args) {
			if (pick.equals("file"))
				ReadFromFile = true;
			if (pick.startsWith("\"path") || pick.startsWith("path")) {
				fileAdr = pick.split("=")[1].replace("\"", "");
			}

			if (pick.startsWith("\"outpath") || pick.startsWith("outpath")) {
				output_file = pick.split("=")[1].replace("\"", "");
			}
			if (pick.startsWith("cid=")) {
				computerID = pick.split("=")[1];
			}
			if (pick.startsWith("be=")) {
				backEnd = pick.split("=")[1];
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
				ShadowInsertion = true;
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

		ReadStream = !ReadFromFile;

		Configurations.getInstance().setSetting(Configurations.COMPRESSSION_LEVEL, String.valueOf(compression));
		Configurations.getInstance().setSetting(Configurations.LEGACY_MODE, String.valueOf(LegacyMode));
		Configurations.getInstance().setSetting(Configurations.SHADOW_INSERTER, String.valueOf(ShadowInsertion));
		Configurations.getInstance().setSetting(Configurations.COMPUTER_ID, computerID);

		if (ShadowInsertion) {
			if (backEnd.equals("neo4j")) {
				ShadowInserter.theInserter = ShadowNeo4JInserter.getInstance();
			} else {
				ShadowInserter.theInserter = ShadowPGInserter.getInstance();
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

		GraphObjectHelper VerboseHelper = new GraphObjectHelper(true, pid);
		GraphObjectHelper ClearHelper = new GraphObjectHelper(false, pid);

		int counter = 0;

		FileWriter output_file_writer = null;
		if (SaveFormated || SaveJSON) {
			output_file_writer = new FileWriter(new File(output_file));
		}
		Long num_edges = (long) theGraph.getEdgeCount();
		Long num_vertex = (long) theGraph.getVertexCount();
		AIQueryProcessor q_processor = new AIQueryProcessor(MemQuery, num_edges, num_vertex, theGraph);

		Thread queryThread = new Thread(q_processor);
		queryThread.start();

		
		AIConnectorGraalfAgent agent = new AIConnectorGraalfAgent();
		Thread agent_thread = new Thread(agent);
		agent_thread.start();
		
		Instant start2 = Instant.now();
		Runtime runtime = Runtime.getRuntime();
		int cleaner_ctr = 0;

		try {
			Scanner test = null;
			if (ReadFromFile) {
				test = new Scanner(new File(fileAdr));
				System.out.println("in read File");
			} else {
				test = new Scanner(System.in);
				System.out.println("in read stream");
			}

			GraphDBDal db = new GraphDBDal();
			String multipleRecords = "";

			while (test.hasNextLine()) {
				try {

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

					// set computer id
					tempObj.Computer_id = Configurations.getInstance().getSetting(Configurations.COMPUTER_ID);

					multipleRecords = "";

					counter++;

					if (SaveToDB)
						objectDAL.Insert(tempObj);

					if (SaveToGraph) {
						GraphActionFactory.Save(tempObj, Neo4JVerbose);
					}
					if (ShowVerbose) {
						VerboseHelper.AddRowToGraph(theGraph, tempObj);
					}
					if (ShowGraph) {
						ClearHelper.AddRowToGraph(theGraph, tempObj);
					}

					if (counter % 10000 == 0) {

//							if (MemQuery) {
//								InMemoryAdapter mem = InMemoryAdapter.getSignleton();
//								for (ResourceItem pick : theGraph.getVertices()) {
//									mem.addResourceItem(pick);
//								}
//								for (AccessCall pick : theGraph.getEdges()) {
//									mem.addAccessCall(pick);
//								}
//							}

//							System.out.println(counter + "(Q :" + ShadowDBInserter.getInstance().getQueLenght() + ")");

						if (counter % 500000 == 0) {

							while (runtime.freeMemory() <= runtime.totalMemory() * 0.20) {
								System.out.print("+");
								// memory is too full, purge some records out then flush GC
								InMemoryAdapter.getSignleton().purge(2000, theGraph);
								System.gc();
							}

//								if (true) {
//									// runtime.freeMemory() <= runtime.totalMemory() * 0.30) {
//									cleaner_ctr++;
////									System.out.print("*");
//									Thread t1 = new Thread(new Runnable() {
//										@Override
//										public void run() {
//											System.gc();
//										}
//									});
//									t1.start();
//								}

						}
					}

				} catch (Exception ex) {
					ColorHelpers.PrintRed("\nError Happened: " + ex.getMessage() + "\n");
					throw ex;
				}

			}
			test.close();
			db.closeConnections();
		} catch (Exception ex) {
			ex.printStackTrace();
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

		theGraph = null;
		ClearHelper.release_maps();
		VerboseHelper.release_maps();

		VerboseHelper = null;
		ClearHelper = null;
		System.out.println("Cleaner was run :" + cleaner_ctr);
		System.gc();

	}

}
