package org.dyndns.doujindb.ui.dialog.util.list;

import org.dyndns.doujindb.db.DataBaseException;
import org.dyndns.doujindb.db.Record;
import org.dyndns.doujindb.db.containers.ArtistContainer;
import org.dyndns.doujindb.db.event.UpdateData;
import org.dyndns.doujindb.db.records.Artist;

@SuppressWarnings("serial")
public class ListArtist extends RecordList<Artist>
{
	private ArtistContainer tokenIArtist;
	
	public ListArtist(ArtistContainer token) throws DataBaseException
	{
		super(token.getArtists(), Artist.class);
		this.tokenIArtist = token;
	}
	
	public boolean contains(Artist item)
	{
		boolean contains = false;
		for(Object o : getRecords())
			if(o.equals(item))
				return true;
		return contains;
	}
	
	public java.util.Iterator<Artist> iterator()
	{
		return getRecords().iterator();
	}
	
	@Override
	public void recordUpdated(Record rcd, UpdateData data)
	{
		switch(data.getType())
		{
		case LINK:
			addRecord((Artist)data.getTarget());
			break;
		case UNLINK:
			removeRecord((Artist)data.getTarget());
			break;
		}
	}
}
