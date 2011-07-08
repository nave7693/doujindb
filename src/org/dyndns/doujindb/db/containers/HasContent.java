package org.dyndns.doujindb.db.containers;

import java.util.Set;

import org.dyndns.doujindb.db.records.Content;


public interface HasContent
{
	public Set<Content> getContents();
}
