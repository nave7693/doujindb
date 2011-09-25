package org.dyndns.doujindb.db.cayenne;

import org.dyndns.doujindb.db.cayenne.auto._Parody;

@SuppressWarnings("serial")
public class Parody extends _Parody {
	@Override
	protected void postAdd() {
		super.setJapaneseName("");
		super.setTranslatedName("");
		super.setRomanjiName("");
		super.setWeblink("");
		super.setRecycled(false);
	}
}
