package org.dyndns.doujindb.ui.dialog.util.combobox;

import java.awt.event.*;

import javax.swing.*;

import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.query.Query.Type;
import org.dyndns.doujindb.db.query.QueryParody;
import org.dyndns.doujindb.db.records.Parody;

@SuppressWarnings("serial")
public class ComboBoxParody extends SearchComboBox<Parody>
{
	@Override
	public void keyPressed(KeyEvent ke)
	{
		JTextField textField = (JTextField) ke.getSource();
		String text = textField.getText();
		switch(ke.getKeyCode())
		{
			case KeyEvent.VK_RIGHT:
				for(Parody e : (Iterable<Parody>) records) {
					if(e.getJapaneseName().startsWith(text)) {
						textField.setText(e.getJapaneseName());
						return;
					}
				}
				break;
			default:
				break;
		}
	}
	
	@Override
	protected ComboBoxModel<Parody> getSuggestedModel(String text)
	{
	    DefaultComboBoxModel<Parody> mdl = new DefaultComboBoxModel<Parody>();
	    QueryParody qp = new QueryParody();
	    qp.QueryType = Type.OR;
	    qp.JapaneseName = text + "%";
	    qp.TranslatedName = text + "%";
	    qp.RomajiName = text + "%";
	    for(Parody e : DataBase.getParodies(qp)) {
	    	mdl.addElement(e);
	    }
	    return mdl;
	}
}
