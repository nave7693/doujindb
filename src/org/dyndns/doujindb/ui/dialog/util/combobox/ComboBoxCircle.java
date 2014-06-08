package org.dyndns.doujindb.ui.dialog.util.combobox;

import java.awt.event.*;

import javax.swing.*;

import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.query.Query.Type;
import org.dyndns.doujindb.db.query.QueryCircle;
import org.dyndns.doujindb.db.records.Circle;

@SuppressWarnings("serial")
public class ComboBoxCircle extends SearchComboBox<Circle>
{
	@Override
	public void keyPressed(KeyEvent ke)
	{
		JTextField textField = (JTextField) ke.getSource();
		String text = textField.getText();
		switch(ke.getKeyCode())
		{
			case KeyEvent.VK_RIGHT:
				for(Circle e : (Iterable<Circle>) records) {
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
	protected ComboBoxModel<Circle> getSuggestedModel(String text)
	{
	    DefaultComboBoxModel<Circle> mdl = new DefaultComboBoxModel<Circle>();
	    QueryCircle qc = new QueryCircle();
	    qc.QueryType = Type.OR;
	    qc.JapaneseName = text + "%";
	    qc.TranslatedName = text + "%";
	    qc.RomajiName = text + "%";
	    for(Circle e : DataBase.getCircles(qc)) {
	    	mdl.addElement(e);
	    }
	    return mdl;
	}
}
