/**
 * 
 */
package exceptions;

/**
 * @author omido
 * 
 *         this exception is used to notify of errors in query entered by user
 */
public class QueryFormatException extends Exception {

	public QueryFormatException(String message) {
		super(message);
	}

	public QueryFormatException() {
		super();
	}

}
