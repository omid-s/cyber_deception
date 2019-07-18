/**
 * 
 */
package querying;

import javax.swing.JFrame;

import classes.AccessCall;
import classes.ResourceItem;
import edu.uci.ics.jung.graph.Graph;
import mainPackage.GraphPanel;
import querying.adapters.BaseAdapter;
import querying.adapters.memory.InMemoryAdapter;
import querying.parsing.*;

/**
 * @author omid
 *
 */
public class AsyncQueryRunner implements Runnable {

	private int ID = -1;
	private String command;
	private boolean stoped = false;
	private boolean showWindow = false;
	private boolean describe = false;

	/**
	 * 
	 */
	public AsyncQueryRunner(int ID, String command) {
		this.ID = ID;
		this.command = command;
		showWindow = false;
	}

	public AsyncQueryRunner(int ID, String command, boolean showWindow) {
		this.ID = ID;
		this.command = command;
		this.showWindow = showWindow;
	}

	public AsyncQueryRunner(int ID, String command, boolean showWindow, boolean describe) {
		this.ID = ID;
		this.command = command;
		this.showWindow = showWindow;
		this.describe = describe;
	}

	public void stop() {
		this.stoped = true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {

		BaseAdapter queryMachine = InMemoryAdapter.getSignleton();
		Graph<ResourceItem, AccessCall> theLocalGraph = null;

		int graphItemsCount = 0;
		GraphPanel theGraphWindow = null;
		JFrame frame1 = new JFrame();
		try {
			ParsedQuery query = QueryInterpreter.interpret(command, theLocalGraph);
			while (!stoped) {

				theLocalGraph = queryMachine.runQuery(query);
				if (theLocalGraph.getEdgeCount() + theLocalGraph.getVertexCount() > graphItemsCount) {

					if (showWindow) {
						theGraphWindow = new GraphPanel(theLocalGraph);
						theGraphWindow.setPrint(false);
						if (frame1.isVisible()) {
							frame1.setVisible(false);
							frame1.dispose();
						}

						frame1 = new JFrame();
						frame1.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
						frame1.setSize(400, 400);

						theGraphWindow.vv.repaint();
						frame1.add(theGraphWindow);
						frame1.setVisible(true);
						frame1.setExtendedState(JFrame.MAXIMIZED_BOTH);
						frame1.setTitle("Autiomated Query ID:" + this.ID);
					}
					graphItemsCount = theLocalGraph.getEdgeCount() + theLocalGraph.getVertexCount();
				}
				Thread.sleep(1000);
			}

		} catch (Exception ex) {

		}
		// TODO Auto-generated method stub

	}

}
