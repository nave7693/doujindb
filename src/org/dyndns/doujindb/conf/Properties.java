package org.dyndns.doujindb.conf;

import java.io.*;


/**  
* Properties.java - Manages all the settings
* @author  nozomu
* @version 1.0
*/
public interface Properties
{
	public Serializable getValue(String key) throws PropertyException;
	
	public void setValue(String key, Serializable value) throws PropertyException;
	
	public void newValue(String key, Serializable value) throws PropertyException;
	
	public Iterable<String> values();
	
	public boolean containsValue(String key);
	
	public Serializable getDescription(String key) throws PropertyException;
	
	public void setDescription(String key, String value) throws PropertyException;
}
