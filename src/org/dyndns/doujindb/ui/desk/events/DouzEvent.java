package org.dyndns.doujindb.ui.desk.events;

public class DouzEvent
{
	public enum Type
	{
		DATABASE_UPDATE,
		DATABASE_INSERT,
		DATABASE_DELETE,
		DATABASE_REFRESH,
		DATABASE_COMMIT,
		DATABASE_ROLLBACK,
		DATABASE_CONNECT,
		DATABASE_DISCONNECT,
		
		SETTINGS_LOADED,
		SETTINGS_CHANGED
	}
	
	private Object parameter;
	private Type type;
	
	public DouzEvent(Type type)
	{
		this(type, null);
	}
	
	public DouzEvent(Type type, Object parameter)
	{
		this.parameter = parameter;
		this.type = type;
	}

	public Object getParameter() {
		return parameter;
	}

	public Type getType() {
		return type;
	}
}
