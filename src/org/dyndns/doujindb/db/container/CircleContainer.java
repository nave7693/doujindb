package org.dyndns.doujindb.db.container;

import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.record.*;

public interface CircleContainer
{
	public RecordSet<Circle> getCircles() throws DataBaseException;
	public void addCircle(Circle circle) throws DataBaseException;
	public void removeCircle(Circle circle) throws DataBaseException;
}
