package org.dyndns.doujindb.ui.dialog.util.list;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.dyndns.doujindb.db.DataBaseException;
import org.dyndns.doujindb.db.Record;
import org.dyndns.doujindb.db.container.ContentContainer;
import org.dyndns.doujindb.db.event.UpdateData;
import org.dyndns.doujindb.db.record.Content;
import org.dyndns.doujindb.ui.UI;
import org.dyndns.doujindb.ui.WindowEx;
import org.dyndns.doujindb.ui.dialog.util.combobox.ComboBoxContent;
import org.dyndns.doujindb.ui.dialog.util.dnd.TransferHandlerContent;

@SuppressWarnings("serial")
public class ListContent extends RecordList<Content>
{
	public ListContent(ContentContainer token) throws DataBaseException
	{
		super(token.getContents());
		searchComboBox = new ComboBoxContent();
		searchComboBox.setHotkeyTarget(addRecord);
		add(searchComboBox);
		addRecord.setToolTipText("Add Content");
		addRecord.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent ae) {
				Object selectedItem = searchComboBox.getSelectedItem();
				if(selectedItem != null && selectedItem instanceof Content)
					tableModel.addRecord((Content) selectedItem);
			}
		});
		add(addRecord);
		
		loadData();
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
	
	@Override
	protected void openRecordWindow(Content record) {
		UI.Desktop.showRecordWindow(WindowEx.Type.WINDOW_CONTENT, record);
	}

	@Override
	protected void registerTransferHandler() {
		TransferHandlerContent thex = new TransferHandlerContent();
		thex.setDragEnabled(true);
		thex.setDropEnabled(true);
		tableData.setTransferHandler(thex);
	}

	@Override
	protected RecordTableModel<Content> getModel() {
		return new TableModel();
	}
}
