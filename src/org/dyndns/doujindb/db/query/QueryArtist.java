package org.dyndns.doujindb.db.query;

import org.dyndns.doujindb.db.records.Artist;

/**  
* QueryArtist.java - Used to query Artist(s)
* @author nozomu
* @version 1.0
*/
public final class QueryArtist extends Query<Artist>
{
	public String ID = null;
	public String JapaneseName = null;
	public String RomajiName = null;
	public String TranslatedName = null;
	public String Weblink = null;
}