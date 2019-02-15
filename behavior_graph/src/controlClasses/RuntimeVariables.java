/**
 * 
 */
package controlClasses;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import classes.SysdigRecordObject;
import exceptions.VariableNoitFoundException;

/**
 * @author omid
 *
 *         This class implements a singleton object as a reference for keeeping
 *         runtime enviroments these values can be set using set keywords in the
 *         query.
 *
 */
public class RuntimeVariables {

	private RuntimeVariables() {
		super();

		// set the mappings
		fieldsMap = new HashMap<String, Field>();

		try {

			fieldsMap.put("forward_depth", this.getClass().getDeclaredField("forwardDepth"));
			fieldsMap.put("back_depth", this.getClass().getDeclaredField("backDepth"));
			fieldsMap.put("ignore_fd_num", this.getClass().getDeclaredField("ignoreFDNumber"));
			fieldsMap.put("auto_merge", this.getClass().getDeclaredField("automaticMerge"));

		} catch (Exception ex) {
			ex.printStackTrace();
		}

		// ****** set default values :
		forwardDepth = Integer.MAX_VALUE;
		backDepth = Integer.MAX_VALUE;
		ignoreFDNumber = true;
		automaticMerge = false;
	}

	private Map<String, Field> fieldsMap;

	private static RuntimeVariables instance = null;

	/**
	 * returns the singleton instance of the runtime variable contorller
	 * 
	 * @return
	 */
	public static RuntimeVariables getInstance() {
		if (instance == null) {
			instance = new RuntimeVariables();
		}
		return instance;
	}

	
	private Boolean automaticMerge; 
	
	
	/**
	 * gets the query modules to automatically merge the outcome form queries. 
	 * this will add edges between the query results if any relationship exists. 
	 *  
	 * @return true is results fo multiple querioes should be merged, falase otherwise 
	 */
	public boolean isAutomaticMerge() {
		return automaticMerge;
	}

	/**
	 *  * gets the query modules to automatically merge the outcome form queries. 
	 * this will add edges between the query results if any relationship exists.
	 * @param automaticMerge true is merging of results is desired, false otherwise 
	 */
	public void setAutomaticMerge(boolean automaticMerge) {
		this.automaticMerge = automaticMerge;
	}

	// the depth to which a forward query will enumerate
	private Integer forwardDepth;

	/**
	 * depth to which a forward query should go
	 * 
	 * @return
	 */
	public int getForwardDepth() {
		return this.forwardDepth;
	}

	/**
	 * sets depth to which a forward query should go
	 * 
	 * @param value
	 */
	public void setForwardDepth(int value) {
		this.forwardDepth = value;
	}

	/**
	 * sets the back traversla steps count
	 */
	private Integer backDepth;

	/**
	 * sets number of steps a back track should follow in the tree
	 * 
	 * @param value
	 *            number of steps
	 */
	public void setBackDepth(int value) {
		this.backDepth = value;
	}

	/**
	 * returns number of steps a back track should follow in the tree
	 * 
	 * @return
	 */
	public int getBackDepth() {
		return this.backDepth;
	}

	/**
	 * sets to ignore FD_numbers, this will create a cleaner graph with
	 * ignoreing fd numbers for files
	 */
	private Boolean ignoreFDNumber;

	/**
	 * sets weather fd_number should be ignored in files or not
	 * 
	 * @param value
	 */
	public void setIgnoreFDNumber(boolean value) {
		this.ignoreFDNumber = value;
	}

	/**
	 * retuiens wheather fd_number should be ignored in files or not
	 * 
	 * @return
	 */
	public Boolean getIgnoreFDNumber() {
		return this.ignoreFDNumber;
	}

	/**
	 * autoamtically sets the value for the given key
	 * 
	 * @param Key
	 *            variabe to set
	 * @param Value
	 *            value to set the variable to
	 * @throws VariableNoitFoundException
	 *             thrown when the input key does not match any of the known
	 *             varialbes
	 */
	public void setValue(String Key, String Value) throws VariableNoitFoundException {
		// validate the key
		if (!fieldsMap.containsKey(Key.toLowerCase()))
			throw new VariableNoitFoundException(String.format(
					"You are trying to set a variable '%s' which was not found!Please cehck and try again ... \r\n",
					Key));

		Object TheValue = null;

		switch (fieldsMap.get(Key.toLowerCase()).getType().getName()) {
		case "java.lang.Integer":
			TheValue = Integer.parseInt(Value);
			break;
		case "java.lang.String":
			TheValue = Value;
			break;

		case "java.lang.Boolean":
			TheValue = Boolean.parseBoolean(Value);
			break;
		default:
			break;
		}
		try {
			fieldsMap.get(Key.toLowerCase()).set(this, TheValue);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * gets the value for a given variable
	 * 
	 * @param Key
	 *            the key to find value for
	 * @return string representation of the value associated with that setting
	 * @throws VariableNoitFoundException
	 *             is thrown when the key is not known to the class
	 */
	public String getValue(String Key) throws VariableNoitFoundException {
		// validate the key
		if (!fieldsMap.containsKey(Key.toLowerCase()))
			throw new VariableNoitFoundException(String.format(
					"You are trying to read a variable '%s' which was not found!Please cehck and try again ... \r\n",
					Key));

		Object TheValue = null;

		try {
			return String.format("%s = '%s'", Key.toUpperCase(), fieldsMap.get(Key.toLowerCase()).get(this).toString());
		} catch (Exception ex) {
			ex.printStackTrace();
			return "Not Found!";
		}
	}

}
