/**
 * 
 */
package helpers;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

/**
 * @author omid
 *
 */
public class Configurations {

	private static String fileName = "oql.conf";
	private static Configurations instance;

	private ArrayList<String> keysOrder;

	private HashMap<String, String> settings;

	/***
	 * initialize the object with either default values or the settings read
	 * form file
	 */
	private Configurations() {
		settings = new HashMap<String, String>();

		File configFile = new File(fileName);

		if (configFile.exists()) { // read the config file and set values
			try {
				Scanner reader = new Scanner(configFile);
				while (reader.hasNextLine()) {
					String line = reader.nextLine().trim();

					if (line.startsWith("#")) // skip comment lines ( they start
												// with a # )
						continue;

					settings.put(line.split("=")[0], line.substring(line.indexOf("=") + 1));
				}
				reader.close();
			} catch (Exception x) {
				helpers.ColorHelpers.PrintRed("Error loading config file! exiting");
				System.exit(1);
			}
		} else {
			// write settings to a default file (keys might not be in order)
			try {
				makeDefaults();
				storeSettigns();
			} catch (IOException ex) {
				helpers.ColorHelpers.PrintRed("Error creating config file!");
			}
		}
	}

	/**
	 * Stores settings into the settign file This can be called when program is
	 * runnig to set values for program settings
	 * 
	 * @throws IOException
	 */
	public void storeSettigns() throws IOException {
		File configFile = new File(fileName);
		configFile.createNewFile();
		PrintWriter printer = new PrintWriter(configFile);
		for (String x : keysOrder) {
			if (x.startsWith("#"))
				printer.println(x);
			else
				printer.println(String.format("%s=%s", x, settings.get(x)));
		}
		printer.flush();
		printer.close();
	}

	/**
	 * creates or returns an instance of the configuration class which contains
	 * settings to the program
	 * 
	 * @return
	 */
	public static Configurations getInstance() {
		if (instance == null)
			instance = new Configurations();

		return instance;
	}

	/**
	 * 
	 * creates the sample settings file with default values default values
	 * should be added the map in this function or they will cause errors!
	 * 
	 */
	private void makeDefaults() {
		/// set the key storage order :)
		keysOrder = new ArrayList<String>();
		keysOrder.add("# OQL tool config file, please note all keys have to be present or program will not run!");

		// neo 4 jj stuff
		settings.put(NEO4J_SERVER, "localhost");
		settings.put(NEO4J_USERNAME, "neo4j");
		settings.put(NEO4J_PASSWORD, "neo4j");

		keysOrder.add("# Neo4j access settings");
		keysOrder.add(NEO4J_SERVER);
		keysOrder.add(NEO4J_USERNAME);
		keysOrder.add(NEO4J_PASSWORD);

		// postgress stuff

		settings.put(PG_BDNAME, "SysDigData");
		settings.put(PG_PASSWORD, "postgres");
		settings.put(PG_PORT, "5432");
		settings.put(PG_SERVER, "127.0.0.1");
		settings.put(PG_USERNAME, "postgres");

		keysOrder.add("# Postgres access settings");
		keysOrder.add(PG_SERVER);
		keysOrder.add(PG_PORT);
		keysOrder.add(PG_BDNAME);
		keysOrder.add(PG_USERNAME);
		keysOrder.add(PG_PASSWORD);

		// program variables
		settings.put(BACKWARD_STEPS, "9999999");
		settings.put(FORWARD_STEPS, "9999999");

		keysOrder.add("# Enviroment variable settings");
		keysOrder.add(BACKWARD_STEPS);
		keysOrder.add(FORWARD_STEPS);

	}

	/**
	 * returns the value associated with the key entered
	 * 
	 * @param Key
	 *            the key to get the settings for
	 * @return setting value for the key
	 */
	public String getSetting(String Key) {
		if (settings.containsKey(Key))
			return settings.get(Key);
		else { // if the key is not in the config hash map, settings file was
				// wrong!
			helpers.ColorHelpers.PrintRed("Error loading settings!");
			System.exit(0);
			return "";
		}
	}

	/**
	 * Constant definitions
	 */
	// neo4j stuff
	public static final String NEO4J_USERNAME = "neo4j_username";
	public static final String NEO4J_PASSWORD = "neo4j_password";
	public static final String NEO4J_SERVER = "neo4j_server";
	// postgres stuff
	public static final String PG_USERNAME = "pg_username";
	public static final String PG_PASSWORD = "pg_password";
	public static final String PG_SERVER = "pg_server";
	public static final String PG_PORT = "pg_port";
	public static final String PG_BDNAME = "pg_dbname";
	// program variables
	public static final String BACKWARD_STEPS = "backward_steps";
	public static final String FORWARD_STEPS = "forward_steps";

}
