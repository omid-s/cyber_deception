package classes;

import java.sql.Date;

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

	public boolean isEqual(AccessCall theOther) {
		AccessCall tmp = (AccessCall) theOther;
		return this.From == tmp.From && this.To == tmp.To && this.Command.equals(tmp.Command);
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
						+ "args:\"%s\", info:\"%s\", user_id:\"%s\" , user_name:\"%s\"  }",
						String.valueOf(Command), 
						String.valueOf(Command), 
						String.valueOf(DateTime), 
						"",//String.valueOf(Description), 
						"",//String.valueOf(args), 
						String.valueOf(Info), 
						String.valueOf(user_id), 
						String.valueOf(user_name)
				
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
}
