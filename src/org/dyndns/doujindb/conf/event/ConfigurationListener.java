package org.dyndns.doujindb.conf.event;

public interface ConfigurationListener
{
	public void propertyAdded(String prop);
	public void propertyDeleted(String prop);
	public void propertyUpdated(String prop);
}