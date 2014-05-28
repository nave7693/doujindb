package org.dyndns.doujindb.ui.dialog.util.combobox;

import java.awt.event.*;
import javax.swing.*;

import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.query.QueryContent;
import org.dyndns.doujindb.db.records.Content;

@SuppressWarnings("serial")
public class ComboBoxContent extends SearchComboBox<Content>
{
	@Override
	public void keyPressed(KeyEvent ke)
	{
		JTextField textField = (JTextField) ke.getSource();
		String text = textField.getText();
		switch(ke.getKeyCode())
		{
			case KeyEvent.VK_RIGHT:
				for(Content e : (Iterable<Content>) records) {
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
	protected ComboBoxModel<Content> getSuggestedModel(String text)
	{
	    DefaultComboBoxModel<Content> mdl = new DefaultComboBoxModel<Content>();
	    QueryContent qc = new QueryContent();
	    qc.TagName = text + "%";
	    for(Content e : DataBase.getContents(qc)) {
	        if(e.getTagName().startsWith(text)) mdl.addElement(e);
	    }
	    return mdl;
	}
}
