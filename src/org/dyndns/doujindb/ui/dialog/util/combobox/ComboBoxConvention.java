package org.dyndns.doujindb.ui.dialog.util.combobox;

import java.awt.event.*;

import javax.swing.*;

import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.query.QueryConvention;
import org.dyndns.doujindb.db.record.Convention;

@SuppressWarnings("serial")
public class ComboBoxConvention extends SearchComboBox<Convention>
{
	@Override
	protected ComboBoxModel<Convention> getSuggestedModel(String text)
	{
	    DefaultComboBoxModel<Convention> mdl = new DefaultComboBoxModel<Convention>();
	    QueryConvention qc = new QueryConvention();
	    qc.TagName = text + "%";
	    for(Convention e : DataBase.getConventions(qc)) {
	    	mdl.addElement(e);
	    }
	    return mdl;
	}
}
