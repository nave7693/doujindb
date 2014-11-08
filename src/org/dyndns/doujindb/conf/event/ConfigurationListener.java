package org.dyndns.doujindb.conf.event;

public interface ConfigurationListener
{
	public void configurationAdded(String key);
	public void configurationDeleted(String key);
	public void configurationUpdated(String key);
}
