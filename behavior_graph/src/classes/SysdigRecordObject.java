package classes;

import java.lang.reflect.Field;
import java.util.StringJoiner;

import controlClasses.RuntimeVariables;

public class SysdigRecordObject {
	public String fd_num;
	public String fd_type;
	public String fd_typechar;
	public String fd_name;
	public String fd_directory;
	public String fd_filename;
	public String fd_ip;
	public String fd_cip;
	public String fd_sip;
	public String fd_port;
	public String fd_cport;
	public String fd_sport;
	public String fd_l4proto;
	public String fd_sockfamily;
	public String fd_is_server;
	public String proc_pid;
	public String proc_exe;
	public String proc_name;
	public String proc_args;
	public String proc_cmdline;
	public String proc_cwd;
	public String proc_nchilds;
	public String proc_ppid;
	public String proc_pname;
	public String proc_apid;
	public String proc_aname;
	public String proc_loginshellid;
	public String proc_duration;
	public String proc_fdopencount;
	public String proc_fdlimit;
	public String proc_fdusage;
	public String proc_vmsize;
	public String proc_vmrss;
	public String proc_vmswap;
	public String thread_pfmajor;
	public String thread_pfminor;
	public String thread_tid;
	public String thread_ismain;
	public String thread_exectime;
	public String thread_totexectime;
	public String evt_num;
	public String evt_time;
	public String evt_time_s;
	public String evt_datetime;
	public String evt_rawtime;
	public String evt_rawtime_s;
	public String evt_rawtime_ns;
	public String evt_reltime;
	public String evt_reltime_s;
	public String evt_reltime_ns;
	public String evt_latency;
	public String evt_latency_s;
	public String evt_latency_ns;
	public String evt_deltatime;
	public String evt_deltatime_s;
	public String evt_deltatime_ns;
	public String evt_dir;
	public String evt_type;
	public String evt_cpu;
	public String evt_args;
	public String evt_info;
	public String evt_buffer;
	public String evt_res;
	public String evt_rawres;
	public String evt_failed;
	public String evt_is_io;
	public String evt_is_io_read;
	public String evt_is_io_write;
	public String evt_io_dir;
	public String evt_is_wait;
	public String evt_is_syslog;
	public String evt_count;
	public String user_uid;
	public String user_name;
	public String user_homedir;
	public String user_shell;
	public String group_gid;
	public String group_name;
	public String syslog_facility_str;
	public String syslog_facility;
	public String syslog_severity_str;
	public String syslog_severity;
	public String syslog_message;
	public String ubsi_unit_id;
	public String ubsi_thread_id;
	public String Computer_id;
	// public String arg1;
	// public String arg2;
	// public String returnValue;

	public String getProcPID() {
		return Computer_id + "|" + proc_pid + "|" + proc_name;
	}

	public String getParentProcID() {
		return Computer_id + "|" + proc_ppid + "|" + proc_pname;
	}

	public String getTID() {
		return getProcPID() + "|" + thread_tid;
	}

	public String getUBSIID() {
		if (ubsi_unit_id != null)
			return getTID() + "|" + ubsi_unit_id;
		else
			return getTID() + "|0";
	}

	public String getFD_ID() {
		if (RuntimeVariables.getInstance().getIgnoreFDNumber())
			return Computer_id + "|" + String.valueOf(this.fd_name);
		else
			return Computer_id + "|" + fd_num + "|" + fd_name;
	}

	/**
	 * returns the json representation of the object
	 * 
	 * @return the json string of the object
	 * @throws IllegalArgumentException if internal issue has been seen
	 * @throws IllegalAccessException   if an internal issue has happened
	 */
	public String toJSONString() throws IllegalArgumentException, IllegalAccessException {

		Class c = this.getClass();

		StringJoiner jsonObject = new StringJoiner(",");
		for (Field field : c.getDeclaredFields()) {
			field.setAccessible(true);
			String name = field.getName();
			String value = String.valueOf(field.get(this));
			jsonObject.add(String.format(" \"%s\": \"%s\"", name, value.replace("\"", "\'")));
		}

		return "{" + jsonObject.toString() + "}";

	}

