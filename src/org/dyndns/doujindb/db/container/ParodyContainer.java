package org.dyndns.doujindb.db.container;

import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.record.*;

public interface ParodyContainer
{
	public RecordSet<Parody> getParodies() throws DataBaseException;
	public void addParody(Parody parody) throws DataBaseException;
	public void removeParody(Parody parody) throws DataBaseException;
}
