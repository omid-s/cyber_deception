package Classes;

import com.mysql.fabric.xmlrpc.base.Array;

import java.io.*;
import java.util.*;
import java.util.stream.Stream;


/**
 * A class dedicated to parsing audit log files and mapping them to the appropriate data structure
 *
 * @author Matthew Jones
 * @email mfj38472@uga.edu
 *
 *
 */
public class AuditParse {
    public BufferedReader buff;
    String line;
    HashMap<String, Record> recordHashMap = new HashMap<>();
    HashMap<String, String> processHashMap = new HashMap<>();
    HashMap<String, String> sysCallTable = new HashMap<>();
    List<SysdigRecordObject> sysDigList  = new ArrayList<>();

    /**
     * Parses the audit log file given to it into a list of records. Each record is identified by it's ID and type, and within each record is a list of tokens containing key=value pairs
     * that pertain to the properties of the system call. Please note this method assumes that the file object given to it is an audit log, any other filed given to this method will result
     * in unexpected behavior.
     *
     * @param auditlog audit log file to analyze
     * @throws IOException
     */
    public void parseAuditLog(File auditlog) throws IOException {
        long start = System.nanoTime();
        try {
            buff = new BufferedReader(new FileReader(auditlog)); //Reads the audit log file into a buffer
            setUpSysCallHash(sysCallTable);
        } catch (FileNotFoundException filex) {
            System.out.println("Error file not found!");
            System.out.println(new File(".").getAbsolutePath());
            System.exit(0);
        }

        while ((line = buff.readLine()) != null) { //Parses each line of the audit log file
            List<String> LineSplit = splitString(line); //split string by space
            if (LineSplit.get(0).equals("type=SYSCALL") || LineSplit.get(0).equals("type=EXECVE")) {
                Record newRec = new Record(LineSplit); //create new record
                if(recordHashMap.containsKey(newRec.getId()) && newRec.getType().equals("SYSCALL")){

                    recordHashMap.get(newRec.getId()).tokenList.addAll(newRec.tokenList);

                }else if(newRec.getType().equals("SYSCALL")){
                    newRec.appendToken("syscall_name=" + sysCallTable.get(newRec.getToken("syscall").getValue()));
                    recordHashMap.put(newRec.getId(), newRec);
                }else if(newRec.getType().equals("EXECVE")){
                    //Obtain the syscall record from the hashmap
                    Record sysCallRec = recordHashMap.get(newRec.getId());

                    //Obtain the pid and ppid values from the syscall record
                    String pid = sysCallRec.getToken("pid").getValue();
                    String ppid = sysCallRec.getToken("ppid").getValue();

                    //check to see if the pid is already mapped to a name or not.
                    if(!processHashMap.containsKey(ppid)){
                        String ppid_name = sysCallRec.getToken("comm").getValue().replaceAll("^\"|\"$", "");
                        processHashMap.put(ppid, ppid_name);
                        sysCallRec.appendToken("ppid_name"+"="+ppid_name);

                    }
                    if(!processHashMap.containsKey(pid)){
                        String pid_name = newRec.tokenList.get(1).getValue();
                        processHashMap.put(pid, pid_name);
                        sysCallRec.appendToken("pid_name=" + pid_name);
                    }

                }else if(newRec.getType().equals("CWD")){
                    recordHashMap.get(newRec.getId()).appendToken("cwd="+newRec.getToken("cwd").getValue());
                }

            }


        }
        sysDigList = maptoSysDig(recordHashMap.values());

        System.out.println(System.nanoTime()-start);
    }


    List<SysdigRecordObject> maptoSysDig(Collection<Record> collection){
        ArrayList<SysdigRecordObject> sysdigList = new ArrayList<>();
        ArrayList<Record> arrRec = new ArrayList<>(collection);
        for(int i = 0; i<arrRec.size(); i++){
            for(int j = 0; j<arrRec.get(i).tokenList.size(); j++){
                SysdigRecordObject sysdig = new SysdigRecordObject();
                switch (arrRec.get(i).tokenList.get(j).getKey()){
                    case "pid":
                        sysdig.proc_pid = arrRec.get(i).tokenList.get(j).getValue();
                        break;

                    case "ppid":
                        sysdig.proc_ppid = arrRec.get(i).tokenList.get(j).getValue();
                        break;

                    case "exe":
                        sysdig.proc_exe = arrRec.get(i).tokenList.get(j).getValue();
                        break;

                    case "comm":
                        sysdig.proc_name = arrRec.get(i).tokenList.get(j).getValue();
                        break;

                    case "uid":
                        sysdig.user_uid = arrRec.get(i).tokenList.get(j).getValue();
                        break;
                    case"gid":
                        sysdig.group_gid = arrRec.get(i).tokenList.get(j).getValue();
                        break;

                    case "cwd":
                        sysdig.proc_cwd = arrRec.get(i).tokenList.get(j).getValue();
                        break;

                }
                sysdigList.add(sysdig);
            }
        }

        return sysdigList;
    }


