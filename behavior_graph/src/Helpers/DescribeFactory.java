package Helpers;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import Classes.AccessCall;

/***
 * 
 * @author omido
 * 
 *         this class acts as a intermediary to read the descriptions from the
 *         memory
 */

public class DescribeFactory {
	public static void doDescribe(String FilePath, boolean isAggregated, String sortBy) {
		ArrayList<AccessCall> theList = BaseMemory.edges_for_describe;

		// describe Magic :
		Collections.sort(theList, new Comparator<AccessCall>() {

			@Override
			public int compare(AccessCall o1, AccessCall o2) {
				int ret = 0;
				if (sortBy == null) {
					Long o1_l = o1.sequenceNumber;
					Long o2_l = o2.sequenceNumber;
					ret = o1_l.compareTo(o2_l);

				} else if (sortBy.equalsIgnoreCase("pid")) {
					ret = o1.From.Number.compareTo(o2.From.Number);
				} else if (sortBy.equalsIgnoreCase("pname")) {
					ret = o1.From.Title.compareTo(o2.From.Title);
					if (ret == 0)
						ret = o1.From.Number.compareTo(o2.From.Number);
				} else if (sortBy.equalsIgnoreCase("seq")) {
					Long o1_l = o1.sequenceNumber;
					Long o2_l = o2.sequenceNumber;
					ret = o1_l.compareTo(o2_l);
				} else if (sortBy.equalsIgnoreCase("fname")) {
					ret = o1.To.Title.compareTo(o2.To.Title);
					if (ret == 0)
						ret = o1.To.Number.compareTo(o2.To.Number);
				}

				return ret;

			}
		});

		String printFormat = "";
		if (FilePath == null) {
			printFormat = isAggregated ? "%1$14d) %2$s : %3$s(%4$s) --%5$s--> %6$s ||%7$12d times"
					: "%1$14d) %2$s : %3$s(%4$s) --%5$s--> %6$s";
		} else
			printFormat = " { \"sequence_number\": %1$d, \"user\": \"%2$s\", \"pid\": %3$s , \"proc_name\": \"%4$s\", \"evt_type\": \"%5$s\", \"file_name\": \"%6$s\" ,  \"count\": %7$d}";

		PrintStream printer = null;

		if (FilePath == null) {
			printer = System.out;
		} else {
			try {
				// System.out.println(FilePath);
				File theFile = new File(FilePath.trim());
				theFile.createNewFile();
				printer = new PrintStream(theFile);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		int count = 1;
		printer.println("[ ");
		for (int index = 0; index < theList.size(); index++) {

			AccessCall pick = theList.get(index);

			// if non-verbose is desired flat the requests
			if (isAggregated && index > 1 && pick.From == theList.get(index - 1).From
					&& pick.To == theList.get(index - 1).To && pick.Command.equals(theList.get(index - 1).Command)
					&& pick.user_name.equals(theList.get(index - 1).user_name)) {
				count++;
				continue;
			}

			if (index != 0 && FilePath != null)
				printer.println(",");

			printer.println(String.format(printFormat, pick.sequenceNumber, pick.user_name,
					pick.From.getID().split("\\|")[0], pick.From.getID().split("\\|")[1], pick.Command,

					pick.To.getID().contains("|") ? pick.To.getID().split("\\|")[1] : pick.To.getID(), count));
			count = 1;
		}
		printer.println("]");
	}
}
