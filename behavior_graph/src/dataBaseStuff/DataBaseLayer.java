package dataBaseStuff;

import java.lang.Thread.State;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import helpers.Configurations;

public class DataBaseLayer {

	private static Connection _underlyingConnectionObject;

	public static Connection getConnection() throws SQLException {
		if (_underlyingConnectionObject == null) {
			Connection conn = null;

			conn = DriverManager.getConnection(
					String.format("jdbc:postgresql://%s:%s/%s",
							Configurations.getInstance().getSetting(Configurations.PG_SERVER),
							Configurations.getInstance().getSetting(Configurations.PG_PORT),
							Configurations.getInstance().getSetting(Configurations.PG_BDNAME)),
					Configurations.getInstance().getSetting(Configurations.PG_USERNAME),
					Configurations.getInstance().getSetting(Configurations.PG_PASSWORD));
			_underlyingConnectionObject = conn;
		}
		return _underlyingConnectionObject;
	}

	public ResultSet RunSelectQuery(String Query) throws SQLException {

		Statement temp = getConnection().createStatement();

		return temp.executeQuery(Query);

	}

	/// returns number of affected rows.
	public int runUpdateQuery(String Query) throws SQLException {
		Statement temp = getConnection().createStatement();
		return temp.executeUpdate(Query);
	}

	public void ensureDataBase() throws Exception {

		try {
			Statement temp = getConnection().createStatement();
			temp.execute("CREATE DATABASE `droidForensics` ;\n");
			temp.execute("use `droidForensics`;\n");
			temp.execute(
					"CREATE TABLE `AccessCalls` (`db_id` int(11) NOT NULL AUTO_INCREMENT,`id` varchar(500) DEFAULT NULL,`FromID` varchar(500) DEFAULT NULL,`ToID` varchar(500) DEFAULT NULL,`DateTime` varchar(45) DEFAULT NULL,`Command` varchar(45) DEFAULT NULL,`Description` varchar(500) DEFAULT NULL,`args` varchar(500) DEFAULT NULL,`Info` varchar(500) DEFAULT NULL,`OccuranceFactor` int(11) DEFAULT NULL,PRIMARY KEY (`db_id`)) ENGINE=InnoDB DEFAULT CHARSET=latin1;");
			temp.execute(
					"CREATE TABLE `ResourceItems` (`db_id` int(11) NOT NULL AUTO_INCREMENT,`id` varchar(500) DEFAULT NULL,`Title` varchar(500) DEFAULT NULL,`Path` varchar(5000) DEFAULT NULL,`Number` varchar(45) DEFAULT NULL,`Description` varchar(3000) DEFAULT NULL,`Type` varchar(50) DEFAULT NULL,PRIMARY KEY (`db_id`)) ENGINE=InnoDB DEFAULT CHARSET=latin1;");
		} catch (Exception ex) {

		}
	}

	private static Connection _underlyingNeo4jConenction;

	/**
	 * returns a connection of neo4j type
	 * @return
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	public static Connection getNeo4JConnection()
			throws SQLException, ClassNotFoundException, IllegalAccessException, InstantiationException {
//		Class.forName("org.neo4j.jdbc.Driver").newInstance();
		if (_underlyingNeo4jConenction == null) {
			_underlyingNeo4jConenction = DriverManager.getConnection(
					String.format("bolt://%s/",
							Configurations.getInstance().getSetting(Configurations.NEO4J_SERVER)),
					Configurations.getInstance().getSetting(Configurations.NEO4J_USERNAME),
					Configurations.getInstance().getSetting(Configurations.NEO4J_PASSWORD)

			);

		}
		return _underlyingNeo4jConenction;
	}

	// private final String ensureDBSql = +
	// "CREATE TABLE `AccessCalls` (\n" + " `db_id` int(11) NOT NULL
	// AUTO_INCREMENT,\n"
	// + " `id` varchar(500) DEFAULT NULL,\n" + " `FromID` varchar(500) DEFAULT
	// NULL,\n"
	// + " `ToID` varchar(500) DEFAULT NULL,\n" + " `DateTime` varchar(45)
	// DEFAULT NULL,\n"
	// + " `Command` varchar(45) DEFAULT NULL,\n" + " `Description` varchar(500)
	// DEFAULT NULL,\n"
	// + " `args` varchar(500) DEFAULT NULL,\n" + " `Info` varchar(500) DEFAULT
	// NULL,\n"
	// + " `OccuranceFactor` int(11) DEFAULT NULL,\n" + " PRIMARY KEY
	// (`db_id`)\n"
	// + ") ENGINE=InnoDB DEFAULT CHARSET=latin1;\n" + "\n" + "CREATE TABLE
	// `ResourceItems` (\n"
	// + " `db_id` int(11) NOT NULL AUTO_INCREMENT,\n" + " `id` varchar(500)
	// DEFAULT NULL,\n"
	// + " `Title` varchar(500) DEFAULT NULL,\n" + " `Path` varchar(5000)
	// DEFAULT NULL,\n"
	// + " `Number` varchar(45) DEFAULT NULL,\n" + " `Description` varchar(3000)
	// DEFAULT NULL,\n"
	// + " `Type` varchar(50) DEFAULT NULL,\n" + " PRIMARY KEY (`db_id`)\n"
	// + ") ENGINE=InnoDB DEFAULT CHARSET=latin1;";
	//
	;

}