    /**
     * Splits the string line up into a list that will then be parsed by the record object
     *
     * @param line the line to be fed and split up
     * @return the list containing all the tokens
     */
    private List<String> splitString(String line){

        boolean quoteMode = false; //Boolean variable for determining if currently processed text is between double quotes
        String tempString = ""; //temp string used to stack on variables that will then be added to the string list.
        ArrayList<String> stringList = new ArrayList<>();
        for(int i = 0; i<line.length(); i++){


            if(line.substring(i, i+1).equals("\"") && quoteMode == false){
            quoteMode = true;
            }else if(line.substring(i, i+1).equals("\"") && quoteMode == true){
            quoteMode = false;
            tempString+="\"";
            stringList.add(tempString);
            tempString = "";
            continue;
            }

            if(quoteMode == true){
            tempString+=line.substring(i,i+1);
            }else if(quoteMode==false){
                if(!line.substring(i,i+1).equals(" ")){
                    tempString+=line.substring(i,i+1);
                }else if(line.substring(i,i+1).equals(" ") && tempString.isEmpty() == false){
                    stringList.add(tempString);
                    tempString = "";
                }
            }


        }

    return stringList;
    }

    /**
     * Initializes the system call table hashmap, which contains a mapping of each system call to its associated ID
     * @param hash the system call hashmap to initialize
     * @throws IOException
     */
    public static void setUpSysCallHash(HashMap<String, String> hash) throws IOException{

        hash.put("0", "read");
        hash.put("1", "write");
        hash.put("2", "open");
        hash.put("3", "close");
        hash.put("4", "stat");
        hash.put("5", "fstat");
        hash.put("6", "lstat");
        hash.put("7", "poll");
        hash.put("8", "lseek");
        hash.put("9", "mmap");
        hash.put("10", "mprotect");
        hash.put("11", "munmap");
        hash.put("12", "brk");
        hash.put("13", "sigaction");
        hash.put("14", "sigprocmask");
        hash.put("15", "sigreturn");
        hash.put("16", "ioctl");
        hash.put("17", "pread64");
        hash.put("18", "pwrite64");
        hash.put("19", "readv");
        hash.put("20", "writev");
        hash.put("21", "access");
        hash.put("22", "pipe");
        hash.put("23", "select");
        hash.put("24", "sched_yield");
        hash.put("25", "mremap");
        hash.put("26", "msync");
        hash.put("27", "minicore");
        hash.put("28", "madvise");
        hash.put("29", "shmget");
        hash.put("30", "shmat");
        hash.put("31", "shmctl");
        hash.put("32", "dup");
        hash.put("33", "dup2");
        hash.put("34", "pause");
        hash.put("35", "nanosleep");
        hash.put("36", "getitimer");
        hash.put("37", "alarm");
        hash.put("38", "setitimer");
        hash.put("39", "getpid");
        hash.put("40", "sendfile64");
        hash.put("41", "socket");
        hash.put("42", "connect");
        hash.put("43", "accept");
        hash.put("44", "sendto");
        hash.put("45", "recvfrom");
        hash.put("46", "sendmsg");
        hash.put("47", "recvmsg");
        hash.put("48", "shutdown");
        hash.put("49", "bind");
        hash.put("50", "listen");
        hash.put("51", "getsockname");
        hash.put("52", "getpeername");
        hash.put("53", "socketpair");
        hash.put("54", "setsockopt");
        hash.put("55", "getsockopt");
        hash.put("56", "stub_clone");
        hash.put("57", "stub_fork");
        hash.put("58", "stub_vfork");
        hash.put("59", "stub_execve");
        hash.put("60", "exit");
        hash.put("61", "wait4");
        hash.put("62", "kill");
        hash.put("63", "newuname");
        hash.put("64", "semget");
        hash.put("65", "semop");
        hash.put("66", "semctl");
        hash.put("67", "schmdt");
        hash.put("68", "msgget");
        hash.put("69", "msgsnd");
        hash.put("70", "msgrcv");
        hash.put("71", "msgctl");
        hash.put("72", "fcntl");
        hash.put("73", "flock");
        hash.put("74", "fsync");
        hash.put("75", "fdatasync");
        hash.put("76", "truncate");
        hash.put("77", "frtuncate");
        hash.put("78", "getdents");
        hash.put("79", "getcwd");
        hash.put("80", "chdir");
        hash.put("81", "fchdir");
        hash.put("82", "rename");
        hash.put("83", "mkdir");
        hash.put("84", "rmdir");
        hash.put("85", "creat");
        hash.put("86", "link");
        hash.put("87", "unlink");
        hash.put("88", "symlink");
        hash.put("89", "readlink");
        hash.put("90", "chmod");
        hash.put("91", "fchmod");
        hash.put("92", "chown");
        hash.put("93", "fchown");
        hash.put("94", "lchown");
        hash.put("95", "umask");
        hash.put("96", "gettimeofday");
        hash.put("97", "getrlimit");
        hash.put("98", "getrusage");
        hash.put("99", "sysinfo");
        hash.put("100", "times");
        hash.put("101", "ptrace");
        hash.put("102", "getuid");
        hash.put("103", "syslog");
        hash.put("104", "getgid");
        hash.put("105", "setuid");
        hash.put("106", "setgid");
        hash.put("107", "geteuid");
        hash.put("108", "getegid");
        hash.put("109", "setpgid");
        hash.put("110", "getppid");
        hash.put("111", "getpgrp");
        hash.put("112", "setsid");
        hash.put("113", "setreuid");
        hash.put("114", "setregid");
        hash.put("115", "getgroups");
        hash.put("116", "setgroups");
        hash.put("117", "setresuid");
        hash.put("118", "getresuid");
        hash.put("119", "setresgid");
        hash.put("120", "getresgid");
        hash.put("121", "getpgid");
        hash.put("122", "setfsuid");
        hash.put("123", "setfsgid");
        hash.put("124", "getsid");
        hash.put("125", "capget");
        hash.put("126", "capset");
        hash.put("127", "rt_sigpending");
        hash.put("128", "sigtimedwait");
        hash.put("129", "sigqueueinfo");
        hash.put("130", "sigsuspend");
        hash.put("131", "sigaltstack");
        hash.put("132", "utime");
        hash.put("133", "mknod");
        hash.put("134", "uselib");
        hash.put("135", "personality");
        hash.put("136", "ustat");
        hash.put("137", "statfs");
        hash.put("138", "fstatfs");
        hash.put("139", "sysfs");
        hash.put("140", "getpriority");
        hash.put("141", "setpriority");
        hash.put("142", "sched_setparam");
        hash.put("143", "sched_getparam");
        hash.put("144", "sched_setscheduler");
        hash.put("145", "sched_getscheduler");
        hash.put("146", "sched_get_priority_max");
        hash.put("147", "sched_get_priority_min");
        hash.put("148", "sched_rr_get_interval");
        hash.put("149", "mlock");
        hash.put("150", "munlock");
        hash.put("151", "mlockall");
        hash.put("152", "munlockall");
        hash.put("153", "vhangup");
        hash.put("154", "modify_ldt");
        hash.put("155", "pivot_root");
        hash.put("156", "sysctl");
        hash.put("157", "prctl");
        hash.put("158", "arch_prctl");
        hash.put("159", "adjtimex");
        hash.put("160", "setrlimit");
        hash.put("161", "chroot");
        hash.put("162", "sync");
        hash.put("163", "acct");
        hash.put("164", "settimeofday");
        hash.put("165", "mount");
        hash.put("166", "umount");
        hash.put("167", "swapon");
        hash.put("168", "swapoff");
        hash.put("169", "reboot");
        hash.put("170", "sethostname");
        hash.put("171", "setdomainname");
        hash.put("172", "stub_iopl");
        hash.put("173", "ioperm");
        hash.put("174", "create_module");
        hash.put("175", "init_module");
        hash.put("176", "delete_module");
        hash.put("177", "get_kernel_syms");
        hash.put("178", "query_module");
        hash.put("179", "sys)quotactl");
        hash.put("180", "nfsservctl");
        hash.put("181", "getpmsg");
        hash.put("182", "putpmsg");
        hash.put("183", "afs_syscall");
        hash.put("184", "tuxcall");
        hash.put("185", "security");
        hash.put("186", "gettid");
        hash.put("187", "readahead");
        hash.put("188", "setxattr");
        hash.put("189", "lsetxattr");
        hash.put("190", "fsetxattr");
        hash.put("191", "getxattr");
        hash.put("192", "lgetxattr");
        hash.put("193", "fgetxattr");
        hash.put("194", "listxattr");
        hash.put("195", "llistxattr");
        hash.put("196", "flistxattr");
        hash.put("197", "removexattr");
        hash.put("198", "lremovexattr");
        hash.put("199", "fremovexattr");
        hash.put("200", "tkill");
        hash.put("201", "time");
        hash.put("202", "futex");
        hash.put("203", "sched_setaffinity");
        hash.put("204", "sched_getaffinity");
        hash.put("205", "set_thread_area");
        hash.put("206", "io_setup");
        hash.put("207", "io_destroy");
        hash.put("208", "io_getevents");
        hash.put("209", "io_submit");
        hash.put("210", "io_cancel");
        hash.put("211", "get_thread_area");
        hash.put("212", "lookup_dcookie");
        hash.put("213", "epoll_create");
        hash.put("214", "epoll_ctl_old");
        hash.put("215", "epoll_wait_old");
        hash.put("216", "remap_file_pages");
        hash.put("217", "getdents64");
        hash.put("218", "set_tid_address");
        hash.put("219", "restart_syscall");
        hash.put("220", "semtimedop");
        hash.put("221", "fadvise64");
        hash.put("222", "timer_create");
        hash.put("223", "timer_settime");
        hash.put("224", "timer_gettime");
        hash.put("225", "timer_getoverrun");
        hash.put("226", "timer_delete");
        hash.put("227", "clock_settime");
        hash.put("228", "clock_gettime");
        hash.put("229", "clock_getres");
        hash.put("230", "clock_nanosleep");
        hash.put("231", "exit_group");
        hash.put("232", "epoll_wait");
        hash.put("233", "epoll_ctl");
        hash.put("234", "tgkill");
        hash.put("235", "utimes");
        hash.put("236", "vserver");
        hash.put("237", "mbind");
        hash.put("238", "set_mempolicy");
        hash.put("239", "get_mempolicy");
        hash.put("240", "mq_open");
        hash.put("241", "mq_unlink");
        hash.put("242", "mq_timedsend");
        hash.put("243", "mq_timedreceive");
        hash.put("244", "mq_notify");
        hash.put("245", "mq_getsetattr");
        hash.put("246", "kexec_load");
        hash.put("247", "waitid");
        hash.put("248", "add_key");
        hash.put("249", "request_key");
        hash.put("250", "keyctl");
        hash.put("251", "ioprio_set");
        hash.put("252", "ioprio_get");
        hash.put("253", "inotify_init");
        hash.put("254", "inotify_add_watch");
        hash.put("255", "inotify_rm_watch");
        hash.put("256", "migrate_pages");
        hash.put("257", "openat");
        hash.put("258", "mkdirat");
        hash.put("259", "mknodat");
        hash.put("260", "fchownat");
        hash.put("261", "futimesat");
        hash.put("262", "newfsatat");
        hash.put("263", "unlinkat");
        hash.put("264", "renameat");
        hash.put("265", "linkat");
        hash.put("266", "symlinkat");
        hash.put("267", "readlinkat");
        hash.put("268", "fchmodat");
        hash.put("269", "faccessat");
        hash.put("270", "pselect6");
        hash.put("271", "ppoll");
        hash.put("272", "unshare");
        hash.put("273", "set_robust_list");
        hash.put("274", "get_robust_list");
        hash.put("275", "splice");
        hash.put("276", "tee");
        hash.put("277", "sync_file_range");
        hash.put("278", "vmsplice");
        hash.put("279", "move_pages");
        hash.put("280", "utimensat");
        hash.put("281", "epoll_pwait");
        hash.put("282", "signalfd");
        hash.put("283", "timerfd_create");
        hash.put("284", "eventfd");
        hash.put("285", "fallocate");
        hash.put("286", "timerfd_settime");
        hash.put("287", "timerfd_gettime");
        hash.put("288", "accept4");
        hash.put("289", "signalfd4");
        hash.put("290", "eventfd2");
        hash.put("291", "epoll_create1");
        hash.put("292", "dup3");
        hash.put("293", "pipe2");
        hash.put("294", "inotify_init1");
        hash.put("295", "preadv");
        hash.put("296", "pwritev");
        hash.put("297", "rt_tgsigqueueinfo");
        hash.put("298", "perf_event_open");
        hash.put("299", "recvmmsg");
        hash.put("300", "fanotify_init");
        hash.put("301", "fanotify_mark");
        hash.put("302", "prlimit64");
        hash.put("303", "name_to_handle_at");
        hash.put("304", "open_by_handle_at");
        hash.put("305", "clock_adjtime");
        hash.put("306", "syncfs");
        hash.put("307", "sendmmsg");
        hash.put("308", "setns");
        hash.put("309", "getcpu");
        hash.put("310", "process_vm_readv");
        hash.put("311", "process_vm_writev");
        hash.put("312", "kcmp");
        hash.put("313", "finit_module");


    }


    public static void main(String[] args){
        AuditParse audit = new AuditParse();
        try {
            audit.parseAuditLog(new File("audit.log"));
        } catch(IOException iox){
            System.out.println("IO Exception Error!");
            System.exit(0);
        }
    }
}
