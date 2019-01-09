/**
 * 
 */
package exceptions;

/**
 * @author omid
 *
 *         This exception is used to show a value is missing from a class
 *
 */
public class MissingValuesException extends Exception {

	public MissingValuesException() {
		super();
	}

	public MissingValuesException(String msg) {
		super(msg);
	}

}
