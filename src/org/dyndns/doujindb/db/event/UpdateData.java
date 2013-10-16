package org.dyndns.doujindb.db.event;

import org.dyndns.doujindb.db.*;

public class UpdateData
{
	public enum Type
	{
		PROPERTY,
		LINK,
		UNLINK
	}
	
	private Type type;
	private String property;
	private Record target;
	
	private UpdateData() { }
	
	public static UpdateData link(Record record)
	{
		UpdateData data = new UpdateData();
		data.type = Type.LINK;
		data.target = record;
		return data; 
	}
	
	public static UpdateData unlink(Record record)
	{
		UpdateData data = new UpdateData();
		data.type = Type.UNLINK;
		data.target = record;
		return data; 
	}
	
	public static UpdateData property(String property)
	{
		UpdateData data = new UpdateData();
		data.type = Type.PROPERTY;
		data.property = property;
		return data; 
	}

	public Type getType()
	{
		return type;
	}

	public String getProperty()
	{
		return property;
	}

	public Record getTarget()
	{
		return target;
	}
}