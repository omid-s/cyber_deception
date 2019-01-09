package filterStack;

import java.util.*;

import classes.SysdigRecordObject;

public class FilterFactory {

	private final int threshold;
	private static final ArrayList<ArrayList<FilterStackObject>> data = new ArrayList<ArrayList<FilterStackObject>>();

	public FilterFactory(int SelectedThreshold) {
		threshold = SelectedThreshold;
	}

	public boolean shouldBeProcessed(SysdigRecordObject inp) {

		return false;
	}

	public void CheckForError() {
//Date d = Da
		// if( data.stream().anyMatch(x->x.size()>0 && x.get(0).) )
		{
		}
	}
}
