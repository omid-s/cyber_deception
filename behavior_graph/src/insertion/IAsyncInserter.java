/**
 * 
 */
package insertion;

/**
 * @author omid
 *
 */
public interface IAsyncInserter {
	
	public void run();

	public void reloadConnection(boolean byForce);
}
