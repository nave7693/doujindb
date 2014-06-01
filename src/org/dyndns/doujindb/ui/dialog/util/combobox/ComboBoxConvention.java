package org.dyndns.doujindb.ui.dialog.util.combobox;

import java.awt.event.*;
import javax.swing.*;

import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.query.QueryConvention;
import org.dyndns.doujindb.db.records.Convention;

@SuppressWarnings("serial")
public class ComboBoxConvention extends SearchComboBox<Convention>
{
	@Override
	public void keyPressed(KeyEvent ke)
	{
		JTextField textField = (JTextField) ke.getSource();
		String text = textField.getText();
		switch(ke.getKeyCode())
		{
			case KeyEvent.VK_RIGHT:
				for(Convention e : (Iterable<Convention>) records) {
					if(e.getTagName().startsWith(text)) {
						textField.setText(e.getTagName());
						return;
					}
				}
				break;
			default:
				break;
		}
	}
	
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
