/**
 * 
 */
package dataBaseStuff;

import java.sql.ResultSet;

import classes.AccessCall;
import classes.ResourceItem;
import classes.ResourceType;
import querying.adapters.memory.BaseMemory;

/**
 * @author omido
 *
 */
public class AccessCallObjectDal {
	private void Insert(AccessCall inp) throws Exception {

		String Query = " INSERT INTO `droidForensics`.`AccessCalls` (`id`,`FromID`,`ToID`,`DateTime`,"
				+ "`Command`,`Description`,`args`,`Info`,`OccuranceFactor`) VALUES";
				
		Query += "'" + inp.id + "',";
		Query += "'" + inp.From.id + "',";
		Query += "'" + inp.To.id + "',";
		Query += "'" + inp.DateTime + "',";
		Query += "'" + inp.Command+ "',";
		Query += "'" + inp.Description + "',";
		Query += "'" + inp.args + "',";
		Query += "'" + inp.Info+ "',";
		Query += "'" + inp.OccuranceFactor + "');";

		DataBaseLayer db = new DataBaseLayer();
		db.runUpdateQuery(Query);
	}
	private void ReadAll() throws Exception {
		String query = "select * from `droidForensics`.`AccessCalls`;";
		DataBaseLayer db = new DataBaseLayer();
		ResultSet ret = db.RunSelectQuery(query);
		while (ret.next()) {
			itemFromDBRow(ret, true);
		}
	}

	private void ReadSome( String Criteria ) throws Exception{
		String query = "select * from `droidForensics`.`AccessCalls` where "+ Criteria +";";
		DataBaseLayer db = new DataBaseLayer();
		ResultSet ret = db.RunSelectQuery(query);
		while (ret.next()) {
			itemFromDBRow(ret, true);
		}	
	}
	
	private AccessCall itemFromDBRow(ResultSet inp, boolean saveToMem) throws Exception {
		AccessCall ret = new AccessCall();
		BaseMemory mem = BaseMemory.getSignleton();
		
		ret.id = inp.getString("id");
		ret.DateTime = inp.getString("DateTime");
		ret.Command = inp.getString("Command");
		ret.args = inp.getString("args");
		ret.Description = inp.getString("Description");
		ret.Info = inp.getString("Info");
		ret.OccuranceFactor = inp.getInt("OccuranceFactor");
		String FromID = inp.getString("FromID");
		String ToID = inp.getString("ToID");

		ret.To =mem.getResourceItem(ToID);
		ret.From =mem.getResourceItem(FromID);
		
		if (saveToMem) {
				mem.addAccessCall(ret);
		}
		return ret;
	}
}