	@Override
	public String toString() {
		return "fd_num=\"" + fd_num.trim() + "\", fd_type=\"" + fd_type.trim() + "\", fd_typechar=\"" + fd_typechar
				+ "\", fd_name=\"" + fd_name.trim() + "\", fd_directory=\"" + fd_directory.trim() + "\", fd_filename=\""
				+ fd_filename + "\", fd_ip=\"" + fd_ip.trim() + "\", fd_cip=\"" + fd_cip.trim() + "\", fd_sip=\""
				+ fd_sip.trim() + "\", fd_port=\"" + fd_port + "\", fd_cport=\"" + fd_cport.trim() + "\", fd_sport=\""
				+ fd_sport.trim() + "\", fd_l4proto=\"" + fd_l4proto + "\", fd_sockfamily=\"" + fd_sockfamily.trim()
				+ "\", fd_is_server=\"" + fd_is_server.trim() + "\", proc_pid=\"" + proc_pid + "\", proc_exe=\""
				+ proc_exe.trim() + "\", proc_name=\"" + proc_name.trim() + "\", proc_args=\"" + proc_args.trim()
				+ "\", proc_cmdline=\"" + proc_cmdline.trim() + "\", proc_cwd=\"" + proc_cwd.trim()
				+ "\", proc_nchilds=\"" + proc_nchilds.trim() + "\", proc_ppid=\"" + proc_ppid.trim()
				+ "\", proc_pname=\"" + proc_pname.trim() + "\", proc_apid=\"" + proc_apid.trim() + "\", proc_aname=\""
				+ proc_aname + "\", proc_loginshellid=\"" + proc_loginshellid.trim() + "\", proc_duration=\""
				+ proc_duration + "\", proc_fdopencount=\"" + proc_fdopencount.trim() + "\", proc_fdlimit=\""
				+ proc_fdlimit.trim() + "\", proc_fdusage=\"" + proc_fdusage.trim() + "\", proc_vmsize=\""
				+ proc_vmsize.trim() + "\", proc_vmrss=\"" + proc_vmrss.trim() + "\", proc_vmswap=\""
				+ proc_vmswap.trim() + "\", thread_pfmajor=\"" + thread_pfmajor.trim() + "\", thread_pfminor=\""
				+ thread_pfminor + "\", thread_tid=\"" + thread_tid.trim() + "\", thread_ismain=\""
				+ thread_ismain.trim() + "\", thread_exectime=\"" + thread_exectime.trim() + "\", thread_totexectime=\""
				+ thread_totexectime.trim() + "\", evt_num=\"" + evt_num + "\", evt_time=\"" + evt_time.trim()
				+ "\", evt_time_s=\"" + evt_time_s.trim() + "\", evt_datetime=\"" + evt_datetime + "\", evt_rawtime=\""
				+ evt_rawtime.trim() + "\", evt_rawtime_s=\"" + evt_rawtime_s.trim() + "\", evt_rawtime_ns=\""
				+ evt_rawtime_ns.trim() + "\", evt_reltime=\"" + evt_reltime.trim() + "\", evt_reltime_s=\""
				+ evt_reltime_s + "\", evt_reltime_ns=\"" + evt_reltime_ns.trim() + "\", evt_latency=\""
				+ evt_latency.trim() + "\", evt_latency_s=\"" + evt_latency_s.trim() + "\", evt_latency_ns=\""
				+ evt_latency_ns.trim() + "\", evt_deltatime=\"" + evt_deltatime + "\", evt_deltatime_s=\""
				+ evt_deltatime_s.trim() + "\", evt_deltatime_ns=\"" + evt_deltatime_ns.trim() + "\", evt_dir=\""
				+ evt_dir.trim() + "\", evt_type=\"" + evt_type.trim() + "\", evt_cpu=\"" + evt_cpu.trim()
				+ "\", evt_args=\"" + evt_args.trim() + "\", evt_info=\"" + evt_info.trim() + "\", evt_buffer=\""
				+ evt_buffer.trim() + "\", evt_res=\"" + evt_res.trim() + "\", evt_rawres=\"" + evt_rawres
				+ "\", evt_failed=\"" + evt_failed.trim() + "\", evt_is_io=\"" + evt_is_io.trim()
				+ "\", evt_is_io_read=\"" + evt_is_io_read + "\", evt_is_io_write=\"" + evt_is_io_write.trim()
				+ "\", evt_io_dir=\"" + evt_io_dir.trim() + "\", evt_is_wait=\"" + evt_is_wait + "\", evt_is_syslog=\""
				+ evt_is_syslog.trim() + "\", evt_count=\"" + evt_count.trim() + "\", user_uid=\"" + user_uid
				+ "\", user_name=\"" + user_name.trim() + "\", user_homedir=\"" + user_homedir.trim()
				+ "\", user_shell=\"" + user_shell + "\", group_gid=\"" + group_gid.trim() + "\", group_name=\""
				+ group_name.trim() + "\", syslog_facility_str=\"" + syslog_facility_str.trim()
				+ "\", syslog_facility=\"" + syslog_facility.trim() + "\", syslog_severity_str=\""
				+ syslog_severity_str.trim() + "\", syslog_severity=\"" + syslog_severity.trim() + "\", ubsi_unit_id="
				+ ubsi_unit_id + " , ubsi_thread_id=" + ubsi_thread_id + "\", syslog_message=\""
				+ syslog_message.trim().replace("\n", " ");
	}

}
