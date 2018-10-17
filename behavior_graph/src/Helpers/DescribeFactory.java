package Helpers;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;

import Classes.AccessCall;

public class DescribeFactory {
	public static void doDescribe(String FilePath) {
		ArrayList<AccessCall> theList = BaseMemory.edges_for_describe;

		PrintStream printer = null;

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

		for (AccessCall pick : theList) {
			System.out.println(String.format("%14d) %s : %s --%s--> %s ", pick.sequenceNumber, pick.user_name,
					pick.From.getID(), pick.Command, pick.To.getID()));
		}

	}
}
