package org.dyndns.doujindb.conf;

@SuppressWarnings("serial")
public final class ReadOnlyConfigurationException extends ConfigurationException
{
	public ReadOnlyConfigurationException() { }

	public ReadOnlyConfigurationException(String message) { super(message); }

	public ReadOnlyConfigurationException(Throwable cause) { super(cause); }

	public ReadOnlyConfigurationException(String message, Throwable cause) { super(message, cause); }
}
