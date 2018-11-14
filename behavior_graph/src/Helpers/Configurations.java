/**
 * 
 */
package Helpers;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Scanner;

/**
 * @author omid
 *
 */
public class Configurations {

	private static String fileName = "oql.conf";
	private static Configurations instance;

	private HashMap<String, String> settings;

	/***
	 * initialize the object with either default values or the settings read
	 * form file
	 */
	private Configurations() {
		settings = new HashMap<String, String>();

		File configFile = new File(fileName);

		if (configFile.exists()) {
			try {
				Scanner reader = new Scanner(configFile);
				while (reader.hasNextLine()) {
					String line = reader.nextLine().trim();
					
					if ( line.startsWith("#") ) // skip comment lines ( they start with a # )
						continue;
					
					settings.put(line.split("=")[0], line.substring(line.indexOf("=") + 1));
				}
				reader.close();
			} catch (Exception x) {
				Helpers.ColorHelpers.PrintRed("Error loading config file! exiting");
				System.exit(1);
			}
		} else {
			// write settings to a default file (keys might not be in order)
			try {
				makeDefaults();
				configFile.createNewFile();
				PrintWriter printer = new PrintWriter(configFile);
				for (String x : settings.keySet()) {
					printer.println(String.format("%s=%s", x, settings.get(x)));
				}
				printer.flush();
				printer.close();
			} catch (IOException ex) {
				Helpers.ColorHelpers.PrintRed("Error creating config file!");
			}
		}

		/// TODO : add check for setuop file existnace
		/// TODO : create the settings file if it dfoes not exist
		/// TODO : read the settings off ofg the setup file
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
		// neo 4 jj stuff
		settings.put(NEO4J_SERVER, "localhost");
		settings.put(NEO4J_USERNAME, "neo4j");
		settings.put(NEO4J_PASSWORD, "neo4j");

		// postgress stuff

		settings.put(PG_BDNAME, "SysDigData");
		settings.put(PG_PASSWORD, "postgres");
		settings.put(PG_PORT, "5432");
		settings.put(PG_SERVER, "127.0.0.1");
		settings.put(PG_USERNAME, "postgres");
	}

	/**
	 * returns the value associated with the key entered
	 * 
	 * @param Key
	 *            the key to get the settings for
	 * @return setting value for the key
	 */
	public String getSetting(String Key) {
		return "";
	}

	/**
	 * Constant definitions
	 */
	// neo4j stuff
	public final String NEO4J_USERNAME = "neo4j_username";
	public final String NEO4J_PASSWORD = "neo4j_password";
	public final String NEO4J_SERVER = "neo4j_server";
	// postgres stuff
	public final String PG_USERNAME = "pg_username";
	public final String PG_PASSWORD = "pg_password";
	public final String PG_SERVER = "pg_server";
	public final String PG_PORT = "pg_port";
	public final String PG_BDNAME = "pg_dbname";

}
