package org.dyndns.doujindb.ui.dialog.util.combobox;

import java.awt.event.*;

import javax.swing.*;

import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.query.QueryContent;
import org.dyndns.doujindb.db.record.Content;
import org.dyndns.doujindb.db.record.Content.Namespace;

@SuppressWarnings("serial")
public class ComboBoxContent extends SearchComboBox<Content>
{
	@Override
	protected ComboBoxModel<Content> getSuggestedModel(String text)
	{
	    DefaultComboBoxModel<Content> mdl = new DefaultComboBoxModel<Content>();
	    QueryContent qc = new QueryContent();
	    if(text.contains(":")) {
	    	// This query contains a Namespace specified as "ns:content"
	    	// parse the 'text' string accordingly to build the query
	    	// also Capitalize the first letter in Namespace string
	    	String ns = text.substring(0, text.indexOf(':'));
	    	ns = ns.substring(0,1).toUpperCase() + ns.substring(1);
	    	String tname = text.substring(text.indexOf(':') + 1);
	    	// try to parse 'ns' string to Namespace type
	    	try {
	    		qc.Namespace = Namespace.valueOf(ns);
	    	} catch (IllegalArgumentException iae) {
	    		// 'ns' is not a valid Namespace
	    		// query returns automatically 0 rows
	    		 return mdl;
	    	}
	    	qc.TagName = tname + "%";
	    } else {
	    	// Simple text content query, no Namespace involved
	    	qc.TagName = text + "%";
	    }
	    for(Content e : DataBase.getContents(qc)) {
	    	mdl.addElement(e);
	    }
	    return mdl;
	}
}
