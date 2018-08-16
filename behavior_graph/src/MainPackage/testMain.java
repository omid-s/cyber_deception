/**
 * 
 */
package MainPackage;

import Classes.ResourceType;
import DataBaseStuff.DataBaseLayer;

/**
 * @author omido
 *
 */
public class testMain {
	public static void main(String argv[]) throws Exception {
//		DataBaseLayer db = new DataBaseLayer();
//		db.ensureDataBase();

		ResourceType T  = ResourceType.valueOf("Activity");
		System.out.print(T.toString());		
		
	}
}
