/**
 * 
 */
package ControlClasses;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import Classes.SysdigRecordObject;
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

		} catch (Exception ex) {
			ex.printStackTrace();
		}

		// ****** set default values :
		forwardDepth = Integer.MAX_VALUE;
		backDepth = Integer.MAX_VALUE;
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
