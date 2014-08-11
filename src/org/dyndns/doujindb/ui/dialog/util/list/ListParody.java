package org.dyndns.doujindb.ui.dialog.util.list;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.dyndns.doujindb.db.DataBaseException;
import org.dyndns.doujindb.db.Record;
import org.dyndns.doujindb.db.container.ParodyContainer;
import org.dyndns.doujindb.db.event.UpdateData;
import org.dyndns.doujindb.db.record.Parody;
import org.dyndns.doujindb.ui.UI;
import org.dyndns.doujindb.ui.WindowEx;
import org.dyndns.doujindb.ui.dialog.util.combobox.ComboBoxParody;
import org.dyndns.doujindb.ui.dialog.util.dnd.TransferHandlerParody;

@SuppressWarnings("serial")
public class ListParody extends RecordList<Parody>
{
	public ListParody(ParodyContainer token) throws DataBaseException
	{
		super(token.getParodies());
		searchComboBox = new ComboBoxParody();
		add(searchComboBox);
		addRecord.setToolTipText("Add Parody");
		addRecord.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent ae) {
				Object selectedItem = searchComboBox.getSelectedItem();
				if(selectedItem != null && selectedItem instanceof Parody)
					tableModel.addRecord((Parody) selectedItem);
			}
		});
		add(addRecord);
		
		loadData();
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
	
	@Override
	protected void openRecordWindow(Parody record) {
		UI.Desktop.showRecordWindow(WindowEx.Type.WINDOW_PARODY, record);
	}

	@Override
	protected void registerTransferHandler() {
		TransferHandlerParody thex = new TransferHandlerParody();
		thex.setDragEnabled(true);
		thex.setDropEnabled(true);
		tableData.setTransferHandler(thex);
	}

	@Override
	protected RecordTableModel<Parody> getModel() {
		return new TableModel();
	}
}
