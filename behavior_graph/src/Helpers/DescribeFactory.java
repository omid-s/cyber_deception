package Helpers;

import java.util.ArrayList;

import Classes.AccessCall;

public class DescribeFactory {
	public static void doDescribe(String FilePath) {
		ArrayList<AccessCall> theList = BaseMemory.edges_for_describe;
		
		for ( AccessCall pick : theList ){
			System.out.println( String.format("%12d) %s : %s --%s--> %s ",
					pick.sequenceNumber,	
					pick.user_name, 
						pick.From.getID(),
						pick.Command,
						pick.To.getID()
					) );
		}
		
	}
}
