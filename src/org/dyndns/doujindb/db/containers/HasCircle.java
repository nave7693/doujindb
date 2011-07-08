package org.dyndns.doujindb.db.containers;

import java.util.Set;

import org.dyndns.doujindb.db.records.Circle;


public interface HasCircle
{
	public Set<Circle> getCircles();
}
