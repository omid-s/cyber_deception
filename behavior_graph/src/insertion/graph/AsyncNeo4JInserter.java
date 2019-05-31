/**
 * 
 */
package insertion.graph;

import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.TransactionWork;

import controlClasses.Configurations;

/**
 * @author omid
 *
 */
public class AsyncNeo4JInserter implements Runnable {

	private ShadowDBInserter __theBuffer;

	/**
	 * creates an instance
	 */
	public AsyncNeo4JInserter(ShadowDBInserter theBuffer) {
		this.__theBuffer = theBuffer;
	}

	private Driver driver = null;
	private String lastQuery;

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {

		reloadConnection(false);
		long counter = 0;
		long counter2 = 0;
		long driverFlush = Long.parseLong(Configurations.getInstance().getSetting(Configurations.DRIVER_FLUSH));
		long transactionFlush = Long
				.parseLong(Configurations.getInstance().getSetting(Configurations.TRANSACTION_FLUSH));

		while (true) {

			try {

				while (true) {

					// refresh the driver instance if it's time
					if (driverFlush > 0 && counter == driverFlush) {
						driver.closeAsync();
						reloadConnection(true);
						counter = 0;
						counter2++;

//						// ## clean the database out if parameter is set
						if (counter2 == 10 && Boolean
								.valueOf(Configurations.getInstance().getSetting(Configurations.EVAL_CLEAR_DB))) {
							counter2 = 0;
							try (Session session = driver.session()) {
								session.writeTransaction(new TransactionWork<Integer>() {
									@Override
									public Integer execute(Transaction tx) {
										tx.run("MATCH (n) DETACH DELETE n ;");
										System.out.println("------------------deleted------------------\n");
										return 1;
									}
								});
							}
						}
					}
					// create a sessin and insert a set of values
					try (Session session = driver.session()) {
						session.writeTransaction(new TransactionWork<Integer>() {
							@Override
							public Integer execute(Transaction tx) {
								int counter = 0;
								while (true) {
									if (__theBuffer.hasNext()) {
										lastQuery = __theBuffer.getQuery();
										tx.run(lastQuery);
										counter++;

										// if the tracation flush is meet, create a new trasaction
										if (transactionFlush >= 0 && counter % transactionFlush == 0)
											break;
									} else {  
										try {
											Thread.sleep(100);
										} catch (Exception ex) {
											// ignore thread sleep errors
										}
									}
								}

								return (int) counter;
							}
						});

					}

					counter++;

				}
			} catch (Exception ex) {

				System.gc();
				System.out.print(ex.getMessage());
				ex.printStackTrace();
				driver.closeAsync();
				reloadConnection(true);

			}
		}
	}

	private void reloadConnection(boolean byForce) {
		try {
			if (driver == null || byForce)
				driver = GraphDatabase.driver(
						"bolt://" + Configurations.getInstance().getSetting(Configurations.NEO4J_SERVER),
						AuthTokens.basic(Configurations.getInstance().getSetting(Configurations.NEO4J_USERNAME),
								Configurations.getInstance().getSetting(Configurations.NEO4J_PASSWORD)));

		} catch (Exception ex) {
			helpers.ColorHelpers.PrintRed("Error connecting to server, exiting!");
			System.exit(1);
		}

	}

}
