/**
 * 
 */
package mainPackage;

import classes.ResourceType;
import controlClasses.RuntimeVariables;
import dataBaseStuff.DataBaseLayer;
import helpers.Configurations;

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
		
//		RuntimeVariables v = RuntimeVariables.getInstance();
//		
//		v.setValue("forward_depth", "123");
//		System.out.println( v.getValue("forward_depth") );
//		
		
//		System.out.println("12940|sshd".split("\\|")[0]);
		
		System.out.println(  Configurations.getInstance().getSetting(Configurations.NEO4J_USERNAME));
		
	}
}
