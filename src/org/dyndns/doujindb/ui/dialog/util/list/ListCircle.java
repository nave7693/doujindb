package org.dyndns.doujindb.ui.dialog.util.list;

import org.dyndns.doujindb.db.DataBaseException;
import org.dyndns.doujindb.db.Record;
import org.dyndns.doujindb.db.containers.CircleContainer;
import org.dyndns.doujindb.db.event.UpdateData;
import org.dyndns.doujindb.db.records.Circle;
import org.dyndns.doujindb.ui.UI;
import org.dyndns.doujindb.ui.WindowEx;
import org.dyndns.doujindb.ui.dialog.util.dnd.CircleTransferHandler;

@SuppressWarnings("serial")
public class ListCircle extends RecordList<Circle>
{
	private CircleContainer tokenICircle;
	
	public ListCircle(CircleContainer token) throws DataBaseException
	{
		super(token.getCircles(), Circle.class);
		this.tokenICircle = token;
	}
	
	public boolean contains(Circle item)
	{
		boolean contains = false;
		for(Object o : getRecords())
			if(o.equals(item))
				return true;
		return contains;
	}
	
	public java.util.Iterator<Circle> iterator()
	{
		return getRecords().iterator();
	}

	@Override
	public void recordUpdated(Record rcd, UpdateData data)
	{
		switch(data.getType())
		{
		case LINK:
			addRecord((Circle)data.getTarget());
			break;
		case UNLINK:
			removeRecord((Circle)data.getTarget());
			break;
		}
	}
	
	private final class TableModel extends RecordTableModel<Circle>
	{
		public TableModel()
		{
			super();
			addColumn("");
			addColumn("Japanese");
			addColumn("Translated");
			addColumn("Romaji");
		}

		public void addRecord(Circle circle)
		{
			if(containsRecord(circle))
				return;
			super.addRow(new Object[] {
					circle,
					circle.getJapaneseName(),
					circle.getTranslatedName(),
					circle.getRomajiName()});
		}
	}
	
	private final class RowFilter extends RecordTableRowFilter<RecordTableModel<Circle>>
	{

		@Override
		public boolean include(Entry<? extends RecordTableModel<Circle>, ? extends Integer> entry)
		{
			String regex = (filterRegex == null || filterRegex.equals("")) ? ".*" : filterRegex;
			Circle circle = (Circle) entry.getModel().getValueAt(entry.getIdentifier(), 0);
        	if(circle.isRecycled())
        		return false;
        	return (circle.getJapaneseName().matches(regex) ||
        			circle.getTranslatedName().matches(regex) ||
        			circle.getRomajiName().matches(regex));
		}
	}

	@Override
	void showRecordWindow(Circle record) {
		UI.Desktop.showRecordWindow(WindowEx.Type.WINDOW_CIRCLE, record);
	}

	@Override
	void makeTransferHandler() {
		CircleTransferHandler thex = new CircleTransferHandler();
		thex.setDragEnabled(true);
		thex.setDropEnabled(true);
		tableData.setTransferHandler(thex);
	}

	@Override
	RecordTableModel<Circle> makeModel() {
		return new TableModel();
	}

	@Override
	RecordTableRowFilter<RecordTableModel<Circle>> makeRowFilter() {
		return new RowFilter();
	}
}

