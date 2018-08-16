package ControlClasses;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Vector;

import javax.vecmath.GVector;

import Classes.AccessCall;
import Classes.ResourceItem;
import Classes.ResourceType;
import Classes.SysdigRecordObject;
import DataBaseStuff.SysdigObjectDAL;
import edu.uci.ics.jung.graph.Graph;

public class GraphObjectHelper
{
	private final boolean			isInVerboseMode;
	private String					pid;
	private HashSet<String>			vectorPid;
	private HashSet<String>			vectorFile;
	private HashMap<String, String>	currentActivity;
	private HashMap<String, String>	currentService;
	private HashSet<String>			blacklist;
	private int						count	= 0;

	public GraphObjectHelper (boolean isInVerboseMode, String _pid)
	{
		this.isInVerboseMode = isInVerboseMode;
		pid = _pid;
		vectorPid = new HashSet<String> ();
		vectorPid.add (_pid);
		currentActivity = new HashMap<String, String> ();
		currentService = new HashMap<String, String> ();
		blacklist = new HashSet<String> ();
		blacklist.add ("/dev/pmsg0");
		blacklist.add ("/dev/cpuctl/tasks");
		blacklist.add ("/dev/cpuctl/bg_non_interactive/tasks");
		blacklist.add ("/dev/ashmem");
		blacklist.add ("1158");
	}

	public String getCurrentActivity (String pid)
	{
		// System.out.println(currentActivity.get(pid));
		return currentActivity.get (pid);
	}

	public void setCurrentActivity (String pid, String name)
	{
		// System.out.println(pid + name);
		currentActivity.put (pid, name);
	}

	public String getCurrentService (String pid)
	{
		// System.out.println(currentActivity.get(pid));
		return currentService.get (pid);
	}

	public void setCurrentService (String pid, String name)
	{
		// System.out.println(pid + name);
		currentService.put (pid, name);
	}

