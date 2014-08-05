package org.dyndns.doujindb.db.containers;

import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.records.*;

public interface CircleContainer
{
	public RecordSet<Circle> getCircles() throws DataBaseException;
	public void addCircle(Circle circle) throws DataBaseException;
	public void removeCircle(Circle circle) throws DataBaseException;
}
