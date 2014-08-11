package org.dyndns.doujindb.ui.dialog.util.combobox;

import java.awt.event.*;

import javax.swing.*;

import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.query.Query.Type;
import org.dyndns.doujindb.db.query.QueryBook;
import org.dyndns.doujindb.db.record.Book;

@SuppressWarnings("serial")
public class ComboBoxBook extends SearchComboBox<Book>
{
	@Override
	public void keyPressed(KeyEvent ke)
	{
		JTextField textField = (JTextField) ke.getSource();
		String text = textField.getText();
		switch(ke.getKeyCode())
		{
			case KeyEvent.VK_RIGHT:
				for(Book e : (Iterable<Book>) records) {
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
	protected ComboBoxModel<Book> getSuggestedModel(String text)
	{
	    DefaultComboBoxModel<Book> mdl = new DefaultComboBoxModel<Book>();
	    QueryBook qb = new QueryBook();
	    qb.QueryType = Type.OR;
	    qb.JapaneseName = text + "%";
	    qb.TranslatedName = text + "%";
	    qb.RomajiName = text + "%";
	    for(Book e : DataBase.getBooks(qb)) {
	    	mdl.addElement(e);
	    }
	    return mdl;
	}
}
