package org.dyndns.doujindb.ui.dialog.util.list;

import org.dyndns.doujindb.db.DataBaseException;
import org.dyndns.doujindb.db.Record;
import org.dyndns.doujindb.db.containers.ContentContainer;
import org.dyndns.doujindb.db.event.UpdateData;
import org.dyndns.doujindb.db.records.Content;
import org.dyndns.doujindb.ui.UI;
import org.dyndns.doujindb.ui.WindowEx;
import org.dyndns.doujindb.ui.dialog.util.TransferHandlerEx;

@SuppressWarnings("serial")
public class ListContent extends RecordList<Content>
{
	private ContentContainer tokenIContent;
	
	public ListContent(ContentContainer token) throws DataBaseException
	{
		super(token.getContents(), Content.class);
		this.tokenIContent = token;
	}
	
	public boolean contains(Content item)
	{
		boolean contains = false;
		for(Object o : getRecords())
			if(o.equals(item))
				return true;
		return contains;
	}
	
	public java.util.Iterator<Content> iterator()
	{
		return getRecords().iterator();
	}

	@Override
	public void recordUpdated(Record rcd, UpdateData data)
	{
		switch(data.getType())
		{
		case LINK:
			addRecord((Content)data.getTarget());
			break;
		case UNLINK:
			removeRecord((Content)data.getTarget());
			break;
		}
	}
	
	private final class TableModel extends RecordTableModel<Content>
	{
		public TableModel()
		{
			super();
			addColumn("");
			addColumn("Tag Name");
			addColumn("Information");
		}

		public void addRecord(Content content)
		{
			if(containsRecord(content))
				return;
			super.addRow(new Object[]{
					content,
					content.getTagName(),
					content.getInfo()});
		}
	}
	
	private final class RowFilter extends RecordTableRowFilter<RecordTableModel<Content>>
	{

		@Override
		public boolean include(Entry<? extends RecordTableModel<Content>, ? extends Integer> entry)
		{
			String regex = (filterRegex == null || filterRegex.equals("")) ? ".*" : filterRegex;
			Content content = (Content) entry.getModel().getValueAt(entry.getIdentifier(), 0);
        	if(content.isRecycled())
        		return false;
        	return (content.getTagName().matches(regex) ||
        			content.getInfo().matches(regex));
		}
	}

	@Override
	void showRecordWindow(Content record) {
		UI.Desktop.showRecordWindow(WindowEx.Type.WINDOW_CONTENT, record);
	}

	@Override
	void makeTransferHandler() {
		TransferHandlerEx thex;
		thex = new TransferHandlerEx(TransferHandlerEx.Type.CONTENT);
		thex.setDragEnabled(true);
		thex.setDropEnabled(true);
		tableData.setTransferHandler(thex);
	}

	@Override
	RecordTableModel<Content> makeModel() {
		return new TableModel();
	}

	@Override
	RecordTableRowFilter<RecordTableModel<Content>> makeRowFilter() {
		return new RowFilter();
	}
}
