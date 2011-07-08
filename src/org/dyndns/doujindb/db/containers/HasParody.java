package org.dyndns.doujindb.db.containers;

import java.util.Set;

import org.dyndns.doujindb.db.records.Parody;


public interface HasParody
{
	public Set<Parody> getParodies();
}
