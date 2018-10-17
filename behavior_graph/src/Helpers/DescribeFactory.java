package Helpers;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;

import Classes.AccessCall;

/***
 * 
 * @author omido
 * 
 *         this class acts as a intermediary to read the descriptions from the
 *         memory
 */

public class DescribeFactory {
	public static void doDescribe(String FilePath, boolean isAggregated) {
		ArrayList<AccessCall> theList = BaseMemory.edges_for_describe;

		String printFormat = isAggregated ? "%14d) %s : %s --%s--> %s ||%12d times" : "%14d) %s : %s --%s--> %s";

		PrintStream printer = null;

//		System.out.print(FilePath);
		
		if (FilePath == null) {
			printer = System.out;
		} else {
			try {
				System.out.println(FilePath);
				File theFile = new File(FilePath.trim());
				theFile.createNewFile();
				printer = new PrintStream(theFile);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		int count = 1;
		for (int index = 0; index < theList.size(); index++) {
			AccessCall pick = theList.get(index);

			// if non-verbose is desired flat the requests
			if (isAggregated && index > 1 && pick.From == theList.get(index - 1).From
					&& pick.To == theList.get(index - 1).To && pick.Command.equals(theList.get(index - 1).Command)
					&& pick.user_name.equals(theList.get(index - 1).user_name)) {
				count++;
				continue;
			}  

			printer.println(String.format(printFormat, pick.sequenceNumber, pick.user_name, pick.From.getID(),
					pick.Command, pick.To.getID()
					, count
					));
			count = 1 ; 
		}
	}
}
