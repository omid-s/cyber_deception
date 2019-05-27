package classes;

import java.sql.Date;
import java.util.ArrayList;

public class AccessCall extends Object {
	public String id;
	public ResourceItem From;
	public ResourceItem To;
	public String DateTime;
	public String Command;
	public String Description;
	public String direction;
	public String args;
	public String Info;
	public String user_id;
	public String user_name;
	public int OccuranceFactor = 1;
	public long sequenceNumber;
	public String computer_ID;
	public ArrayList<Long> times;

	public boolean isEqual(AccessCall theOther) {
		AccessCall tmp = (AccessCall) theOther;
		return  this.From == tmp.From && this.To == tmp.To && this.Command.equals(tmp.Command);
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return id + " | from: " + this.From.toString() + " | to :" + this.To.toString() + " | cooamnd:" + Command
				+ " | user:" + user_name;
	}

	/**
	 * returns the Neo4J string representation of the object
	 * 
	 * @return the string representation of the access call
	 */
	public String toN4JObjectString() {
		return String.format(
				"%s{command:\"%s\",date:\"%s\", description:\"%s\" , "
						+ "args:\"%s\", info:\"%s\", user_id:\"%s\" , user_name:\"%s\", computer_id:\"%s\"  }",
				String.valueOf(Command), String.valueOf(Command), String.valueOf(DateTime), "", // String.valueOf(Description),
				"", // String.valueOf(args),
				String.valueOf(Info), String.valueOf(user_id), String.valueOf(user_name), String.valueOf(computer_ID)

		).replace("\\", "");

	}

	@Override
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		if (obj == null || obj.getClass() != this.getClass())
			return false;

		return this.toString().equals(obj.toString()); // .
														// this.id.equals(((AccessCall)
														// obj).id) ;
	}

	/**
	 * adds the given sequence counter to the edge
	 * 
	 * @param sequenceNumber
	 * @param mode           0- no compression , 1- will keep only the edge and adds
	 *                       seq counters 2- will keep only first and last 3- will
	 *                       keep just the first
	 */
	public void addTime(long sequenceNumber, int mode) {
		if (times == null)
			times = new ArrayList<Long>();
		switch (mode) {
		case 1: 
			// both will be kept
			times.add(sequenceNumber);
			break;
		case 2:
			// the beggining and end will be kept
			switch (times.size()) {
			case 0:
			case 1:
				times.add(sequenceNumber);
				break;
			case 2:
				times.remove(1);
				times.add(sequenceNumber);
				break;

			default:
				break;
			}
			break;
		case 3:
			// onely one instace is to be kept
			switch (times.size()) {
			case 0:
				times.add(sequenceNumber);
				break;
			case 1:
				times.remove(0);
				times.add(sequenceNumber);
				break;

			default:
				break;
			}

		}
	}

}
