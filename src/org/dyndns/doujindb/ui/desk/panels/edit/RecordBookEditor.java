package org.dyndns.doujindb.ui.desk.panels.edit;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.dyndns.doujindb.Core;
import org.dyndns.doujindb.db.DataBaseException;
import org.dyndns.doujindb.db.Record;
import org.dyndns.doujindb.db.containers.CntBook;
import org.dyndns.doujindb.db.event.DataBaseListener;
import org.dyndns.doujindb.db.event.UpdateData;
import org.dyndns.doujindb.db.records.Book;
import org.dyndns.doujindb.ui.desk.panels.util.*;

@SuppressWarnings("serial")
public class RecordBookEditor extends JPanel implements LayoutManager, ActionListener, DataBaseListener
{
	private CntBook tokenIBook;
	private RecordList<Book> recordList;
	private JTextField searchField;
	private boolean previewToggled = false;
	private JButton toggleList;
	private JButton togglePreview;
	private final Font font = Core.Resources.Font;
	
	public RecordBookEditor(CntBook token) throws DataBaseException
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
		togglePreview = new JButton(Core.Resources.Icons.get("Desktop/Explorer/Table/View/Preview"));
		togglePreview.setToolTipText("Toggle Preview");
		togglePreview.addActionListener(this);
		togglePreview.setFocusable(false);
		super.add(searchField);
		super.add(toggleList);
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
			togglePreview.setBounds(width - 20, 0, 20, 20);
		} else {
			toggleList.setBounds(width - 20, 0, 20, 20);
			togglePreview.setBounds(0, 0, 0, 0);
		}
		recordList.setBounds(0, 20, width, height - 20);
	}
	
	@Override
	public void actionPerformed(ActionEvent ae)
	{
		if(ae.getSource().equals(toggleList))
		{
			previewToggled = false;
			doLayout();
			return;
		}
		if(ae.getSource().equals(togglePreview))
		{
			previewToggled = true;
			doLayout();
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