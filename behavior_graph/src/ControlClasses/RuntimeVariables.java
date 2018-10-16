/**
 * 
 */
package ControlClasses;

/**
 * @author omid
 *
 */
public class RuntimeVariables {

	private RuntimeVariables(){
		super();
	}
	
	private static RuntimeVariables instance = null; 

	/**
	 * returns the singleton instance of the runtime variable contorller
	 * @return
	 */
	public static RuntimeVariables getInstance(){
		if( instance ==null ){
			instance = new RuntimeVariables();
		}
		return instance;
	}
	
}
