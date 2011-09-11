package org.dyndns.doujindb.db;

import java.io.Serializable;

/**  
* Record.java - DoujinDB database record.
* @author  nozomu
* @version 1.0
*/
public interface Record extends Serializable
{
	public String getID();
}