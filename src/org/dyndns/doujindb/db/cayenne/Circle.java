package org.dyndns.doujindb.db.cayenne;

import org.dyndns.doujindb.db.cayenne.auto._Circle;

@SuppressWarnings("serial")
public class Circle extends _Circle
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
