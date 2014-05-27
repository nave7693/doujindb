package org.dyndns.doujindb.ui.dialog.util.list;

import org.dyndns.doujindb.db.DataBaseException;
import org.dyndns.doujindb.db.Record;
import org.dyndns.doujindb.db.containers.ParodyContainer;
import org.dyndns.doujindb.db.event.UpdateData;
import org.dyndns.doujindb.db.records.Parody;
import org.dyndns.doujindb.ui.UI;
import org.dyndns.doujindb.ui.WindowEx;
import org.dyndns.doujindb.ui.dialog.util.dnd.ParodyTransferHandler;

@SuppressWarnings("serial")
public class ListParody extends RecordList<Parody>
{
	private ParodyContainer tokenIParody;
	
	public ListParody(ParodyContainer token) throws DataBaseException
	{
		super(token.getParodies(), Parody.class);
		this.tokenIParody = token;
	}
	
	public boolean contains(Parody item)
	{
		boolean contains = false;
		for(Object o : getRecords())
			if(o.equals(item))
				return true;
		return contains;
	}
	
	public java.util.Iterator<Parody> iterator()
	{
		return getRecords().iterator();
	}

	@Override
	public void recordUpdated(Record r, UpdateData data)
	{
		switch(data.getType())
		{
		case LINK:
			addRecord((Parody)data.getTarget());
			break;
		case UNLINK:
			removeRecord((Parody)data.getTarget());
			break;
		}
	}
	
	private final class TableModel extends RecordTableModel<Parody>
	{
		public TableModel()
		{
			super();
			addColumn("");
			addColumn("Japanese");
			addColumn("Translated");
			addColumn("Romaji");
		}

		public void addRecord(Parody parody)
		{
			if(containsRecord(parody))
				return;
			super.addRow(new Object[] {
					parody,
					parody.getJapaneseName(),
					parody.getTranslatedName(),
					parody.getRomajiName()});
		}
	}
	
	private final class RowFilter extends RecordTableRowFilter<RecordTableModel<Parody>>
	{

		@Override
		public boolean include(Entry<? extends RecordTableModel<Parody>, ? extends Integer> entry)
		{
			String regex = (filterRegex == null || filterRegex.equals("")) ? ".*" : filterRegex;
			Parody parody = (Parody) entry.getModel().getValueAt(entry.getIdentifier(), 0);
        	if(parody.isRecycled())
        		return false;
        	return (parody.getJapaneseName().matches(regex) ||
        			parody.getTranslatedName().matches(regex) ||
        			parody.getRomajiName().matches(regex));
		}
	}

	@Override
	void showRecordWindow(Parody record) {
		UI.Desktop.showRecordWindow(WindowEx.Type.WINDOW_PARODY, record);
	}

	@Override
	void makeTransferHandler() {
		ParodyTransferHandler thex = new ParodyTransferHandler();
		thex.setDragEnabled(true);
		thex.setDropEnabled(true);
		tableData.setTransferHandler(thex);
	}

	@Override
	RecordTableModel<Parody> makeModel() {
		return new TableModel();
	}

	@Override
	RecordTableRowFilter<RecordTableModel<Parody>> makeRowFilter() {
		return new RowFilter();
	}
}
