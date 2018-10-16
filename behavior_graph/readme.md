# Behavior Graph 

This tool shows a graph of activities. 

## Install 
sysdig is required for running this tool to install sysdig : 
	
	curl -s https://s3.amazonaws.com/download.draios.com/stable/install-sysdig | sudo bash
	
## to run Sysdig for data collection  

Program runs on two modes of logs for now, 

* Long Format : this will assume all 84 fields of the sysdig logs to be present in the report 
* Short Format : this will assume a smaller set of fields, this is to save disk space when log is stored. 

### Long format

* to collect information, sysdig is uised, to invoke sysdig : (output can be piped/sent to a file for latter processing)

	sudo sysdig -p *"%fd.num=&amin&=%fd.type=&amin&=%fd.typechar=&amin&=%fd.name=&amin&=%fd.directory=&amin&=%fd.filename=&amin&=%fd.ip=&amin&=%fd.cip=&amin&=%fd.sip=&amin&=fd.port=&amin&=%fd.cport=&amin&=%fd.sport=&amin&=%fd.l4proto=&amin&=%fd.sockfamily=&amin&=%fd.is_server=&amin&=%proc.pid=&amin&=%proc.exe=&amin&=%proc.name=&amin&=%proc.args=&amin&=%proc.cmdline=&amin&=%proc.cwd=&amin&=%proc.nchilds=&amin&=%proc.ppid=&amin&=%proc.pname=&amin&=%proc.apid=&amin&=%proc.aname=&amin&=%proc.loginshellid=&amin&=%proc.duration=&amin&=%proc.fdopencount=&amin&=%proc.fdlimit=&amin&=%proc.fdusage=&amin&=%proc.vmsize=&amin&=%proc.vmrss=&amin&=%proc.vmswap=&amin&=%thread.pfmajor=&amin&=%thread.pfminor=&amin&=%thread.tid=&amin&=%thread.ismain=&amin&=%thread.exectime=&amin&=%thread.totexectime=&amin&=%evt.num=&amin&=%evt.time=&amin&=%evt.time.s=&amin&=%evt.datetime=&amin&=%evt.rawtime=&amin&=%evt.rawtime.s=&amin&=%evt.rawtime.ns=&amin&=%evt.reltime=&amin&=%evt.reltime.s=&amin&=%evt.reltime.ns=&amin&=%evt.latency=&amin&=%evt.latency.s=&amin&=%evt.latency.ns=&amin&=%evt.deltatime=&amin&=%evt.deltatime.s=&amin&=%evt.deltatime.ns=&amin&=%evt.dir=&amin&=%evt.type=&amin&=%evt.cpu=&amin&=%evt.args=&amin&=%evt.info=&amin&=%evt.buffer=&amin&=%evt.res=&amin&=%evt.rawres=&amin&=%evt.failed=&amin&=%evt.is_io=&amin&=%evt.is_io_read=&amin&=%evt.is_io_write=&amin&=%evt.io_dir=&amin&=%evt.is_wait=&amin&=evt.is_syslog=&amin&=evt.count=&amin&=%user.uid=&amin&=%user.name=&amin&=user.homedir=&amin&=user.shell=&amin&=group.gid=&amin&=group.name=&amin&=syslog.facility.str=&amin&=syslog.facility=&amin&=%syslog.severity.str=&amin&=syslog.severity=&amin&=syslog.message" "(evt.type=read or evt.type=write or evt.type=open or evt.type=close or evt.type=pwrite64 or evt.type=writev or evt.type=pwritev or evt.type=socket or evt.type=connect or evt.type=accept or  evt.type=sendto or evt.type=recvfrom or  evt.type=sendmsg or evt.type=recvmsg or evt.type=clone or evt.type=fork or evt.type=vfork or evt.type=execve or evt.type=pipe2 or evt.type=pipe or evt.type=accept4 or evt.type=pread64 or evt.type=readv or evt.type=preadv or  evt.type=rename or evt.type=renameat or evt.type=unlink or evt.type=link or evt.type=kill) and evt.failed!=true and evt.dir=<"
	
	
### Short format 

