package org.dyndns.doujindb.db.cayenne;

import org.dyndns.doujindb.db.cayenne.auto._Artist;

@SuppressWarnings("serial")
public class Artist extends _Artist
{
	@Override
	protected void postAdd() {
		super.setJapaneseName("");
		super.setTranslatedName("");
		super.setRomajiName("");
		super.setWeblink("");
		super.setRecycled(false);
	}
	
	public Integer getID() {
		return (Integer) getObjectId().getIdSnapshot().get(ID_PK_COLUMN);
	}
}
