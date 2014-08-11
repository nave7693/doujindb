package org.dyndns.doujindb.db;

public interface RecordSet<T> extends Iterable<T>
{
	public int size();
}
