package org.dyndns.doujindb.db.query;

import org.dyndns.doujindb.db.records.Artist;

public final class QueryArtist extends Query<Artist>
{
	public Integer Id = null;
	public String JapaneseName = null;
	public String RomajiName = null;
	public String TranslatedName = null;
	public String Weblink = null;
}