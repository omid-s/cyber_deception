/**
 * 
 */
package MainPackage;

import Classes.ResourceType;
import ControlClasses.RuntimeVariables;
import DataBaseStuff.DataBaseLayer;

/**
 * @author omido
 *
 */
public class testMain {
	public static void main(String argv[]) throws Exception {
//		DataBaseLayer db = new DataBaseLayer();
//		db.ensureDataBase();
//
//		ResourceType T  = ResourceType.valueOf("Activity");
//		System.out.print(T.toString());	
		
		RuntimeVariables v = RuntimeVariables.getInstance();
		
		v.setValue("forward_depth", "123");
		System.out.println( v.getValue("forward_depth") );
		
	}
}
