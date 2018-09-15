import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.jsoup.parser.Parser;
public class Record {
    String id;
    String type;
    List<Token> tokenList = new ArrayList<Token>();
    HashMap<String, String> sysCallTable = new HashMap<>();

    Token syscallToken;
    Record(List<String> list) throws IOException{
            setUpSysCallHash(sysCallTable);
            //Extract the type from the string type=value
            type = list.get(0).substring(list.get(0).indexOf("=")+1);

            //Extracts the substring from the : up until the ) in audit(TIMESTAMP:ID)
            id = list.get(1).substring(list.get(1).indexOf(":")+1, list.get(1).indexOf(")"));
            list.remove(0);
            list.remove(0);
            for(int i = 0; i<list.size(); i++){
                Token token = new Token(list.get(i));
                tokenList.add(token);
            }



    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
    class Token{
        private String key;
        private String value;

        Token(String keyValuePair){
            key = keyValuePair.substring(0, keyValuePair.indexOf("="));
            value = keyValuePair.substring(keyValuePair.indexOf("=")+1);
            if(key.equals("syscall")){

            }
        }
        public void setValue(String value) {
            value = value;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }

    }


    public void setUpSysCallHash(HashMap<String, String> hash) throws IOException{

        hash.put("0", "sys_read");
        hash.put("1", "sys_write");
        hash.put("2", "sys_open");
        hash.put("3", "sys_close");
        hash.put("4", "sys_stat");
        hash.put("5", "sys_fstat");
        hash.put("6", "sys_lstat");
        hash.put("7", "sys_poll");
        hash.put("8", "sys_lseek");
        hash.put("9", "sys_mmap");
        hash.put("10", "sys_mprotect");
        hash.put("11", "sys_munmap");
        hash.put("12", "sys_brk");
        hash.put("13", "sys_rt_sigaction");
        hash.put("14", "sys_rt_sigprocmask");
        hash.put("15", "sys_rt_sigreturn");
        hash.put("16", "sys_ioctl");
        hash.put("17", "sys_pread64");
        hash.put("18", "sys_pwrite64");
        hash.put("19", "sys_readv");
        hash.put("20", "sys_writev");
        hash.put("21", "sys_access");
        hash.put("22", "sys_pipe");
        hash.put("23", "sys_select");
        hash.put("24", "sys_sched_yield");
        hash.put("25", "sys_mremap");
        hash.put("26", "sys_msync");
        hash.put("27", "sys_minicore");
        hash.put("28", "sys_madvise");
        hash.put("29", "sys_shmget");
        hash.put("30", "sys_shmat");
        hash.put("31", "sys_shmctl");
        hash.put("32", "sys_dup");
        hash.put("33", "sys_dup2");
        hash.put("34", "sys_pause");
        hash.put("35", "sys_nanosleep");
        hash.put("36", "sys_getitimer");
        hash.put("37", "sys_alarm");
        hash.put("38", "sys_setitimer");
        hash.put("39", "sys_getpid");
        hash.put("40", "sys_sendfile64");
        hash.put("41", "sys_socket");
        hash.put("42", "sys_connect");
        hash.put("43", "sys_accept");
        hash.put("44", "sys_sendto");
        hash.put("45", "sys_recvfrom");
        hash.put("46", "sys_sendmsg");
        hash.put("47", "sys_recvmsg");
        hash.put("48", "sys_shutdown");
        hash.put("49", "sys_bind");
        hash.put("50", "sys_listen");
        hash.put("51", "sys_getsockname");
        hash.put("52", "sys_getpeername");
        hash.put("53", "sys_socketpair");
        hash.put("54", "sys_setsockopt");
        hash.put("55", "sys_getsockopt");
        hash.put("56", "stub_clone");
        hash.put("57", "stub_fork");
        hash.put("58", "stub_vfork");
        hash.put("59", "stub_execve");
        hash.put("60", "sys_exit");
        hash.put("61", "sys_wait4");
        hash.put("62", "sys_kill");
        hash.put("63", "sys_newuname");
        hash.put("64", "sys_semget");
        hash.put("65", "sys_semop");
        hash.put("66", "sys_semctl");
        hash.put("67", "sys_schmdt");
        hash.put("68", "sys_msgget");
        hash.put("69", "sys_msgsnd");
        hash.put("70", "sys_msgrcv");
        hash.put("71", "sys_msgctl");
        hash.put("72", "sys_fcntl");
        hash.put("73", "sys_flock");
        hash.put("74", "sys_fsync");
        hash.put("75", "sys_fdatasync");
        hash.put("76", "sys_truncate");
        hash.put("77", "sys_frtuncate");
        hash.put("78", "sys_getdents");
        hash.put("79", "sys_getcwd");
        hash.put("80", "sys_chdir");
        hash.put("81", "sys_fchdir");
        hash.put("82", "sys_rename");
        hash.put("83", "sys_mkdir");
        hash.put("84", "sys_rmdir");
        hash.put("85", "sys_creat");
        hash.put("86", "sys_link");
        hash.put("87", "sys_unlink");
        hash.put("88", "sys_symlink");
        hash.put("89", "sys_readlink");
        hash.put("90", "sys_chmod");
        hash.put("91", "sys_fchmod");
        hash.put("92", "sys_chown");
        hash.put("93", "sys_fchown");
        hash.put("94", "sys_lchown");
        hash.put("95", "sys_umask");
        hash.put("96", "sys_gettimeofday");
        hash.put("97", "sys_getrlimit");
        hash.put("98", "sys_getrusage");
        hash.put("99", "sys_sysinfo");
        hash.put("100", "sys_times");
        hash.put("101", "sys_ptrace");
        hash.put("102", "sys_getuid");
        hash.put("103", "sys_syslog");
        hash.put("104", "sys_getgid");
        hash.put("105", "sys_setuid");
        hash.put("106", "sys_setgid");
        hash.put("107", "sys_geteuid");
        hash.put("108", "sys_getegid");
        hash.put("109", "sys_setpgid");
        hash.put("110", "sys_getppid");
        hash.put("111", "sys_getpgrp");
        hash.put("112", "sys_setsid");
        hash.put("113", "sys_setreuid");
        hash.put("114", "sys_setregid");
        hash.put("115", "sys_getgroups");
        hash.put("116", "sys_setgroups");
        hash.put("117", "sys_setresuid");
        hash.put("118", "sys_getresuid");
        hash.put("119", "sys_setresgid");
        hash.put("120", "sys_getresgid");
        hash.put("121", "sys_getpgid");
        hash.put("122", "sys_setfsuid");
        hash.put("123", "sys_setfsgid");
        hash.put("124", "sys_getsid");
        hash.put("125", "sys_capget");
        hash.put("126", "sys_capset");
        hash.put("127", "sys_rt_sigpending");
        hash.put("128", "sys_rt_sigtimedwait");
        hash.put("129", "sys_rt_sigqueueinfo");
        hash.put("130", "sys_rt_sigsuspend");
        hash.put("131", "sys_sigaltstack");
        hash.put("132", "sys_utime");
        hash.put("133", "sys_mknod");
        hash.put("134", "uselib");
        hash.put("135", "sys_personality");
        hash.put("136", "sys_ustat");
        hash.put("137", "sys_statfs");
        hash.put("138", "sys_fstatfs");
        hash.put("139", "sys_sysfs");
        hash.put("140", "sys_getpriority");
        hash.put("141", "sys_setpriority");
        hash.put("142", "sys_sched_setparam");
        hash.put("143", "sys_sched_getparam");
        hash.put("144", "sys_sched_setscheduler");
        hash.put("145", "sys_sched_getscheduler");
        hash.put("146", "sys_sched_get_priority_max");
        hash.put("147", "sys_sched_get_priority_min");
        hash.put("148", "sys_sched_rr_get_interval");
        hash.put("149", "sys_mlock");
        hash.put("150", "sys_munlock");
        hash.put("151", "sys_mlockall");
        hash.put("152", "sys_munlockall");
        hash.put("153", "sys_vhangup");
        hash.put("154", "sys_modify_ldt");
        hash.put("155", "sys_pivot_root");
        hash.put("156", "sys_sysctl");
        hash.put("157", "sys_prctl");
        hash.put("158", "sys_arch_prctl");
        hash.put("159", "sys_adjtimex");
        hash.put("160", "sys_setrlimit");
        hash.put("161", "sys_chroot");
        hash.put("162", "sys_sync");
        hash.put("163", "sys_acct");
        hash.put("164", "sys_settimeofday");
        hash.put("165", "sys_mount");
        hash.put("166", "sys_umount");
        hash.put("167", "sys_swapon");
        hash.put("168", "sys_swapoff");
        hash.put("169", "sys_reboot");
        hash.put("170", "sys_sethostname");
        hash.put("171", "sys_setdomainname");
        hash.put("172", "stub_iopl");
        hash.put("173", "sys_ioperm");
        hash.put("174", "create_module");
        hash.put("175", "sys_init_module");
        hash.put("176", "sys_delete_module");
        hash.put("177", "get_kernel_syms");
        hash.put("178", "query_module");
        hash.put("179", "sys)quotactl");
        hash.put("180", "nfsservctl");
        hash.put("181", "getpmsg");
        hash.put("182", "putpmsg");
        hash.put("183", "afs_syscall");
        hash.put("184", "tuxcall");
        hash.put("185", "security");
        hash.put("186", "sys_gettid");
        hash.put("187", "sys_readahead");
        hash.put("188", "sys_setxattr");
        hash.put("189", "sys_lsetxattr");
        hash.put("190", "sys_fsetxattr");
        hash.put("191", "sys_getxattr");
        hash.put("192", "sys_lgetxattr");
        hash.put("193", "sys_fgetxattr");
        hash.put("194", "sys_listxattr");
        hash.put("195", "sys_llistxattr");
        hash.put("196", "sys_flistxattr");
        hash.put("197", "sys_removexattr");
        hash.put("198", "sys_lremovexattr");
        hash.put("199", "sys_fremovexattr");
        hash.put("200", "sys_tkill");
        hash.put("201", "sys_time");
        hash.put("202", "sys_futex");
        hash.put("203", "sys_sched_setaffinity");
        hash.put("204", "sys_sched_getaffinity");
        hash.put("205", "set_thread_area");
        hash.put("206", "sys_io_setup");
        hash.put("207", "sys_io_destroy");
        hash.put("208", "sys_io_getevents");
        hash.put("209", "sys_io_submit");
        hash.put("210", "sys_io_cancel");
        hash.put("211", "get_thread_area");
        hash.put("212", "sys_lookup_dcookie");
        hash.put("213", "sys_epoll_create");
        hash.put("214", "epoll_ctl_old");
        hash.put("215", "epoll_wait_old");
        hash.put("216", "sys_remap_file_pages");
        hash.put("217", "sys_getdents64");
        hash.put("218", "sys_set_tid_address");
        hash.put("219", "sys_restart_syscall");
        hash.put("220", "sys_semtimedop");
        hash.put("221", "sys_fadvise64");
        hash.put("222", "sys_timer_create");
        hash.put("223", "sys_timer_settime");
        hash.put("224", "sys_timer_gettime");
        hash.put("225", "sys_timer_getoverrun");
        hash.put("226", "sys_timer_delete");
        hash.put("227", "sys_clock_settime");
        hash.put("228", "sys_clock_gettime");
        hash.put("229", "sys_clock_getres");
        hash.put("230", "sys_clock_nanosleep");
        hash.put("231", "sys_exit_group");
        hash.put("232", "sys_epoll_wait");
        hash.put("233", "sys_epoll_ctl");
        hash.put("234", "sys_tgkill");
        hash.put("235", "sys_utimes");
        hash.put("236", "vserver");
        hash.put("237", "sys_mbind");
        hash.put("238", "sys_set_mempolicy");
        hash.put("239", "sys_get_mempolicy");
        hash.put("240", "sys_mq_open");
        hash.put("241", "sys_mq_unlink");
        hash.put("242", "sys_mq_timedsend");
        hash.put("243", "sys_mq_timedreceive");
        hash.put("244", "sys_mq_notify");
        hash.put("245", "sys_mq_getsetattr");
        hash.put("246", "sys_kexec_load");
        hash.put("247", "sys_waitid");
        hash.put("248", "sys_add_key");
        hash.put("249", "sys_request_key");
        hash.put("250", "sys_keyctl");
        hash.put("251", "sys_ioprio_set");
        hash.put("252", "sys_ioprio_get");
        hash.put("253", "sys_inotify_init");
        hash.put("254", "sys_inotify_add_watch");
        hash.put("255", "sys_inotify_rm_watch");
        hash.put("256", "sys_migrate_pages");
        hash.put("257", "sys_openat");
        hash.put("258", "sys_mkdirat");
        hash.put("259", "sys_mknodat");
        hash.put("260", "sys_fchownat");
        hash.put("261", "sys_futimesat");
        hash.put("262", "sys_newfsatat");
        hash.put("263", "sys_unlinkat");
        hash.put("264", "sys_renameat");
        hash.put("265", "sys_linkat");
        hash.put("266", "sys_symlinkat");
        hash.put("267", "sys_readlinkat");
        hash.put("268", "sys_fchmodat");
        hash.put("269", "sys_faccessat");
        hash.put("270", "sys_pselect6");
        hash.put("271", "sys_ppoll");
        hash.put("272", "sys_unshare");
        hash.put("273", "sys_set_robust_list");
        hash.put("274", "sys_get_robust_list");
        hash.put("275", "sys_splice");
        hash.put("276", "sys_tee");
        hash.put("277", "sys_sync_file_range");
        hash.put("278", "sys_vmsplice");
        hash.put("279", "sys_move_pages");
        hash.put("280", "sys_utimensat");
        hash.put("281", "sys_epoll_pwait");
        hash.put("282", "sys_signalfd");
        hash.put("283", "sys_timerfd_create");
        hash.put("284", "sys_eventfd");
        hash.put("285", "sys_fallocate");
        hash.put("286", "sys_timerfd_settime");
        hash.put("287", "sys_timerfd_gettime");
        hash.put("288", "sys_accept4");
        hash.put("289", "sys_signalfd4");
        hash.put("290", "sys_eventfd2");
        hash.put("291", "sys_epoll_create1");
        hash.put("292", "sys_dup3");
        hash.put("293", "sys_pipe2");
        hash.put("294", "sys_inotify_init1");
        hash.put("295", "sys_preadv");
        hash.put("296", "sys_pwritev");
        hash.put("297", "sys_rt_tgsigqueueinfo");
        hash.put("298", "sys_perf_event_open");
        hash.put("299", "sys_recvmmsg");
        hash.put("300", "sys_fanotify_init");
        hash.put("301", "sys_fanotify_mark");
        hash.put("302", "sys_prlimit64");
        hash.put("303", "sys_name_to_handle_at");
        hash.put("304", "sys_open_by_handle_at");
        hash.put("305", "sys_clock_adjtime");
        hash.put("306", "sys_syncfs");
        hash.put("307", "sys_sendmmsg");
        hash.put("308", "sys_setns");
        hash.put("309", "sys_getcpu");
        hash.put("310", "sys_process_vm_readv");
        hash.put("311", "sys_process_vm_writev");
        hash.put("312", "sys_kcmp");
        hash.put("313", "sys_finit_module");


    }
}
