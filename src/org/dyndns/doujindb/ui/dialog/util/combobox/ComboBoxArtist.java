package org.dyndns.doujindb.ui.dialog.util.combobox;

import java.awt.event.*;
import javax.swing.*;

import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.query.QueryArtist;
import org.dyndns.doujindb.db.records.Artist;

@SuppressWarnings("serial")
public class ComboBoxArtist extends SearchComboBox<Artist>
{
	@Override
	public void keyPressed(KeyEvent ke)
	{
		JTextField textField = (JTextField) ke.getSource();
		String text = textField.getText();
		switch(ke.getKeyCode())
		{
			case KeyEvent.VK_RIGHT:
				for(Artist e : (Iterable<Artist>) records) {
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
	protected ComboBoxModel<Artist> getSuggestedModel(String text)
	{
	    DefaultComboBoxModel<Artist> mdl = new DefaultComboBoxModel<Artist>();
	    QueryArtist qa = new QueryArtist();
	    qa.JapaneseName = text + "%";
	    for(Artist e : DataBase.getArtists(qa)) {
	        if(e.getJapaneseName().startsWith(text)) mdl.addElement(e);
	    }
	    return mdl;
	}
}
