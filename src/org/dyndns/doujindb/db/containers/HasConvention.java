package org.dyndns.doujindb.db.containers;

import org.dyndns.doujindb.db.records.Convention;

public interface HasConvention
{
	public Convention getConvention();
	public void setConvention(Convention convention);
}