	public void AddRowToGraph (Graph<ResourceItem, AccessCall> theGraph, SysdigRecordObject pick)
	{
		if (count++ % 10000 == 0)
		{
			System.out.println (count + " " + theGraph.getVertexCount () + " " + theGraph.getEdgeCount ());
		}

		boolean isActivity = false;
		boolean isService = false;
		ResourceItem TheProc = null;

		if (pick.evt_type.equals ("openat"))
		{// activity or service
			if (pick.evt_args.startsWith ("dirfd=-100(ENETDOWN) name=activityStart") || pick.evt_args.startsWith ("dirfd=-100(ENETDOWN) name=activityResume"))
			{// Add activity
				String activityName = pick.evt_args.split ("name=")[1].split (pick.evt_args.startsWith ("dirfd=-100(ENETDOWN) name=activityStart") ? "activityStart/" : "activityResume/")[1].split ("flags")[0].trim ();
				setCurrentActivity (pick.thread_tid, activityName);

				return;
			}
			else if (pick.evt_args.startsWith ("dirfd=-100(ENETDOWN) name=activityPause") || pick.evt_args.startsWith ("dirfd=-100(ENETDOWN) name=activityStop"))
			{// Remove activity
				setCurrentActivity (pick.thread_tid, "");

				return;
			}
			else if (pick.evt_args.startsWith ("dirfd=-2015"))
			{// Add bind service
				String serviceName = pick.evt_args.split ("service=")[1].split ("flags=")[0].trim ();
				setCurrentService (pick.thread_tid, serviceName);
			}
			else if (pick.evt_args.startsWith ("dirfd=-100(ENETDOWN) name=serviceStart"))
			{// Add service
				String serviceName = pick.evt_args.split ("serviceStart/")[1].split ("flags=")[0].trim ();

				setCurrentService (pick.thread_tid, serviceName);

				return;
			}
			else if (pick.evt_args.startsWith ("dirfd=-100(ENETDOWN) name=serviceStop"))
			{// Remove service
				setCurrentService (pick.thread_tid, "");

				return;
			}
		}

		// Change thread_tid to activity/service name
		String tmpAppend = "";

		if (getCurrentActivity (pick.thread_tid) != null)
		{
			if (getCurrentActivity (pick.thread_tid).length () != 0)
			{
				tmpAppend = "/Activity" + getCurrentActivity (pick.thread_tid);
				isActivity = true;
			}
		}
		if (getCurrentService (pick.thread_tid) != null)
		{
			if (getCurrentService (pick.thread_tid).length () != 0)
			{
				tmpAppend = "/Service{" + getCurrentService (pick.thread_tid) + "}";
				isService = true;
			}
		}
		if (tmpAppend.length () != 0)
		{
			if (vectorPid.contains (pick.proc_pid))
			{
				vectorPid.add (pick.thread_tid + tmpAppend);
			}
			// pick.proc_ppid = pick.thread_tid;
			pick.thread_tid += tmpAppend;

		}

		if (blacklist.contains (pick.proc_name) || blacklist.contains (pick.thread_tid) || blacklist.contains (pick.proc_pid)) return;

		// ///////////////////////////////////////////////////

		if (!theGraph.getVertices ().stream ().anyMatch (x -> (x.Type == ResourceType.Thread || x.Type == ResourceType.Activity || x.Type == ResourceType.Service) && x.Number.equals (pick.thread_tid)))
		{
			// add the thread/activity/service
			ResourceItem tempItem = new ResourceItem ();

			tempItem.Type = isService ? ResourceType.Service : (isActivity ? ResourceType.Activity : ResourceType.Thread);
			tempItem.id = pick.thread_tid;
			tempItem.Number = pick.thread_tid;
			tempItem.Title = pick.proc_name;
			tempItem.Path = pick.proc_name;
			tempItem.Description = pick.proc_args;

			TheProc = tempItem;
			if (vectorPid.contains (pick.proc_pid))
			{
				vectorPid.add (pick.thread_tid);
				theGraph.addVertex (tempItem);
			}
		}
		else
		{
			TheProc = theGraph.getVertices ().stream ().filter (x -> (x.Type == ResourceType.Thread || x.Type == ResourceType.Activity || x.Type == ResourceType.Service) && x.Number.equals (pick.thread_tid)).findFirst ().get ();
			if (!TheProc.Path.contains (pick.proc_name))
			{
				TheProc.Path += "/" + pick.proc_name;
			}
		}
		if (TheProc == null) return;
		// ///////////////////////////////
		// Add process vertex to services
		if (isService && vectorPid.contains (pick.thread_tid))
		{
			ResourceItem tempItem = null;
			if (!theGraph.getVertices ().stream ().anyMatch (x -> x.Type == ResourceType.Process && x.Number.equals (pick.proc_pid + " / Main Process")))
			{
				tempItem = new ResourceItem ();
				
				tempItem.Type = ResourceType.Process;
				tempItem.id = pick.proc_pid + " / Main Process";
				tempItem.Number = pick.proc_pid + " / Main Process";
				tempItem.Title = pick.proc_name;
				tempItem.Description = "";
				theGraph.addVertex (tempItem);
			}
			else
			{
				tempItem = theGraph.getVertices ().stream ().filter (x -> x.Type == ResourceType.Process && x.Number.equals (pick.proc_pid + " / Main Process")).findFirst ().get ();
			}

			AccessCall tempCallItem = new AccessCall ();
			tempCallItem.From = tempItem;
			tempCallItem.To = TheProc;
			tempCallItem.Command = "service";

			if (!theGraph.getEdges ().stream ().anyMatch (x -> x.Command.equals ("service") && x.From.equals (tempCallItem.From) && x.To.equals (tempCallItem.To)))
			{
				theGraph.addEdge (tempCallItem, tempCallItem.From, tempCallItem.To);
			}
		}

		if (vectorPid.contains (pick.thread_tid) && pick.evt_type.equals ("openat") && pick.evt_args.startsWith ("dirfd=-100(ENETDOWN) name=API:"))
		{// Add API
			String APIName = pick.evt_args.split ("API:")[1].split ("flags=")[0].trim ();
			// String tmpTid = pick.thread_tid.contains ("/Activity{") ? pick.thread_tid.split ("/")[0].trim () : pick.thread_tid;

			ResourceItem tempItem = null;
			// if (!theGraph.getVertices ().stream ().anyMatch (x -> x.Type == ResourceType.StartedService && x.Number.equals (tmpTid + "/ServiceStart{" + serviceName + "}")))
			{
				tempItem = new ResourceItem ();
				tempItem.Type = ResourceType.API;
				tempItem.id = APIName;
				tempItem.Number = APIName;
				tempItem.Title = APIName;
				tempItem.Description = "";
				theGraph.addVertex (tempItem);
			}
			// else
			// {
			// tempItem = theGraph.getVertices ().stream ().filter (x -> x.Type == ResourceType.StartedService && x.Number.equals (tmpTid + "/ServiceStart{" + serviceName + "}")).findFirst ().get ();
			// }

			AccessCall tempCallItem = new AccessCall ();
			tempCallItem.From = tempItem;
			tempCallItem.To = TheProc;
			tempCallItem.Command = "API";

			// if (!theGraph.getEdges ().stream ().anyMatch (x -> x.Command.equals ("serviceStart") && x.From.equals (tempCallItem.From) && x.To.equals (tempCallItem.To)))
			{
				theGraph.addEdge (tempCallItem, tempCallItem.From, tempCallItem.To);
			}
			return;
		}

		// Add serviceStart
		// if (vectorPid.contains (pick.thread_tid) && pick.evt_type.equals ("openat") && pick.evt_args.startsWith ("dirfd=-100(ENETDOWN) name=serviceStart"))
		// {// Add service
		// String serviceName = pick.evt_args.split ("serviceStart/")[1].split ("flags=")[0].trim ();
		// String tmpTid = pick.thread_tid.contains ("/Activity{") ? pick.thread_tid.split ("/")[0].trim () : pick.thread_tid;
		//
		// ResourceItem tempItem = null;
		// if (!theGraph.getVertices ().stream ().anyMatch (x -> x.Type == ResourceType.StartedService && x.Number.equals (tmpTid + "/ServiceStart{" + serviceName + "}")))
		// {
		// tempItem = new ResourceItem ();
		// tempItem.Type = ResourceType.StartedService;
		// tempItem.Number = tmpTid + "/ServiceStart{" + serviceName + "}";
		// tempItem.Title = tmpTid + "/ServiceStart{" + serviceName + "}";
		// tempItem.Description = "";
		// theGraph.addVertex (tempItem);
		// }
		// else
		// {
		// tempItem = theGraph.getVertices ().stream ().filter (x -> x.Type == ResourceType.StartedService && x.Number.equals (tmpTid + "/ServiceStart{" + serviceName + "}")).findFirst ().get ();
		// }
		//
		// AccessCall tempCallItem = new AccessCall ();
		// tempCallItem.From = tempItem;
		// tempCallItem.To = TheProc;
		// tempCallItem.Command = "serviceStart";
		//
		// if (!theGraph.getEdges ().stream ().anyMatch (x -> x.Command.equals ("serviceStart") && x.From.equals (tempCallItem.From) && x.To.equals (tempCallItem.To)))
		// {
		// theGraph.addEdge (tempCallItem, tempCallItem.From, tempCallItem.To);
		// }
		// return;
		// }

		// Link tid and pid
		if (theGraph.getVertices ().stream ().anyMatch (x -> (x.Type == ResourceType.Process || x.Type == ResourceType.Thread || x.Type == ResourceType.Activity || x.Type == ResourceType.Service) && x.Number.startsWith (pick.proc_pid + "/")))
		{
			ResourceItem parentProcess = theGraph.getVertices ().stream ().filter (x -> (x.Type == ResourceType.Process || x.Type == ResourceType.Thread || x.Type == ResourceType.Activity || x.Type == ResourceType.Service) && x.Number.startsWith (pick.proc_pid + "/")).findFirst ().get ();
			ResourceItem thisThread = TheProc;

			// if (!parentProcess.Title.contains (pick.proc_name))
			// {
			// parentProcess.Title += "/" + pick.proc_name;
			// }
			if (!theGraph.getEdges ().stream ().anyMatch (x -> x.From.isEqual (parentProcess) && x.To.isEqual (thisThread)))
			{
				// add the connection to the processes/activity/service
				AccessCall tempCallItem = new AccessCall ();
				tempCallItem.From = parentProcess;
				tempCallItem.To = thisThread;
				tempCallItem.Command = isService ? "service" : (isActivity ? "activity" : "thread");

				if (vectorPid.contains (pick.proc_pid))
				{
					vectorPid.add (pick.thread_tid);
					vectorPid.add (parentProcess.Number);
					theGraph.addEdge (tempCallItem, tempCallItem.From, tempCallItem.To);
				}
			}
		}
		else if (pid.equals (pick.proc_pid))
		{
			
			if (!theGraph.getVertices ().stream ().anyMatch (x -> (x.Type == ResourceType.Process) && x.Number.startsWith (pick.proc_pid + " / Main Process")))
			{
				ResourceItem parentProcess = new ResourceItem ();
				parentProcess.Type = ResourceType.Process;
				parentProcess.id = pick.proc_pid + " / Main Process";
				parentProcess.Number = pick.proc_pid + " / Main Process";
				parentProcess.Title = pick.proc_name;
				parentProcess.Description = pick.evt_args;

				// vectorPid.add (pick.thread_tid);
				vectorPid.add (pick.proc_pid);
				theGraph.addVertex (parentProcess);

				AccessCall tempCallItem = new AccessCall ();
				tempCallItem.From = parentProcess;
				tempCallItem.To = TheProc;
				tempCallItem.Command = isService ? "service" : (isActivity ? "activity" : "thread");

				theGraph.addEdge (tempCallItem, tempCallItem.From, tempCallItem.To);
			}
			else
			{
				ResourceItem parentProcess = theGraph.getVertices ().stream ().filter (x -> (x.Type == ResourceType.Process) && x.Number.startsWith (pick.proc_pid + " / Main Process")).findFirst ().get ();
				ResourceItem thisThread = TheProc;
				if (!theGraph.getEdges ().stream ().anyMatch (x -> x.From.isEqual (parentProcess) && x.To.isEqual (thisThread)))
				{
					AccessCall tempCallItem = new AccessCall ();
					tempCallItem.From = parentProcess;
					tempCallItem.To = TheProc;
					tempCallItem.Command = isService ? "service" : (isActivity ? "activity" : "thread");

					theGraph.addEdge (tempCallItem, tempCallItem.From, tempCallItem.To);
				}
			}
		}

		// Add link to ppid
		if (theGraph.getVertices ().stream ().anyMatch (x -> x.Type == ResourceType.Thread && x.Number.equals (pick.proc_ppid)))
		{
			ResourceItem parentP = theGraph.getVertices ().stream ().filter (x -> x.Type == ResourceType.Thread && x.Number.equals (pick.proc_ppid)).findFirst ().get ();
			ResourceItem tp = TheProc;
			if (!theGraph.getEdges ().stream ().anyMatch (x -> x.From.isEqual (parentP) && x.To.isEqual (tp)))
			{
				// add the connection to the process
				AccessCall tempCallItem = new AccessCall ();
				
				tempCallItem.From = parentP;
				tempCallItem.To = TheProc;
				tempCallItem.Command = "execve";

				if (pick.proc_ppid.equals (pid) || vectorPid.contains (pick.proc_ppid))
				{
					vectorPid.add (tempCallItem.To.Number);
					vectorPid.add (tempCallItem.From.Number);
					theGraph.addEdge (tempCallItem, tempCallItem.From, tempCallItem.To);
				}
			}

		}
		if (pick.evt_type.equals ("openat") && pick.evt_args.startsWith ("dirfd=-2015"))
		{// Add binder connections
			String senderPid = pick.evt_args.split ("sender_pid=")[1].split ("service")[0].trim ();
			final String senderActivity = senderPid + "/Activity" + getCurrentActivity (senderPid);

			final ResourceItem senderProc;
			final ResourceItem receiverProc = TheProc;

			if (vectorPid.contains (senderActivity))
			{
				senderProc = theGraph.getVertices ().stream ().filter (x -> (x.Type == ResourceType.Thread || x.Type == ResourceType.Activity || x.Type == ResourceType.Service) && x.Number.equals (senderActivity)).findFirst ().get ();

				if (!isInVerboseMode && theGraph.getEdges ().stream ().anyMatch (x -> x.Command.equals ("binder") && x.From.equals (senderProc) && x.To.equals (receiverProc)))
				{
					return;
				}
				AccessCall theCall = new AccessCall ();
				theCall.From = senderProc;
				theCall.To = receiverProc;
				theCall.Command = "binder";
				theCall.DateTime = pick.evt_time;
				theCall.Description = pick.arg1;
				theCall.Info = pick.evt_args;

				if ((receiverProc.Number.equals (pid) || senderProc.Number.equals (pid) || vectorPid.contains (receiverProc.Number) || vectorPid.contains (senderProc.Number)))
				{
					vectorPid.add (senderProc.Number);
					vectorPid.add (receiverProc.Number);
					theGraph.addEdge (theCall, theCall.From, theCall.To);
				}
			}
		}
		else
		{
			if (pick.evt_args.contains ("fd="))
			{
				final ResourceItem FromItem;
				final ResourceItem ToItem;
				String fdName = "";
				// String fdFilename = "";
				String fdType = pick.fd_typechar;
				String fdNum = pick.evt_args.split ("fd=")[1];

				if (fdNum.length () != 0)
				{
					if (fdNum.contains ("("))
					{
						fdNum = fdNum.split ("\\(")[0];
					}
					else
					{
						fdNum = fdNum.split (" ")[0];
					}
				}
				if (pick.evt_args.contains ("<") && pick.evt_args.contains (">"))
				{
					// fdType = pick.evt_args.substring (pick.evt_args.indexOf ("<") + 1, pick.evt_args.indexOf (">"));
					fdName = pick.evt_args.substring (pick.evt_args.indexOf (">") + 1).substring (0, pick.evt_args.substring (pick.evt_args.indexOf (">") + 1).indexOf (")"));
				}
				if (blacklist.contains (fdName) || fdName.startsWith ("/system/fonts/")) return;

				final String fdNameFinal = fdName;
				if (theGraph.getVertices ().stream ().anyMatch (x -> (x.Type != ResourceType.Process && x.Type != ResourceType.Thread && x.Type != ResourceType.Activity && x.Type != ResourceType.Service && x.Type != ResourceType.API) && x.Path.equals (fdNameFinal)))
				{// Have item vertex
					FromItem = TheProc;
					ToItem = theGraph.getVertices ().stream ().filter (x -> (x.Type != ResourceType.Process && x.Type != ResourceType.Thread && x.Type != ResourceType.Activity && x.Type != ResourceType.Service && x.Type != ResourceType.API) && x.Path.equals (fdNameFinal)).findFirst ().get ();

					if (!isInVerboseMode && theGraph.getEdges ().stream ().anyMatch (x -> /* x.Command.equals(pick.evt_type) && only one edge is enough */x.From.equals (FromItem) && x.To.equals (ToItem)))
					{
						theGraph.getEdges ().stream ().filter (x -> /* x.Command.equals(pick.evt_type) && only one edge is enough */x.From.equals (FromItem) && x.To.equals (ToItem)).findFirst ().get ().OccuranceFactor++;
					}
					else
					{
						AccessCall theCall = new AccessCall ();
						theCall.From = FromItem;
						theCall.To = ToItem;
						theCall.Command = pick.evt_type;
						theCall.DateTime = pick.evt_time;
						theCall.Description = pick.evt_rawres;
						theCall.Info = pick.evt_args;

						if (vectorPid.contains (FromItem.Number))
						{
							// vectorPid.add(ToItem.Path);
							theGraph.addEdge (theCall, theCall.From, theCall.To);
						}

					}
					return;
				}
				else
				{// Add item vertex
					ResourceItem tempItem = new ResourceItem ();
					// / find type
					ResourceType ItemType = ResourceType.File;
					switch (fdType)
					{
						case "f":
							ItemType = ResourceType.File;
							break;
						case "4":
							ItemType = ResourceType.NetworkIPV4;
							break;
						case "6":
							ItemType = ResourceType.NetworkIPV6;
							break;
						case "p":
							ItemType = ResourceType.Pipe;
							break;
						case "u":
							ItemType = ResourceType.Unix;
							break;
						case "s":
							ItemType = ResourceType.SignalFDs;
							break;
						case "e":
							ItemType = ResourceType.EventFDs;
							break;
						case "i":
							ItemType = ResourceType.iNotifyFDS;
							break;
						case "t":
							ItemType = ResourceType.TimerFDs;
							break;
					}

					if (fdName.length () == 0) return;
					// /end of find type

					tempItem.id = fdName;
					tempItem.Type = ItemType;
					tempItem.Number = fdNum;
					tempItem.Path = fdName;
					tempItem.Title = fdName;
					tempItem.Description = pick.evt_args;

					if (vectorPid.contains (TheProc.Number))
					{// if(fdType.equals("4t"))System.out.println(fdName);
						theGraph.addVertex (tempItem);
					}
					// /////////////////////////////
					FromItem = TheProc;
					ToItem = tempItem;

					// if (ToItem == null)
					// ToItem = theGraph.getVertices().stream().filter(x -> x.Path.equals(fdName)).findFirst().get();
					// create the link item :

					if (!isInVerboseMode && theGraph.getEdges ().stream ().anyMatch (x -> /* x.Command.equals(pick.evt_type) && only one edge is enough */x.From.equals (FromItem) && x.To.equals (ToItem)))
					{
						theGraph.getEdges ().stream ().filter (x -> /* x.Command.equals(pick.evt_type) && only one edge is enough */x.From.equals (FromItem) && x.To.equals (ToItem)).findFirst ().get ().OccuranceFactor++;
					}
					else
					{
						AccessCall theCall = new AccessCall ();
						theCall.From = FromItem;
						theCall.To = ToItem;
						theCall.Command = pick.evt_type;
						theCall.DateTime = pick.evt_time;
						theCall.Description = pick.evt_rawres;
						theCall.Info = pick.evt_args;

						if (vectorPid.contains (FromItem.Number))
						{
							theGraph.addEdge (theCall, theCall.From, theCall.To);
						}
					}
				}
			}
		}
	}
}
