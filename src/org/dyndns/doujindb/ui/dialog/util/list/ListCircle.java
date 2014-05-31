package org.dyndns.doujindb.ui.dialog.util.list;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.dyndns.doujindb.db.DataBaseException;
import org.dyndns.doujindb.db.Record;
import org.dyndns.doujindb.db.containers.CircleContainer;
import org.dyndns.doujindb.db.event.UpdateData;
import org.dyndns.doujindb.db.records.Circle;
import org.dyndns.doujindb.ui.UI;
import org.dyndns.doujindb.ui.WindowEx;
import org.dyndns.doujindb.ui.dialog.util.combobox.ComboBoxCircle;
import org.dyndns.doujindb.ui.dialog.util.dnd.TransferHandlerCircle;

@SuppressWarnings("serial")
public class ListCircle extends RecordList<Circle>
{
	public ListCircle(CircleContainer token) throws DataBaseException
	{
		super(token.getCircles());
		searchComboBox = new ComboBoxCircle();
		add(searchComboBox);
		addRecord.setToolTipText("Add Circle");
		addRecord.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent ae) {
				Object selectedItem = searchComboBox.getSelectedItem();
				if(selectedItem != null && selectedItem instanceof Circle)
					tableModel.addRecord((Circle) selectedItem);
			}
		});
		add(addRecord);
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
	
	@Override
	protected void openRecordWindow(Circle record) {
		UI.Desktop.showRecordWindow(WindowEx.Type.WINDOW_CIRCLE, record);
	}

	@Override
	protected void registerTransferHandler() {
		TransferHandlerCircle thex = new TransferHandlerCircle();
		thex.setDragEnabled(true);
		thex.setDropEnabled(true);
		tableData.setTransferHandler(thex);
	}

	@Override
	protected RecordTableModel<Circle> getModel() {
		return new TableModel();
	}
}
