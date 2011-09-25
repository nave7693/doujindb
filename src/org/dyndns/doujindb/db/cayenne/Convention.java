package org.dyndns.doujindb.db.cayenne;

import org.dyndns.doujindb.db.cayenne.auto._Convention;

@SuppressWarnings("serial")
public class Convention extends _Convention {
	@Override
	protected void postAdd() {
		super.setRecycled(false);
	}
}
