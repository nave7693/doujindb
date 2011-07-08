package org.dyndns.doujindb.ui.desk.panels.edit;

import java.awt.*;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.dyndns.doujindb.Core;
import org.dyndns.doujindb.core.Database;
import org.dyndns.doujindb.db.containers.HasContent;
import org.dyndns.doujindb.db.records.Content;
import org.dyndns.doujindb.ui.desk.events.*;
import org.dyndns.doujindb.ui.desk.panels.utils.*;





@SuppressWarnings("serial")
public class RecordContentEditor extends JSplitPane implements Validable
{
	private HasContent tokenIContent;
	private DouzCheckBoxList<Content> checkboxList;
	private JTextField searchField = new JTextField("");
	private final Font font = (Font)Core.Settings.getValue("org.dyndns.doujindb.ui.font");
	
	public RecordContentEditor(HasContent token)
	{
		super();
		this.tokenIContent = token;
		searchField.setFont(font);
		setOrientation(JSplitPane.VERTICAL_SPLIT);
		searchField.getDocument().addDocumentListener(new DocumentListener()
		{
		    public void insertUpdate(DocumentEvent e) {
		    	checkboxList.validateUI(new DouzEvent(DouzEvent.DATABASE_REFRESH, null));
		    }
		    public void removeUpdate(DocumentEvent e) {
		    	checkboxList.validateUI(new DouzEvent(DouzEvent.DATABASE_REFRESH, null));
		    }
		    public void changedUpdate(DocumentEvent e) {
		    	checkboxList.validateUI(new DouzEvent(DouzEvent.DATABASE_REFRESH, null));
		    }
		});
		checkboxList = new DouzCheckBoxList<Content>(Database.getContents(), searchField);
		checkboxList.setSelectedItems(tokenIContent.getContents());
		setTopComponent(searchField);
		setBottomComponent(checkboxList);
		setDividerSize(0);
		setEnabled(false);
		validate();
	}
	
	public boolean contains(Content item)
	{
		boolean contains = false;
		for(Object o : checkboxList.getSelectedItems())
			if(o == item)
				return true;
		return contains;
	}
	
	public java.util.Iterator<Content> iterator()
	{
		return checkboxList.getSelectedItems().iterator();
	}
	
	@Override
	public void validateUI(DouzEvent ve)
	{
		if(ve.getType() != DouzEvent.DATABASE_ITEMCHANGED)
			checkboxList.validateUI(ve);
		else
		{
			checkboxList.setSelectedItems(tokenIContent.getContents());
		}
		validate();
	}
	
}