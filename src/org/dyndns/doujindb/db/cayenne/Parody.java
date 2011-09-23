package org.dyndns.doujindb.db.cayenne;

import org.apache.cayenne.PersistenceState;
import org.dyndns.doujindb.db.cayenne.auto._Parody;

@SuppressWarnings("serial")
public class Parody extends _Parody {
	public void setPersistenceState(int state)
	{
		super.setPersistenceState(state);
		if (state == PersistenceState.NEW)
		{
			super.setRecycled(false);
		}
	}
}
