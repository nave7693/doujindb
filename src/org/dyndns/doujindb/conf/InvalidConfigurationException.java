package org.dyndns.doujindb.conf;

@SuppressWarnings("serial")
public final class InvalidConfigurationException extends ConfigurationException
{
	public InvalidConfigurationException() { }

	public InvalidConfigurationException(String message) { super(message); }

	public InvalidConfigurationException(Throwable cause) { super(cause); }

	public InvalidConfigurationException(String message, Throwable cause) { super(message, cause); }
}
