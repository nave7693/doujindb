package org.dyndns.doujindb.ui.desk.panels.edit;

import java.awt.*;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.dyndns.doujindb.Core;
import org.dyndns.doujindb.core.Database;
import org.dyndns.doujindb.db.containers.CircleContainer;
import org.dyndns.doujindb.db.records.Circle;
import org.dyndns.doujindb.ui.desk.events.*;
import org.dyndns.doujindb.ui.desk.panels.utils.*;





@SuppressWarnings("serial")
public class RecordCircleEditor extends JSplitPane implements Validable
{
	private CircleContainer tokenICircle;
	private DouzCheckBoxList<Circle> checkboxList;
	private JTextField searchField = new JTextField("");
	private final Font font = Core.Properties.get("org.dyndns.doujindb.ui.font").asFont();
	
	public RecordCircleEditor(CircleContainer token)
	{
		super();
		this.tokenICircle = token;
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
		checkboxList = new DouzCheckBoxList<Circle>(Database.getCircles(), searchField);
		checkboxList.setSelectedItems(tokenICircle.getCircles());
		setTopComponent(searchField);
		setBottomComponent(checkboxList);
		setDividerSize(0);
		setEnabled(false);
		validate();
	}
	
	public boolean contains(Circle item)
	{
		boolean contains = false;
		for(Object o : checkboxList.getSelectedItems())
			if(o == item)
				return true;
		return contains;
	}
	
	public java.util.Iterator<Circle> iterator()
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
			checkboxList.setSelectedItems(tokenICircle.getCircles());
		}
		validate();
	}
	
}