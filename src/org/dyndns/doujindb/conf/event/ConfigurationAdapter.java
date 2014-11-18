package org.dyndns.doujindb.conf.event;

import org.dyndns.doujindb.conf.ConfigurationItem;

public class ConfigurationAdapter implements ConfigurationListener
{
	@Override
	public <T> void configurationChanged(ConfigurationItem<T> configItem, T oldValue, T newValue) { }
}
