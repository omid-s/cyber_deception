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
			try {
			ParsedQuery query = QueryInterpreter.interpret(command, theLocalGraph);
			GraphPanel oldPanel = null;
			
			while (!stoped) {

				theLocalGraph = queryMachine.runQuery(query);
				if (theLocalGraph.getEdgeCount() + theLocalGraph.getVertexCount() > graphItemsCount) {

					if (showWindow) {
						oldPanel = this.pop_up_window(theLocalGraph, oldPanel);
					}
					graphItemsCount = theLocalGraph.getEdgeCount() + theLocalGraph.getVertexCount();
				}
				Thread.sleep(2000);
			}

		} catch (Exception ex) {

		}
	}
	
//	TODO : create a method to pop up a window or set AI variables 
	
	private GraphPanel pop_up_window(Graph<ResourceItem, AccessCall> theLocalGraph, GraphPanel oldPanel) {
		GraphPanel theGraphWindow = null;
		JFrame frame1 = null;// new JFrame();

		theGraphWindow = new GraphPanel(theLocalGraph, false);
		theGraphWindow.setPrint(false);


		if (frame1 == null) {
			frame1 = new JFrame();
			frame1.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			frame1.setSize(400, 400);

			frame1.setExtendedState(JFrame.MAXIMIZED_BOTH);
			frame1.setTitle("Autiomated Query ID:" + this.ID);
		frame1.setVisible(true);
		}
		if (oldPanel != null)
			frame1.remove(oldPanel);
		
		frame1.add(theGraphWindow);
		theGraphWindow.vv.repaint();
		frame1.repaint();	
		frame1.pack();

		return theGraphWindow;
	}

}