to collect logs to be used with the short format, use the following invocation for sysdig 

	 sudo sysdig -p *"%evt.datetime=&amin&=%evt.type=&amin&=%thread.tid=&amin&=%proc.name=&amin&=%proc.args=&amin&=%proc.cwd=&amin&=%proc.cmdline=&amin&=%proc.pname=&amin&=%proc.pid=&amin&=%proc.ppid=&amin&=%fd.cip=&amin&=%fd.cport=&amin&=%fd.directory=&amin&=%fd.filename=&amin&=%fd.ip=&amin&=%fd.name=&amin&=%fd.num=&amin&=%fd.sip=&amin&=%fd.sockfamily=&amin&=%fd.sport=&amin&=%fd.type=&amin&=%fd.typechar=&amin&=%user.name=&amin&=%user.uid=&amin&=%evt.num=&amin&=%evt.args=&amin&=%user.shell" "(evt.type=read or evt.type=write or evt.type=open or evt.type=close or evt.type=pwrite64 or evt.type=writev or evt.type=pwritev or evt.type=socket or evt.type=connect or evt.type=accept or  evt.type=sendto or evt.type=recvfrom or  evt.type=sendmsg or evt.type=recvmsg or evt.type=clone or evt.type=fork or evt.type=vfork or evt.type=execve or evt.type=pipe2 or evt.type=pipe or evt.type=accept4 or evt.type=pread64 or evt.type=readv or evt.type=preadv or  evt.type=rename or evt.type=renameat or evt.type=unlink or evt.type=link or evt.type=kill) and evt.failed!=true and not proc.name contains gnome and evt.dir=<"
	 
	 
	
## Running Jar File and keywords

there is a build jar file in tool_bin dir. to run it :

	java -jar dcf.jar [arguments]
	
arguments : 

	* ssql :  save a copy of all the recods read to a postgress database
	* gv : create the graph in verbose mode
	* g : create the graph in non-verbose mode (do not use with new graph tool)
	* file : sets the source of logs to be file, if this key is used path= has to be provided ; if this key is not provided , stdin will be assumed the source of logs 
	*  sf : saves the formated out put to be used in other tools (should be used with outpath= to choose where to save)
	* path=[path to input file] : path to input file
	* outpath=[path to file] : path to which the formated output is supposed to be stored. 
	
	
## Query model 


OQL is capable supports projection and filter and it’s based on the sub graphs initiated by the nodes identified through the criterias. 
In our data model we divide the data in two parts : 
	Resources : are represented as vertices in our causal graph representation, and include processes, threads, files, pipes, networks, activities, etc. we identify them by color coding based on type in the graph. 
	Access Calls : are represented as edges in our  causal graph and represent access to resources, systems calls, initiations, API calls, Binder Calls, etc. we identify them by color coding based on the type in the graph.

The general structure of our query language is as follows : 
	
	1- [set/get] [variable_name] [value]
	
	2- [verbose] [back] [forward] select {* ,[ projection of Access Types ]} from {*,[ projection of Resource Types ]} [where [[field] [operator] [value]]^ ] [;]

### Variable setting/ Getting query

THis model is used to set or get environment variables, variables are as follows (case insensitive) : 
 
 * FORWARD_DEPTH : this sets the depth to which each forward tree is traversed 
 * BACK_DEPTH : this sets the depth to which each backward tracking is done 
 
 
### Searching query
The parts in the query account for the followings :
[verbose] : we have two options to show only existence of the relation between resources or seeing all the relations on the graph. 
[back] : back tracks from selected nodes to first node with input degree = 0 in the graph. 
[forward] : forward tracks from selected nodes to all resources it has touched and this will recursively continue. 

[projection of access types]  : this option is either * for all, or is a selection of system calls including read, write, open, exec, etc. NOTE : because we do not capture starting of all processes, `exec` denotes an execution of a process by it's parent but time stamp will be the first time it has been executed which might or might not be when it was started.
[projection of Resource types] : this option is either * for all, or is a selection of “file”, "process", "soc" for all types of socket calls, "pipe" or "unix"
, ..
Criteria : criterias are formated as `[field] [operator] [value]` . The “field” is one of the options : “pid” for process id, “tid” for thread Id and “activity.name” is the name of activity. The “operator” is either “is” or “has” which account for exact match and the contains operator. Different criterions can be added in the query using the separator “,also,”; “or” logical operator would be applied to these criterias. we have used this format to minimize the parsing efforts.  

The “;” in the end indicates whether to add the results of the current query to the graph which is already present in the window; so we can have results of multiple queries create the whole picture piece by piece. 

