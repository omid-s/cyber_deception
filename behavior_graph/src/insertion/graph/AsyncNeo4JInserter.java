/**
 * 
 */
package insertion.graph;

import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.Transaction;

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

		while (true) {
			long counter = 0;
			boolean x = true;
			try {
				reloadConnection(true);
				Session session = driver.session();

				Transaction trnx = session.beginTransaction();

				while (session.isOpen()) {
					if (x) {
						System.out.println("+");
						x = false;
					}
					if (__theBuffer.hasNext()) {
						lastQuery = __theBuffer.getQuery();
						trnx.run(lastQuery);
						counter++;
						if (counter == 100000) {
							trnx.success();
							System.out.println("-0");
//							trnx.close();
							System.out.println("-1");
							session.closeAsync();
							System.out.println("-2");
							driver.closeAsync();
							System.gc();
							break;
						}
					} else {
						Thread.sleep(100);
					}
				}
				System.out.println("-3");
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
