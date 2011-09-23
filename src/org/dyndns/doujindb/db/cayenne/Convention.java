package org.dyndns.doujindb.db.cayenne;

import org.apache.cayenne.PersistenceState;
import org.dyndns.doujindb.db.cayenne.auto._Convention;

@SuppressWarnings("serial")
public class Convention extends _Convention {
	public void setPersistenceState(int state)
	{
		super.setPersistenceState(state);
		if (state == PersistenceState.NEW)
		{
			super.setRecycled(false);
		}
	}
}
