package org.dyndns.doujindb.db.query;

import org.dyndns.doujindb.db.records.Parody;

public final class QueryParody extends Query<Parody>
{
	public Integer Id = null;
	public String JapaneseName = null;
	public String RomajiName = null;
	public String TranslatedName = null;
	public String Weblink = null;
}