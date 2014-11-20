package org.dyndns.doujindb.conf;

public final class ConfigurationItem<T>
{
	private T value;
	private T defaultValue;
	private String info;
	private Validator<T> validator;
	private final boolean readOnly;

	public ConfigurationItem(T defaultValue, String info) {
		this(defaultValue, defaultValue, info);
	}

	public ConfigurationItem(T defaultValue, T value, String info) {
		this(defaultValue, value, info, new Validator<T>() {
			@Override
			public boolean isValid(T value) {
				return true;
			}
		});
	}
	
	public ConfigurationItem(T defaultValue, T value, String info, Validator<T> validator) {
		this(defaultValue, defaultValue, info, validator, false);
	}

	public ConfigurationItem(T defaultValue, T value, String info, Validator<T> validator, boolean readOnly) {
		this.defaultValue = defaultValue;
		this.value = value;
		this.info = info;
		this.validator = validator;
		this.readOnly = readOnly;
	}
	
	public T get() {
		return this.value;
	}
	
	public void set(T value) throws InvalidConfigurationException, ReadOnlyConfigurationException {
		if(!validator.isValid(value))
			throw new InvalidConfigurationException("Invalid value '" + value + "'");
		if(readOnly)
			throw new ReadOnlyConfigurationException("Configuration is read-only");
		Configuration.fireConfigurationChange(this, this.value, value);
		this.value = value;
	}
	
	public String getInfo() {
		return this.info;
	}
	
	public boolean isDefault() {
		return readOnly;
	}
	
	public boolean isReadOnly() {
		return this.defaultValue.equals(this.value);
	}
	
	public void reset() {
		this.value = this.defaultValue;
	}
	
	@Override
	public String toString() {
		return value.toString();
	}
	
	public Class<?> getType() {
		return value.getClass();
	}
	
	public interface Validator<T>
	{
		public abstract boolean isValid(T value);
	}
}
