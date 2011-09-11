package org.dyndns.doujindb.ui.desk.panels.edit;

import java.awt.*;
import java.rmi.RemoteException;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.dyndns.doujindb.Core;
import org.dyndns.doujindb.Client;
import org.dyndns.doujindb.db.DataBaseException;
import org.dyndns.doujindb.db.containers.CntParody;
import org.dyndns.doujindb.db.records.Parody;
import org.dyndns.doujindb.log.Level;
import org.dyndns.doujindb.ui.desk.events.*;
import org.dyndns.doujindb.ui.desk.panels.utils.*;

@SuppressWarnings("serial")
public class RecordParodyEditor extends JSplitPane implements Validable
{
	private CntParody tokenIParody;
	private DouzCheckBoxList<Parody> checkboxList;
	private JTextField searchField = new JTextField("");
	private final Font font = Core.Properties.get("org.dyndns.doujindb.ui.font").asFont();
	
	public RecordParodyEditor(CntParody token) throws DataBaseException, RemoteException
	{
		super();
		this.tokenIParody = token;
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
		checkboxList = new DouzCheckBoxList<Parody>(Client.DB.getParodies(null), searchField);
		checkboxList.setSelectedItems(tokenIParody.getParodies());
		setTopComponent(searchField);
		setBottomComponent(checkboxList);
		setDividerSize(0);
		setEnabled(false);
		validate();
	}
	
	public boolean contains(Parody item)
	{
		boolean contains = false;
		for(Object o : checkboxList.getSelectedItems())
			if(o.equals(item))
				return true;
		return contains;
	}
	
	public java.util.Iterator<Parody> iterator()
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
			try {
				checkboxList.setSelectedItems(tokenIParody.getParodies());
			} catch (RemoteException re) {
				Core.Logger.log(re.getMessage(), Level.ERROR);
				re.printStackTrace();
			}
		}
		validate();
	}
	
}