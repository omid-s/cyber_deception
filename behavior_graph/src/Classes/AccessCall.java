package Classes;

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
