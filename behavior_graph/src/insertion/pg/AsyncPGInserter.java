/**
 * 
 */
package insertion.pg;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.StringJoiner;
import controlClasses.Configurations;
import dataBaseStuff.DataBaseLayer;
import insertion.IAsyncInserter;
import insertion.ShadowInserter;

/**
 * @author omid
 *
 */
public class AsyncPGInserter implements IAsyncInserter, Runnable {
	private ShadowInserter __theBuffer;

	/**
	 * 
	 */
	public AsyncPGInserter(ShadowInserter theBuffer) {
		this.__theBuffer = theBuffer;
	}

	private Connection conn = null;
	private String lastQuery;

	protected static StringJoiner items = new StringJoiner(" ");

	@Override
	public void run() {
		long counter = 0;
		long transactionFlush = Long
				.parseLong(Configurations.getInstance().getSetting(Configurations.TRANSACTION_FLUSH));

		while (true) {
			if (__theBuffer.hasNext()) {
				lastQuery = __theBuffer.getQuery();
				items.add(lastQuery);
				counter++;

				// if the tracation flush is meet, create a new trasaction
				if (transactionFlush >= 0 && counter % transactionFlush == 0) {
					try {
						DataBaseLayer DL = new DataBaseLayer();
						DL.runUpdateQuery(items.toString());
						items = new StringJoiner(" ");
						counter=0;
					} catch (Exception ex) {
						helpers.ColorHelpers.PrintRed(ex.getMessage() + "\n");
					}
				}
			} else {
				try {
					Thread.sleep(100);
				} catch (Exception ex) {
					// ignore thread sleep errors
				}
			}
		}
	}

	@Override
	public void reloadConnection(boolean byForce) {
		// TODO Auto-generated method stub
		if (conn == null) {
			try {
				conn = DriverManager.getConnection(
						String.format("jdbc:postgresql://%s:%s/%s",
								Configurations.getInstance().getSetting(Configurations.PG_SERVER),
								Configurations.getInstance().getSetting(Configurations.PG_PORT),
								Configurations.getInstance().getSetting(Configurations.PG_BDNAME)),
						Configurations.getInstance().getSetting(Configurations.PG_USERNAME),
						Configurations.getInstance().getSetting(Configurations.PG_PASSWORD));
			} catch (Exception ex) {
				helpers.ColorHelpers.PrintRed("Error connecting to server, exiting!");
//				System.exit();
			}
		}

	}

}
