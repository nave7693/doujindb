package org.dyndns.doujindb.db.query;

import org.dyndns.doujindb.db.records.Parody;

/**  
* QueryParody.java - Used to query Parody(ies)
* @author nozomu
* @version 1.0
*/
public final class QueryParody extends Query<Parody>
{
	public Integer Id = null;
	public String JapaneseName = null;
	public String RomajiName = null;
	public String TranslatedName = null;
	public String Weblink = null;
}