package org.dyndns.doujindb.db.containers;

import java.util.Set;

import org.dyndns.doujindb.db.records.Circle;

/**  
* CircleContainer.java - Interface every item in the DB containing circle(s) must implement.
* @author nozomu
* @version 1.0
*/
public interface CircleContainer
{
	public Set<Circle> getCircles();
}
