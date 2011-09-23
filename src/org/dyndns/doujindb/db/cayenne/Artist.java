package org.dyndns.doujindb.db.cayenne;

import org.apache.cayenne.PersistenceState;
import org.dyndns.doujindb.db.cayenne.auto._Artist;

@SuppressWarnings("serial")
public class Artist extends _Artist {
	public void setPersistenceState(int state)
	{
		super.setPersistenceState(state);
		if (state == PersistenceState.NEW)
		{
			super.setRecycled(false);
		}
	}
}
