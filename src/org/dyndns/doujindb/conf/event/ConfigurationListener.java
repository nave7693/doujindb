package org.dyndns.doujindb.conf.event;

public interface ConfigurationListener
{
	public void configAdded(String key);
	public void configDeleted(String key);
	public void configUpdated(String key);
}
