package org.dyndns.doujindb.db.query;

import org.dyndns.doujindb.db.records.Circle;

/**  
* QueryCircle.java - Used to query Circle(s)
* @author nozomu
* @version 1.0
*/
public final class QueryCircle extends Query<Circle>
{
	public Integer Id = null;
	public String JapaneseName = null;
	public String RomajiName = null;
	public String TranslatedName = null;
	public String Weblink = null;
}