package org.dyndns.doujindb.db.query;

import org.dyndns.doujindb.db.records.Convention;

/**  
* QueryConvention.java - Used to query Convention(s)
* @author nozomu
* @version 1.0
*/
public final class QueryConvention extends Query<Convention>
{
	public String ID = null;
	public String TagName = null;
	public String Weblink = null;
}