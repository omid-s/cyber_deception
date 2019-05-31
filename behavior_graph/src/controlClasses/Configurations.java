/**
 * 
 */
package controlClasses;

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
	 * initialize the object with either default values or the settings read form
	 * file
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
	 * creates the sample settings file with default values default values should be
	 * added the map in this function or they will cause errors!
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

		// program configurations

		settings.put(LINE_SEPERATOR, "=&amin&=");
		settings.put(PRINT_QUERY, "false");
		settings.put(TRANSACTION_FLUSH, "10");
		settings.put(DRIVER_FLUSH, "1000");

		keysOrder.add("# Internal program configurations");
		keysOrder.add(LINE_SEPERATOR);
		keysOrder.add(PRINT_QUERY);
		keysOrder.add(TRANSACTION_FLUSH);
		keysOrder.add(DRIVER_FLUSH);

	}

	/**
	 * returns the value associated with the key entered
	 * 
	 * @param Key the key to get the settings for
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
	 * Adds a settings key to the settings
	 * 
	 * @param Key   the key
	 * @param Value the value assigned
	 */
	public void setSetting(String Key, String Value) {
		settings.put(Key, Value);
	}

	/**
	 * Returns a list of fields that are considered in the short format
	 * 
	 * @return the list of fields in short format
	 */
	public static String[] getShortFieldList() {
		final String[] ret = { "evt_datetime", "evt_type", "thread_tid", "proc_name", "proc_args", "proc_cwd",
				"proc_cmdline", "proc_pname", "proc_pid", "proc_ppid", "fd_cip", "fd_cport", "fd_directory",
				"fd_filename", "fd_ip", "fd_name", "fd_num", "fd_sip", "fd_sockfamily", "fd_sport", "fd_type",
				"fd_typechar", "user_name", "user_uid", "evt_num", "evt_args", "user_shell" };

		return ret;
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

	// program configuration
	public static final String LINE_SEPERATOR = "line_seperator";
	public static final String PRINT_QUERY = "print_query";
	public static final String LEGACY_MODE = "legacy_mode";
	public static final String SHADOW_INSERTER = "shadow_inserter";
	public static final String COMPRESSSION_LEVEL = "compression_level";
	public static final String TRANSACTION_FLUSH = "trasaction_flush";
	public static final String DRIVER_FLUSH = "driver_flush";
	public static final String EVAL_CLEAR_DB = "eval_clear_db";
	public static final String COMPUTER_ID = "computer_id";

}
