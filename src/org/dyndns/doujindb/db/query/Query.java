package org.dyndns.doujindb.db.query;

import java.lang.reflect.*;

import org.dyndns.doujindb.db.Record;

public abstract class Query<T extends Record>
{
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
