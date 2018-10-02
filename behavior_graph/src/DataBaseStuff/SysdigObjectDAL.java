package DataBaseStuff;

import java.lang.reflect.Field;
import java.sql.Statement;
import java.util.ArrayList;
import Classes.*;

public class SysdigObjectDAL {
	private Field[] ClassFields;
	private String InsertTemplate;
	
	private final String[] shortFieldsList = { "evt_datetime", 
            "evt_type", "thread_tid", "proc_name",
           "proc_args","proc_cwd","proc_cmdline",
           "proc_pname", "proc_pid", "proc_ppid", "fd_cip",
           "fd_cport", "fd_directory", "fd_filename", "fd_ip",
            "fd_name", "fd_num",
           "fd_sip", "fd_sockfamily", "fd_sport",
           "fd_type","fd_typechar", "user_name",
           "user_uid", "evt_num" ,"evt_args","user_shell"};
	
	private final String[] androidFieldsList = { "evt_time", "proc_name", "proc_pid", "thread_tid", "proc_ppid",
			"evt_dir", "evt_type", "fd_typechar", "evt_args" };

	public SysdigObjectDAL(boolean shortList, boolean android) throws Exception {
		// region Set Class fields
		Class<?> c = new SysdigRecordObject().getClass();
		if (!shortList && !android)
			ClassFields = c.getFields();
		else if (shortList && !android) {
			ArrayList<Field> temp = new ArrayList<Field>();
			for (String pick : shortFieldsList)
				temp.add(c.getField(pick));

			ClassFields = temp.toArray(new Field[temp.size()]);
		} else if (android) {
			ArrayList<Field> temp = new ArrayList<Field>();
			for (String pick : androidFieldsList)
				temp.add(c.getField(pick));

			ClassFields = temp.toArray(new Field[temp.size()]);
		}

		// endregion

		// region create insert template
		String Keys = " Insert into SysdigOutPut ( ";
		String Values = "";
		int FirstLen = Keys.length();
		for (Field pick : ClassFields) {
			if (Keys.length() != FirstLen)
				Keys += ",";

			Keys += pick.getName();
		}
		Keys += " ) values ( %1$s )";
		InsertTemplate = Keys;
		// endregion

	}

	/**
	 * Insets the record into the Database
	 * 
	 * @param inp
	 *            the object to be inseted
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
				// PickString +="\""+ temp.toString().replace("\"",
				// "\\\"")+"\"";
			}
			DataBaseLayer DL = new DataBaseLayer();
			Query = String.format(InsertTemplate, PickString);
			DL.runUpdateQuery(Query);
		} catch (Exception ex) {
			System.out.println(Query);
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
			throws NumberFormatException, IllegalArgumentException, IllegalAccessException {
		SysdigRecordObject ret = new SysdigRecordObject();

		String tokens[] = inp.split("=&amin&=");

		if (tokens.length != ClassFields.length) {
			throw new NumberFormatException("Error! number of fields do not match!" + tokens.length + " instead of "
					+ ClassFields.length + " : " + inp);
			// System.out.println("Error! number of fields do not match!" +
			// tokens.length + " instead of "+ ClassFields.length);
		}
		for (int index = 0; index < tokens.length; index++)
			ClassFields[index].set(ret, tokens[index].trim());

//		ret.fd_num = ret.fd_name + "|" + ret.fd_num;
//		ret.proc_pid = ret.proc_pid+"|" + ret.proc_name;
		return ret;
	}

	// public SysdigRecordObject GetObjectFromAndroidTextLine(String inp)
	// throws InvalidFormatException, IllegalArgumentException,
	// IllegalAccessException {
	// SysdigRecordObject ret = new SysdigRecordObject();
	//
	// String tokens[] = inp.split(",");
	//
	// if (tokens.length != ClassFields.length) {
	// throw new InvalidFormatException(
	// "Error! number of fields do not match!" + tokens.length + " instead of "
	// + ClassFields.length);
	// // System.out.println("Error! number of fields do not match!" +
	// // tokens.length + " instead of "+ ClassFields.length);
	// }
	// for (int index = 0; index < tokens.length; index++)
	// ClassFields[index].set(ret, tokens[index]);
	//
	// return ret;
	// }

}
