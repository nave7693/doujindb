package org.dyndns.doujindb.ui.dialog.util.list;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

import javax.swing.*;

import org.dyndns.doujindb.conf.Configuration;
import org.dyndns.doujindb.dat.DataStore;
import org.dyndns.doujindb.dat.DataStoreException;
import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.container.BookContainer;
import org.dyndns.doujindb.db.event.UpdateData;
import org.dyndns.doujindb.db.query.QueryBook;
import org.dyndns.doujindb.db.record.Book;
import org.dyndns.doujindb.ui.UI;
import org.dyndns.doujindb.ui.WindowEx;
import org.dyndns.doujindb.ui.dialog.util.WrapLayout;
import org.dyndns.doujindb.ui.dialog.util.combobox.ComboBoxBook;
import org.dyndns.doujindb.ui.dialog.util.dnd.TransferHandlerBook;
import org.dyndns.doujindb.util.ImageTool;

import static org.dyndns.doujindb.ui.UI.Icon;

@SuppressWarnings("serial")
public class ListBook extends RecordList<Book> implements ActionListener, LayoutManager
{
	private JPanel recordPreview;
	private JScrollPane scrollRecordPreview;
	private boolean previewToggled = false;
	private boolean previewEnabled = Configuration.ui_panel_book_preview.get();
	private JButton toggleList;
	private JButton togglePreview;
	
	public ListBook(BookContainer token) throws DataBaseException
	{
		super(token.getBooks());
		super.setLayout(this);
		
		searchComboBox = new ComboBoxBook();
		searchComboBox.setHotkeyTarget(addRecord);
		addRecord.setToolTipText("Add Book");
		addRecord.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent ae) {
				Object selectedItem = searchComboBox.getSelectedItem();
				if(selectedItem != null && selectedItem instanceof Book)
					addRecord((Book) selectedItem);
			}
		});
		toggleList = new JButton(Icon.desktop_explorer_table_view_list);
		toggleList.setToolTipText("Toggle List");
		toggleList.addActionListener(this);
		toggleList.setFocusable(false);
		recordPreview = new JPanel();
		recordPreview.setLayout(new WrapLayout());
		scrollRecordPreview = new JScrollPane(recordPreview);
		scrollRecordPreview.getVerticalScrollBar().setUnitIncrement(25);
		scrollRecordPreview.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollRecordPreview.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {
			@Override
			public void adjustmentValueChanged(AdjustmentEvent ae) {
		        int extent = scrollRecordPreview.getVerticalScrollBar().getModel().getExtent();
		        if((scrollRecordPreview.getVerticalScrollBar().getValue() + extent) == scrollRecordPreview.getVerticalScrollBar().getMaximum())
		        	loadData();
		    }
		});
		togglePreview = new JButton(Icon.desktop_explorer_table_view_preview);
		togglePreview.setToolTipText("Toggle Preview");
		togglePreview.addActionListener(this);
		togglePreview.setFocusable(false);
		if(previewEnabled)
		{
			super.add(toggleList);
			super.add(togglePreview);
			super.add(scrollRecordPreview);
		}
		add(searchComboBox);
		add(addRecord);
		
		loadData();
	}
	
	@Override
	public void addRecord(Book record)
	{
		Integer bookId = record.getId();
		if(previewEnabled && !tableModel.containsRecord(record))
		{
			JButton bookButton;
			try {
				bookButton = new JButton(
					new ImageIcon(
						ImageTool.read(DataStore.getThumbnail(bookId).openInputStream())));
				bookButton.setName(bookId.toString());
				bookButton.setActionCommand(bookId.toString());
				bookButton.addActionListener(this);
				bookButton.setBorder(null);
				recordPreview.add(bookButton);
			} catch (DataBaseException | IOException | DataStoreException e) {
				e.printStackTrace();
			}
		}
		super.addRecord(record);
	}
	
	@Override
	public void removeRecord(Book record)
	{
		Integer bookId = record.getId();
		for(Component comp : recordPreview.getComponents())
			if(comp.getName().equals(bookId.toString()))
				recordPreview.remove(comp);
		super.removeRecord(record);
	}
	
	@Override
	public void addLayoutComponent(String name, Component comp) { }

	@Override
	public void removeLayoutComponent(Component comp) { }

	@Override
	public Dimension preferredLayoutSize(Container parent) { return new Dimension(250, 	250); }

	@Override
	public Dimension minimumLayoutSize(Container parent) { return new Dimension(250, 250); }

	@Override
	public void layoutContainer(Container parent)
	{
		if(previewEnabled)
		{
			int width = parent.getWidth(),
				height = parent.getHeight();
			searchComboBox.setBounds(0, 0, width - 41, 20);
			addRecord.setBounds(width - 40, 0, 20, 20);
			if(!previewToggled)
			{
				toggleList.setBounds(0, 0, 0, 0);
				togglePreview.setBounds(width - 20, 0, 20, 20);
				scrollPane.setBounds(0, 21, width, height - 20);
				scrollRecordPreview.setBounds(0, 0, 0, 0);
			} else {
				toggleList.setBounds(width - 20, 0, 20, 20);
				togglePreview.setBounds(0, 0, 0, 0);
				scrollPane.setBounds(0, 0, 0, 0);
				scrollRecordPreview.setBounds(0, 21, width, height - 20);
			}
		} else
			super.layoutContainer(parent);
	}
	
	@Override
	public void actionPerformed(ActionEvent ae)
	{
		if(ae.getSource().equals(toggleList))
		{
			previewToggled = false;
			doLayout();
			toggleList.validate();
			return;
		}
		if(ae.getSource().equals(togglePreview))
		{
			previewToggled = true;
			doLayout();
			scrollRecordPreview.validate();
			return;
		}
		if(ae.getActionCommand() != null)
		{
			QueryBook query = new QueryBook();
			query.Id = Integer.parseInt(ae.getActionCommand());
			RecordSet<Book> result = DataBase.getBooks(query);
			openRecordWindow(result.iterator().next());
		}
	}
	
	public boolean contains(Book item)
	{
		boolean contains = false;
		for(Object o : getRecords())
			if(o.equals(item))
				return true;
		return contains;
	}
	
	public java.util.Iterator<Book> iterator()
	{
		return getRecords().iterator();
	}
	
	@Override
	public void recordUpdated(Record rcd, UpdateData data)
	{
		switch(data.getType())
		{
		case LINK:
				addRecord((Book)data.getTarget());
			break;
		case UNLINK:
				removeRecord((Book)data.getTarget());
			break;
		}
	}
	
	private final class TableModel extends RecordTableModel<Book>
	{
		public TableModel()
		{
			super();
			addColumn("");
			addColumn("Japanese");
			addColumn("Translated");
			addColumn("Romaji");
		}

		public void addRecord(Book book)
		{
			if(containsRecord(book))
				return;
			super.addRow(new Object[] {
					book,
					book.getJapaneseName(),
					book.getTranslatedName(),
					book.getRomajiName()});
		}
	}
	
	@Override
	protected void openRecordWindow(Book record) {
		UI.Desktop.showRecordWindow(WindowEx.Type.WINDOW_BOOK, record);
	}

	@Override
	protected void registerTransferHandler() {
		TransferHandlerBook thex = new TransferHandlerBook();
		thex.setDragEnabled(true);
		thex.setDropEnabled(true);
		tableData.setTransferHandler(thex);
	}

	@Override
	protected RecordTableModel<Book> getModel() {
		return new TableModel();
	}
}
