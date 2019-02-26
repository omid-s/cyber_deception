/**
 * 
 */
package classes;

/**
 * @author omid This class contains a 2 or 3 vertices and 1 or 2 edges that
 *         represents each sysdig object record
 */
public class SysdigRecordObjectGraph {

	/**
	 * This method creates an instance of the graph object class
	 * 
	 * 
	 */
	public SysdigRecordObjectGraph() {
		// TODO Auto-generated constructor stub
	}

	private ResourceItem proc;
	private ResourceItem parentProc;
	private AccessCall exec;
	private AccessCall syscall;
	private ResourceItem item;
	
	private ResourceItem thread ;
	private ResourceItem UBSIUnit; 
	private AccessCall spawn ; 
	private AccessCall ubsi_start; 
	
	
	/**
	 * This method creates an instance of the graph object class usaing values
	 * 
	 * @param proc       the process
	 * @param parentProc the parent process
	 * @param exec       the exec that created the process
	 * @param syscall    the system call of the main record
	 * @param item       the item on which the system call was executed
	 */
	
	public SysdigRecordObjectGraph(ResourceItem proc, ResourceItem parentProc, AccessCall exec, AccessCall syscall,
			ResourceItem item) {
		super();
		this.proc = proc;
		this.parentProc = parentProc;
		this.exec = exec;
		this.syscall = syscall;
		this.item = item;
	}

	/**
	 * This method creates an instance of the graph object class usaing values
	 * 
	 * @param proc       the process
	 * @param parentProc the parent process
	 * @param exec       the exec that created the process
	 * @param syscall    the system call of the main record
	 * @param item       the item on which the system call was executed
	 */
	
	public SysdigRecordObjectGraph(ResourceItem proc, ResourceItem parentProc, AccessCall exec , AccessCall syscall,
			ResourceItem item  , ResourceItem  thread , AccessCall spwan , ResourceItem ubsi_unit , AccessCall ubsi_start) {
		super();
		this.proc = proc;
		this.parentProc = parentProc;
		this.exec = exec;
		this.syscall = syscall;
		this.item = item;
		this.thread = thread ;
		this.UBSIUnit = ubsi_unit;
		this.spawn = spwan;
		this.ubsi_start = ubsi_start;
	}

	
	
	public ResourceItem getThread() {
		return thread;
	}



	public void setThread(ResourceItem thread) {
		this.thread = thread;
	}



	public ResourceItem getUBSIUnit() {
		return UBSIUnit;
	}



	public void setUBSIUnit(ResourceItem uBSIUnit) {
		UBSIUnit = uBSIUnit;
	}



	public AccessCall getSpawn() {
		return spawn;
	}



	public void setSpawn(AccessCall spawn) {
		this.spawn = spawn;
	}



	public AccessCall getUbsi_start() {
		return ubsi_start;
	}



	public void setUbsi_start(AccessCall ubsi_start) {
		this.ubsi_start = ubsi_start;
	}



	public ResourceItem getProc() {
		return proc;
	}

	public void setProc(ResourceItem proc) {
		this.proc = proc;
	}

	public ResourceItem getParentProc() {
		return parentProc;
	}

	public void setParentProc(ResourceItem parentProc) {
		this.parentProc = parentProc;
	}

	public AccessCall getExec() {
		return exec;
	}

	public void setExec(AccessCall exec) {
		this.exec = exec;
	}

	public AccessCall getSyscall() {
		return syscall;
	}

	public void setSyscall(AccessCall syscall) {
		this.syscall = syscall;
	}

	public ResourceItem getItem() {
		return item;
	}

	public void setItem(ResourceItem item) {
		this.item = item;
	}

}
