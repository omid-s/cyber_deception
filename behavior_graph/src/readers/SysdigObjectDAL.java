package readers;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.StringJoiner;
import classes.*;
import controlClasses.Configurations;
import dataBaseStuff.DataBaseLayer;
import exceptions.HighFieldNumberException;
import exceptions.LowFieldNumberException;

public class SysdigObjectDAL {
	protected Field[] ClassFields;
	protected String InsertTemplate;

	public SysdigObjectDAL(boolean shortList) throws NoSuchFieldException, SecurityException {
		// region Set Class fields
		Class<?> c = new SysdigRecordObject().getClass();
		if (!shortList)
			ClassFields = c.getFields();
		else {
			ArrayList<Field> temp = new ArrayList<Field>();
			for (String pick : Configurations.getShortFieldList())
				temp.add(c.getField(pick));

			ClassFields = temp.toArray(new Field[temp.size()]);
		}
		// endregion
	}

	public SysdigObjectDAL() {

	}

	protected void init() {

		// region create insert template
		String Keys = " Insert into SysdigOutPut ( ";
		String Values = "";
		int FirstLen = Keys.length();
		for (Field pick : ClassFields) {
			if (Keys.length() != FirstLen)
				Keys += ",";

			Keys += pick.getName();
		}
		Keys += " ) values ( %1$s ) ;\r\n ";
		InsertTemplate = Keys;
		// endregion

	}

	protected static String big_query = "";
	protected static int big_query_counter = 0;
	protected static StringJoiner items = new StringJoiner(" ");

	/**
	 * Insets the record into the Database
	 * 
	 * @param inp the object to be inseted
	 */
	public void Insert(SysdigRecordObject inp) {
		String Query = "";
		try {
			String PickString = "";
			for (Field pick : ClassFields) {
				Object temp = pick.get(inp);
				if (temp == null)
					temp = "";

				if (PickString.length() != 0)
					PickString += " , ";

				PickString += "'" + temp.toString().replace("'", "''") + "'";
			}
			DataBaseLayer DL = new DataBaseLayer();
			Query = String.format(InsertTemplate, PickString);

			items.add(Query);

			big_query_counter++;

			if (big_query_counter % 1000 == 0) {
				DL.runUpdateQuery(items.toString());
				big_query = "";
				items = new StringJoiner(" ");
			}
		} catch (Exception ex) {
			System.out.println(Query);
			System.out.println("Class Not found!");
		}
	}

	/**
	 * Loads the SysdigRecordObject from a sql resultset
	 * 
	 * @param input the resultset to load the object from
	 * @return created sysdoigobject based on the row
	 * @throws SQLException             if row is not well formed
	 * @throws IllegalArgumentException is value is not compatible with the record's
	 *                                  expectation
	 * @throws IllegalAccessException   should not be thrown!
	 */
	public SysdigRecordObject LoadFromResultSet(ResultSet input)
			throws SQLException, IllegalArgumentException, IllegalAccessException {
		SysdigRecordObject ret = new SysdigRecordObject();

		for (Field pick : ClassFields) {

			String value = input.getString(pick.getName());
			if (value != null && !value.isEmpty())
				pick.set(ret, value);
		}

		return ret;
	}

	/**
	 *  Flushes the rows into the storage 
	 *  This method will be called periodically to flush the rows 
	 */
	public void flushRows() {
		try {
			DataBaseLayer DL = new DataBaseLayer();
			DL.runUpdateQuery(items.toString());
		} catch (Exception ex) {
			System.out.println(big_query);
			System.out.println("Class Not found!");
		}
	}

	/**
	 * Reads the line of input and tryes to create a sysdig reciord based on the
	 * input text. number of fields in the row should match
	 * 
	 * @param inp
	 * @return
	 * @throws NumberFormatException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public SysdigRecordObject GetObjectFromTextLine(String inp)
			throws LowFieldNumberException, HighFieldNumberException, IllegalArgumentException, IllegalAccessException {
		SysdigRecordObject ret = new SysdigRecordObject();

		String tokens[] = inp.split(Configurations.getInstance().getSetting(Configurations.LINE_SEPERATOR));

		int Length = tokens.length;

		if (Configurations.getInstance().getSetting(Configurations.LEGACY_MODE).equals("true"))
			Length += 2;

		if (Length < ClassFields.length) {
			throw new LowFieldNumberException("Error! number of fields do not match!" + tokens.length + " instead of "
					+ ClassFields.length + " : " + inp);
		} else if (Length > ClassFields.length) {
			throw new HighFieldNumberException("Error! number of fields do not match!" + tokens.length + " instead of "
					+ ClassFields.length + " : " + inp);
		}
		for (int index = 0; index < tokens.length; index++)
			ClassFields[index].set(ret, tokens[index].trim());

		// ret.fd_num = ret.fd_name + "|" + ret.fd_num;
		// ret.proc_pid = ret.proc_pid+"|" + ret.proc_name;
		return ret;
	}

}
