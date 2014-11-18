package org.dyndns.doujindb.conf.event;

import org.dyndns.doujindb.conf.ConfigurationItem;

public interface ConfigurationListener
{
	public <T> void configurationChanged(ConfigurationItem<T> configItem, T oldValue, T newValue);
}
