package org.dyndns.doujindb.db.query;

import java.lang.reflect.*;

import org.dyndns.doujindb.db.Record;

public abstract class Query<T extends Record>
{
	public int pagesize = 0; // Query pagination is by default disabled
	
	public enum Type
	{
		OR,
		AND
	}
	
	public Type QueryType;
	
	protected Query()
	{
		QueryType = Type.AND;
	}
	
	@Override
	public String toString() {
		boolean firstField = true;
		String str = "[";
		for(Field field : getClass().getFields())
			try {
				Object value = field.get(this);
				if(value != null)
				{
					str += (firstField ? "" : ", ") + field.getName() + "='" + value + "'";
					firstField = false;
				}
			} catch (IllegalArgumentException iae) {
				iae.printStackTrace();
			} catch (IllegalAccessException iae) {
				iae.printStackTrace();
			}
		str += "]";
		return str;
	}
}
