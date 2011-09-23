package org.dyndns.doujindb.db.cayenne;

import org.apache.cayenne.PersistenceState;
import org.dyndns.doujindb.db.cayenne.auto._Circle;

@SuppressWarnings("serial")
public class Circle extends _Circle {
	public void setPersistenceState(int state)
	{
		super.setPersistenceState(state);
		if (state == PersistenceState.NEW)
		{
			super.setRecycled(false);
		}
	}
}
