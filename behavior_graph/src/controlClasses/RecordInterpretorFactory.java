package controlClasses;

import java.lang.invoke.CallSite;
import java.util.ArrayList;
import java.util.Collections;

import javax.annotation.Resources;

import classes.*;

import java.sql.Connection;

public class RecordInterpretorFactory
{
	private ArrayList<SysdigRecordObject>	SourceList;

	private ArrayList<ResourceItem>			items;
	private ArrayList<AccessCall>			Connections;

	private boolean							IsInVerboseMode	= false;

	public RecordInterpretorFactory (ArrayList<SysdigRecordObject> inp, boolean IsInverboseMode)
	{

		SourceList = inp;
		this.IsInVerboseMode = IsInverboseMode;
	}

	public ArrayList<ResourceItem> GetItems ()
	{
		if (items == null)
		{
			// generate stuff
			makeCollections ();
		}
		return items;
	}

	public ArrayList<AccessCall> GetCalls ()
	{
		if (Connections == null)
		{
			// /to do : craete the collections ~
			makeCollections ();
		}
		return Connections;
	}

	private void makeCollections ()
	{
		items = new ArrayList<ResourceItem> ();
		Connections = new ArrayList<AccessCall> ();
		for (SysdigRecordObject pick : SourceList)
		{
			ResourceItem FromItem = null;
			ResourceItem ToItem = null;
			if (!items.stream ().anyMatch (x -> x.Type == ResourceType.Process && x.Number.equals (pick.proc_pid)))
			{
				// add the process
				ResourceItem tempItem = new ResourceItem ();

				tempItem.Type = ResourceType.Process;
				tempItem.Number = pick.proc_pid;
				tempItem.Title = pick.proc_name;
				tempItem.Description = pick.proc_args;

				items.add (tempItem);
			}
			if (!items.stream ().anyMatch (x -> x.Type == ResourceType.Thread && x.Number.equals (pick.thread_tid)))
			{
				// add the thread
				ResourceItem tempItem = new ResourceItem ();

				tempItem.Type = ResourceType.Thread;
				tempItem.Number = pick.thread_tid;
				tempItem.Title = pick.proc_name;
				tempItem.Description = pick.thread_totexectime;

				items.add (tempItem);

				FromItem = tempItem;

				ResourceItem TempProcItem = items.stream ().filter (x -> x.Number.equals (pick.proc_pid) && x.Type == ResourceType.Process).findFirst ().get ();

				// add the connection to the process
				AccessCall tempCallItem = new AccessCall ();
				tempCallItem.From = TempProcItem;
				tempCallItem.To = tempItem;
				tempCallItem.Command = "exec";

				Connections.add (tempCallItem);
			}

			if (pick.fd_num != "<NA>" && !items.stream ().anyMatch (x -> (x.Type != ResourceType.Process && x.Type != ResourceType.Thread) && x.Number.equals (pick.fd_num)))
			{
				ResourceItem tempItem = new ResourceItem ();
				// / find type
				ResourceType ItemType = ResourceType.File;
				switch (pick.fd_typechar.toLowerCase ())
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

				// /end of find type

				tempItem.Type = ItemType;
				tempItem.Number = pick.fd_num;
				tempItem.Path = pick.fd_directory;
				tempItem.Title = pick.fd_name;
				// tempItem.Description = pick.fd_

				items.add (tempItem);

				ToItem = tempItem;
			}

			// assert pick.thread_tid != "<NA>";
			if (!pick.fd_num.equals ("<NA>"))
			{
				if (FromItem == null) FromItem = items.stream ().filter (x -> x.Number.equals (pick.thread_tid) && x.Type == ResourceType.Thread).findFirst ().get ();
				if (ToItem == null) ToItem = items.stream ().filter (x -> x.Number.equals (pick.fd_num)).findFirst ().get ();

				// create the link item :
				final ResourceItem FF = FromItem;
				final ResourceItem TT = ToItem;
				if (!IsInVerboseMode && Connections.stream ().anyMatch (x -> x.Command.equals (pick.evt_type) && x.From.equals (FF) && x.To.equals (TT)))
				{
					Connections.stream ().filter (x -> x.Command.equals (pick.evt_type) && x.From.equals (FF) && x.To.equals (TT)).findFirst ().get ().OccuranceFactor++;

				}
				else
				{

					AccessCall theCall = new AccessCall ();
					theCall.From = FromItem;
					theCall.To = ToItem;
					theCall.Command = pick.evt_type;
					theCall.DateTime = pick.evt_time_s;
					theCall.Description = pick.evt_rawres;
					theCall.Info = pick.evt_args;

					Connections.add (theCall);
				}
			}
		}
	}
}
