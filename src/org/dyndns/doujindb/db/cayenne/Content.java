package org.dyndns.doujindb.db.cayenne;

import org.apache.cayenne.PersistenceState;
import org.dyndns.doujindb.db.cayenne.auto._Content;

@SuppressWarnings("serial")
public class Content extends _Content {
	public void setPersistenceState(int state)
	{
		super.setPersistenceState(state);
		if (state == PersistenceState.NEW)
		{
			super.setRecycled(false);
		}
	}
}
