/**
 * 
 */
package insertion.pg;

import classes.AccessCall;
import classes.ResourceItem;
import insertion.ShadowInserter;

/**
 * @author omid
 *
 */
public class ShadowPGInserter extends ShadowInserter {
	private static ShadowPGInserter __Instance = null; // holds the singleton instance of shadow inserter

	/**
	 * returns the singleton inserter object.
	 * 
	 * @return the inserter object
	 */
	public static ShadowPGInserter getInstance() {
		if (__Instance == null)
			__Instance = new ShadowPGInserter();

		return __Instance;
	}

	/**
	 * 
	 */
	public ShadowPGInserter() {
		super();
		inserterThread = new Thread(new AsyncPGInserter(this));
		inserterThread.start();

	}

	/**
	 * returns the first query to be processed
	 * 
	 * @return the first query to be run
	 */
	@Override
	public String getQuery() {
		Object ret = theObjectQue.poll();
		if (ret instanceof String)
			return (String) ret;
		else if (ret instanceof AccessCall) {
			AccessCall edge = (AccessCall) ret;

			// remove the item from indexer list
			theIndexer.remove(edge.sequenceNumber);

			return edge.toPGInsertString();
		} else if (ret instanceof ResourceItem) {
			ResourceItem node = (ResourceItem) ret;

			return node.toPGInsertString();

		}
		return "";
	}

}
