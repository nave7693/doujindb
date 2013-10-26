package org.dyndns.doujindb.ui.desk.panels.edit;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.dyndns.doujindb.Core;
import org.dyndns.doujindb.db.DataBaseException;
import org.dyndns.doujindb.db.Record;
import org.dyndns.doujindb.db.containers.BookContainer;
import org.dyndns.doujindb.db.event.DataBaseListener;
import org.dyndns.doujindb.db.event.UpdateData;
import org.dyndns.doujindb.db.records.Book;
import org.dyndns.doujindb.ui.desk.WindowEx;
import org.dyndns.doujindb.ui.desk.panels.util.*;
import org.dyndns.doujindb.util.ImageTool;

@SuppressWarnings("serial")
public class RecordBookEditor extends JPanel implements LayoutManager, ActionListener, DataBaseListener
{
	private BookContainer tokenIBook;
	private RecordList<Book> recordList;
	private JPanel recordPreview;
	private JScrollPane scrollRecordPreview;
	private JTextField searchField;
	private boolean previewToggled = false;
	private JButton toggleList;
	private JButton togglePreview;
	private final Font font = Core.Resources.Font;
	
	public RecordBookEditor(BookContainer token) throws DataBaseException
	{
		super();
		this.tokenIBook = token;
		super.setLayout(this);
		searchField = new JTextField("");
		searchField.setFont(font);
		searchField.getDocument().addDocumentListener(new DocumentListener()
		{
		    public void insertUpdate(DocumentEvent e) {
		    	recordList.filterChanged(searchField.getText());
		    }
		    public void removeUpdate(DocumentEvent e) {
		    	recordList.filterChanged(searchField.getText());
		    }
		    public void changedUpdate(DocumentEvent e) {
		    	recordList.filterChanged(searchField.getText());
		    }
		});
		recordList = new RecordList<Book>(tokenIBook.getBooks(), Book.class);
		toggleList = new JButton(Core.Resources.Icons.get("Desktop/Explorer/Table/View/List"));
		toggleList.setToolTipText("Toggle List");
		toggleList.addActionListener(this);
		toggleList.setFocusable(false);
		recordPreview = new JPanel();
		recordPreview.setLayout(new WrapLayout());
		new SwingWorker<Void,JButton>()
		{
			@Override
			protected Void doInBackground() throws Exception
			{
				for(final Book book : tokenIBook.getBooks())
				{
					JButton bookButton;
					bookButton = new JButton(
						new ImageIcon(
							ImageTool.read(Core.Repository.getPreview(book.getID()).getInputStream())));
					bookButton.addActionListener(new ActionListener()
					{
						@Override
						public void actionPerformed(ActionEvent ae) {
							Core.UI.Desktop.showRecordWindow(WindowEx.Type.WINDOW_BOOK, book);
						}
					});
					publish(bookButton);
				}
				return null;
			}
			@Override
			protected void process(List<JButton> chunks) {
				for(JButton button : chunks) {
					recordPreview.add(button);
				}
			}
			@Override
			protected void done()
			{
				recordPreview.validate();
				recordPreview.doLayout();
			}
		}.execute();
		scrollRecordPreview = new JScrollPane(recordPreview);
		scrollRecordPreview.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		togglePreview = new JButton(Core.Resources.Icons.get("Desktop/Explorer/Table/View/Preview"));
		togglePreview.setToolTipText("Toggle Preview");
		togglePreview.addActionListener(this);
		togglePreview.setFocusable(false);
		super.add(searchField);
		super.add(recordList);
		super.add(toggleList);
		super.add(scrollRecordPreview);
		super.add(togglePreview);
		super.add(recordList);
		super.setEnabled(false);
		validate();
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
		int width = parent.getWidth(),
			height = parent.getHeight();
		searchField.setBounds(0, 0, width - 20, 20);
		if(!previewToggled)
		{
			toggleList.setBounds(0, 0, 0, 0);
			recordList.setBounds(0, 20, width, height - 20);
			togglePreview.setBounds(width - 20, 0, 20, 20);
			scrollRecordPreview.setBounds(0, 0, 0, 0);
		} else {
			toggleList.setBounds(width - 20, 0, 20, 20);
			recordList.setBounds(0, 0, 0, 0);
			togglePreview.setBounds(0, 0, 0, 0);
			scrollRecordPreview.setBounds(0, 20, width, height - 20);
		}
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
	}
	
	public boolean contains(Book item)
	{
		boolean contains = false;
		for(Object o : recordList.getRecords())
			if(o.equals(item))
				return true;
		return contains;
	}
	
	public java.util.Iterator<Book> iterator()
	{
		return recordList.getRecords().iterator();
	}
	
	public RecordList<Book> getRecordList()
	{
		return recordList;
	}

	@Override
	public void setEnabled(boolean enabled)
	{
		recordList.setEnabled(enabled);
		searchField.setEnabled(enabled);
	}

	@Override
	public void recordAdded(Record rcd) { }
	
	@Override
	public void recordDeleted(Record rcd) { }
	
	@Override
	public void recordUpdated(Record rcd, UpdateData data)
	{
		switch(data.getType())
		{
		case LINK:
			recordList.addRecord((Book)data.getTarget());
			break;
		case UNLINK:
			recordList.removeRecord((Book)data.getTarget());
			break;
		}
	}
	
	@Override
	public void databaseConnected() { }
	
	@Override
	public void databaseDisconnected() { }
	
	@Override
	public void databaseCommit() { }
	
	@Override
	public void databaseRollback() { }

	@Override
	public void recordRecycled(Record rcd)
	{
		recordList.recordsChanged();
	}

	@Override
	public void recordRestored(Record rcd)
	{
		recordList.recordsChanged();
	}
}