/**
 * 
 */
package insertion;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import classes.AccessCall;
import classes.ResourceItem;

/**
 * @author omid
 *
 */
public class ShadowInserter {
	protected ConcurrentLinkedQueue<Object> theObjectQue;
	protected ConcurrentLinkedQueue<AccessCall> theEdgeUpdateQue;
	protected ConcurrentHashMap<Long, Integer> theIndexer;
	protected Thread inserterThread;
	
	/**
	 * 
	 */
	public ShadowInserter() {
		// TODO Auto-generated constructor stub
		theObjectQue = new ConcurrentLinkedQueue<Object>();
		theEdgeUpdateQue = new ConcurrentLinkedQueue<AccessCall>();

		theIndexer = new ConcurrentHashMap<Long, Integer>();

//		inserterThread = new Thread(new AsyncNeo4JInserter(this));
//		inserterThread.start();
	}
	
	
	/**
	 * inserts the graph nodes/ resources in the queue to be added to the graph 
	 * @param node the resource item to be inserted
	 */
	public void insertNode(ResourceItem node) {
		theObjectQue.add(node);
	}

	/**
	 * adds the edge to be inserted this is to be used for new edges
	 * @param edge edge to be inseted
	 */
	public void insertEdge(AccessCall edge) {

		if (!theIndexer.keySet().contains(edge.sequenceNumber)) {
			theIndexer.put(edge.sequenceNumber, 1);
			theObjectQue.add(edge);
		}

	}

	/**
	 * adds the edge to the queue for update. THis should only be used for edges that are set for update not inserts
	 * @param edge the edge to be updated
	 */
	public void setEdgeForUpdate(AccessCall edge) {

		if (!theIndexer.keySet().contains(edge.sequenceNumber)) {
			theIndexer.put(edge.sequenceNumber, 2);
			theObjectQue.add(edge);
		}
		else {
			// TODO : this case should be ignored in this model if other methods are desired this should change
		}
	}

	/**
	 * returns true if there are queris to be run
	 * 
	 * @return true if there are queris to be processed
	 */
	public boolean hasNext() {
		return theObjectQue.size() + theEdgeUpdateQue.size() > 0;
	}
	/**
	 * returns the first query to be processed
	 * 
	 * @return the first query to be run
	 */
	public String getQuery() {
		return null;
	}
	
	/**
	 * returns the total length of the queues 
	 * @return the length of the queues
	 */
	public long getQueLenght() {
		return theObjectQue.size() + theEdgeUpdateQue.size();
	}

	public Thread getWorkerThread() {
		return this.inserterThread;
	}

}
