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
	 * 
	 */
	public AsyncNeo4JInserter(ShadowDBInserter theBuffer) {
		// TODO Auto-generated constructor stub
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
		long driverFlush = Long.parseLong(Configurations.getInstance().getSetting(Configurations.DRIVER_FLUSH));
		long transactionFlush = Long
				.parseLong(Configurations.getInstance().getSetting(Configurations.TRANSACTION_FLUSH));
		;

		while (true) {

			try {

				while (true) {

					if (driverFlush > 0 && counter == driverFlush) {
						driver.closeAsync();
						reloadConnection(true);
						counter = 0;
					}
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
										if (transactionFlush >= 0 && counter % transactionFlush == 0)
											break;
									} else {
										try {
											Thread.sleep(100);
										} catch (Exception ex) {
										}
									}
								}

								return (int) counter;
							}
						});

					}
					counter++;

//						session.run(lastQuery);

//						trnx.run(lastQuery);
//						counter++;
//						if (counter % 100=0) {
//							trnx.success();
////							System.out.println("-0");
//							trnx.commitAsync().thenRun((Runnable) session.closeAsync());
////							System.out.println("-1");
////							session.closeAsync();
////							System.out.println("-2");
////							driver.closeAsync();
////							System.gc();
////							return ;
//							break;
//						}
//						if( counter % 1000==0 ) {
//						
//							trnx.
//							
//						}

				}
//				System.out.println("-3");
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
