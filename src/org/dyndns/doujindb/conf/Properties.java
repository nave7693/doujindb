package org.dyndns.doujindb.conf;

/**  
* Properties.java - Manages all the settings
* @author  nozomu
* @version 1.0
*/
public interface Properties
{
	public Property get(String key) throws PropertyException;
	
	public void add(String key) throws PropertyException;
	
	public void remove(String key) throws PropertyException;
	
	public boolean contains(String key);
	
	public Iterable<String> keys();
	
	public Iterable<Property> values();
	
	public void load() throws PropertyException;
	
	public void save() throws PropertyException;
}
