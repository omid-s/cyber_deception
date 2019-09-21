package readers;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections15.map.HashedMap;

import classes.SysdigRecordObject;
import controlClasses.Configurations;
import exceptions.HighFieldNumberException;
import exceptions.LowFieldNumberException;

public class CSVReader extends SysdigObjectDAL {

	String fields_list[] = { "evt_datetime", "evt_type", "thread_tid", "proc_name", "proc_args", "proc_cwd",
			"proc_cmdline", "proc_pname", "proc_pid", "proc_ppid", "fd_cip", "fd_cport", "fd_directory", "fd_filename",
			"fd_ip","fd_port", "fd_name", "fd_num", "fd_type", "fd_typechar", "user_name", "user_uid", "evt_num", "evt_args",
			"user_shell", "ubsi_unit_id" };

	public CSVReader() throws NoSuchFieldException, SecurityException {

		Class<?> c = new SysdigRecordObject().getClass();
		ArrayList<Field> temp = new ArrayList<Field>();

		for (String pick : fields_list)
			temp.add(c.getField(pick));

		ClassFields = temp.toArray(new Field[temp.size()]);

		// finilize class creation
		init();
	}

	Map<String, String> pid_to_pname = new HashedMap<String, String>();

	@Override
	public SysdigRecordObject GetObjectFromTextLine(String inp)
			throws LowFieldNumberException, HighFieldNumberException, IllegalArgumentException, IllegalAccessException {

		SysdigRecordObject ret = new SysdigRecordObject();

		String fields[] = { "evt_num", "evt_datetime", "evt_type", "evt_res", "evt_args", "thread_tid", "thread_unitid",
				"proc_pid", "proc_ppid", "proc_name", "proc_exepath", "user_uid", "user_euid", "user_gid", "fd_num",
				"fd_type", "fd_filename", "fd_name", "fd_inode", "fd_ip", "fd_port", "fd_1_num", "fd_1_type",
				"fd_1_filename", "fd_1_name", "fd_1_inode", "fd_1_ip", "fd_1_port", "exec_proc_cwd", "exec_proc_args",
				"exe_proc_name", "exe_proc_inode", "dep_tid", "ubsi_unit_id", "" };

		List<String> indexes_list = Arrays.asList(fields_list);

		String tokens[] = inp.split(Configurations.getInstance().getSetting(Configurations.LINE_SEPERATOR));

		if (tokens.length < fields.length) {
			throw new LowFieldNumberException("Error! number of fields do not match!" + tokens.length + " instead of "
					+ ClassFields.length + " : " + inp);

		} else if (tokens.length > fields.length) {
			throw new HighFieldNumberException("Error! number of fields do not match!" + tokens.length + " instead of "
					+ ClassFields.length + " : " + inp);
		}
		for (int index = 0; index < tokens.length - 1; index++) {
			int i = indexes_list.indexOf(fields[index]);
//			if (i < 0)
//				System.out.println(fields[index]);
			if (i >= 0)
				ClassFields[i].set(ret, tokens[index].trim());
		}

		if (ret.proc_name != null && !ret.proc_name.trim().isEmpty()) {
			pid_to_pname.put(ret.proc_pid, ret.proc_name);
		}

		if (ret.evt_type.indexOf('(') >= 0)
			ret.evt_type = ret.evt_type.substring(0, ret.evt_type.indexOf('('));

		ret.fd_typechar = getFDTypeChar(ret.fd_type);
		
		if( ret.fd_type.toLowerCase().trim().equals("ipv4") ) {
			ret.fd_name= ret.fd_ip + ":" + ret.fd_port;
		}
		
		if (ret.fd_name == null || ret.fd_name.isEmpty())
			ret.fd_name = "<NA>";
		if (ret.fd_num == null || ret.fd_num.isEmpty())
			ret.fd_num = "<NA>";
		if (ret.proc_name == null || ret.proc_name.isEmpty())
			ret.proc_name = "<NA>";
		if (ret.proc_pname == null || ret.proc_pname.isEmpty())
			if (pid_to_pname.containsKey(ret.proc_ppid)) {
				ret.proc_pname = pid_to_pname.get(ret.proc_ppid);
				System.out.println("*");
			} else
				ret.proc_pname = "<NA>";

		
		
		return ret;
	}

	private String getFDTypeChar(String str) {
		switch (str) {
		case "ipv4":
			return "4";
		case "ipv6":
			return "6";
		case "file":
			return "f";
		case "signal":
			return "s";
		case "unix":
			return "u";
		case "event":
			return "e";
		case "inotify":
			return "i";
		case "timer":
			return "t";

		}
		return "<NA>";
	}
}
