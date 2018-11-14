/**
 * 
 */
package Helpers;

import java.util.HashMap;

/**
 * @author omid
 *
 */
public class Configurations {

	private static String fileName = "oql.conf";
	private static Configurations instance;

	private HashMap<String , String> settings ;

	
	/***
	 * initialize the object with either default values or the settings read form file
	 */
	private Configurations() {
		settings = new HashMap<String ,String>();
		
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
	 * creates the sample settings file with default values default values should be
	 * added the map in this function or they will cause errors!
	 * 
	 */
	private void makeDefaultFile() {

	}

	/**
	 * returns the value associated with the key entered
	 * 
	 * @param Key the key to get the settings for
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
