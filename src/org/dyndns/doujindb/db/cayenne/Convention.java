package org.dyndns.doujindb.db.cayenne;

import org.dyndns.doujindb.db.cayenne.auto._Convention;

@SuppressWarnings("serial")
public class Convention extends _Convention
{
	@Override
	protected void postAdd() {
		super.setTagName("");
		super.setInfo("");
		super.setWeblink("");
		super.setRecycled(false);
	}
	
	public Integer getID() {
		return (Integer) getObjectId().getIdSnapshot().get(ID_PK_COLUMN);
	}
}