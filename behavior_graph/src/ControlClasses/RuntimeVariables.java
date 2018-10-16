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
		
		// ****** set default values :
		forwardDepth = -1; 
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
	
	// the depth to which a forward query will enumerate
	private int forwardDepth;
	
	/**
	 * depth to which a forward query should go
	 * @return
	 */
	public int getForwardDepth(){
		return this.forwardDepth;
	}
	
	/**
	 * sets depth to which a forward query should go 
	 * @param value
	 */
	public void setForwardDepth( int value ){
		this.forwardDepth= value;
	}
	
	
}
