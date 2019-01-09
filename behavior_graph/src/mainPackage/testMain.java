/**
 * 
 */
package mainPackage;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import classes.ResourceType;
import classes.SysdigRecordObject;
import controlClasses.RuntimeVariables;
import dataBaseStuff.DataBaseLayer;
import dataBaseStuff.SysdigObjectDAL;
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
		
//		System.out.println(  Configurations.getInstance().getSetting(Configurations.NEO4J_USERNAME));
//		
//		Connection theConnection = DataBaseLayer.getConnection();
//		
//		String Query = "select * from sysdigoutput limit 5";
//		Statement st  = theConnection.createStatement();
//		ResultSet resutls =  st.executeQuery(Query);
//		
//		
//		SysdigObjectDAL objectDAL = new SysdigObjectDAL(false);
//		
//		while( resutls.next() ){
//			SysdigRecordObject temp = objectDAL.LoadFromResultSet(resutls);
//			System.out.println(temp.toString());
//		}
//		
		String Fields = "";
		
		for ( String x : Configurations.getShortFieldList() ) {
			if (Fields.length() > 0)
				Fields+= ",";
		
			Fields+=x;
		}
		
		System.out.println( Fields );
		
	}
}
