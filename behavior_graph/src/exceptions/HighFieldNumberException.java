/**
 * 
 */
package exceptions;

/**
 * @author omido
 *
 *         This exception is to be thrown when number of items in the govven
 *         aray is more than those expected
 *
 */
public class HighFieldNumberException extends Exception {

	public HighFieldNumberException() {
		super();
	}

	public HighFieldNumberException(String message) {
		super(message);
	}
}
