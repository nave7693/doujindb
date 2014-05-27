package org.dyndns.doujindb.ui.dialog.util.list;

import org.dyndns.doujindb.db.DataBaseException;
import org.dyndns.doujindb.db.Record;
import org.dyndns.doujindb.db.containers.ArtistContainer;
import org.dyndns.doujindb.db.event.UpdateData;
import org.dyndns.doujindb.db.records.Artist;
import org.dyndns.doujindb.ui.UI;
import org.dyndns.doujindb.ui.WindowEx;
import org.dyndns.doujindb.ui.dialog.util.dnd.ArtistTransferHandler;

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
	
	private final class TableModel extends RecordTableModel<Artist>
	{
		public TableModel()
		{
			super();
			addColumn("");
			addColumn("Japanese");
			addColumn("Translated");
			addColumn("Romaji");
		}

		public void addRecord(Artist artist)
		{
			if(containsRecord(artist))
				return;
			super.addRow(new Object[] {
					artist,
					artist.getJapaneseName(),
					artist.getTranslatedName(),
					artist.getRomajiName()});
		}
	}
	
	private final class RowFilter extends RecordTableRowFilter<RecordTableModel<Artist>>
	{
		@Override
		public boolean include(Entry<? extends RecordTableModel<Artist>, ? extends Integer> entry)
		{
			String regex = (filterRegex == null || filterRegex.equals("")) ? ".*" : filterRegex;
			Artist artist = (Artist) entry.getModel().getValueAt(entry.getIdentifier(), 0);
        	if(artist.isRecycled())
        		return false;
        	return (artist.getJapaneseName().matches(regex) ||
        			artist.getTranslatedName().matches(regex) ||
        			artist.getRomajiName().matches(regex));
		}
	}

	@Override
	void showRecordWindow(Artist record) {
		UI.Desktop.showRecordWindow(WindowEx.Type.WINDOW_ARTIST, record);
	}

	@Override
	void makeTransferHandler() {
		ArtistTransferHandler thex = new ArtistTransferHandler();
		thex.setDragEnabled(true);
		thex.setDropEnabled(true);
		tableData.setTransferHandler(thex);
	}

	@Override
	RecordTableModel<Artist> makeModel() {
		return new TableModel();
	}

	@Override
	RecordTableRowFilter<RecordTableModel<Artist>> makeRowFilter() {
		return new RowFilter();
	}
}
