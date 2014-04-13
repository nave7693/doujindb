package org.dyndns.doujindb.ui.desk.panels.util;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.query.QueryConvention;
import org.dyndns.doujindb.db.records.Convention;

/**
 * @author Terai Atsuhiro
 * @see http://java-swing-tips.blogspot.it/2009/01/create-auto-suggest-jcombobox.html
 */

@SuppressWarnings("serial")
public final class ComboBoxConvention extends JComboBox<Convention>
{
	public ComboBoxConvention()
	{
		super(new Convention[]{});
		super.setEditable(true);
		super.setSelectedIndex(-1);
		JTextField field = (JTextField) super.getEditor().getEditorComponent();
        field.setText("");
        field.addKeyListener(new ComboKeyHandler(this));
	}
	
	class ComboKeyHandler extends KeyAdapter
	{
		private final JComboBox<Convention> comboBox;
		private final Vector<Convention> conventionList = new Vector<Convention>();
		
		public ComboKeyHandler(JComboBox<Convention> combo)
		{
			this.comboBox = combo;
			for(int i=0;i<comboBox.getModel().getSize();i++) {
				conventionList.addElement((Convention)comboBox.getItemAt(i));
			}
		}
		
		@Override
		public void keyTyped(final KeyEvent ke)
		{
			EventQueue.invokeLater(new Runnable()
			{
				@Override public void run()
				{
					String text = ((JTextField) ke.getSource()).getText();
					if(text.length() == 0) {
						setSuggestionModel(comboBox, new DefaultComboBoxModel<Convention>(conventionList), "");
						comboBox.hidePopup();
					} else {
						ComboBoxModel<Convention> mdl = getSuggestedModel(text);
						if(mdl.getSize() == 0) {
							comboBox.hidePopup();
						} else {
							setSuggestionModel(comboBox, mdl, text);
							comboBox.showPopup();
						}
					}
				}
			});
		}
		
		@Override public void keyPressed(KeyEvent ke)
		{
			JTextField textField = (JTextField) ke.getSource();
			String text = textField.getText();
			switch(ke.getKeyCode())
			{
				case KeyEvent.VK_RIGHT:
					for(Convention e : conventionList) {
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
		
		private void setSuggestionModel(JComboBox<Convention> comboBox, ComboBoxModel<Convention> mdl, String text)
		{
		    comboBox.setModel(mdl);
		    comboBox.setSelectedIndex(-1);
		    ((JTextField) comboBox.getEditor().getEditorComponent()).setText(text);
		}
		
		private ComboBoxModel<Convention> getSuggestedModel(String text)
		{
		    DefaultComboBoxModel<Convention> mdl = new DefaultComboBoxModel<Convention>();
		    QueryConvention qc = new QueryConvention();
		    qc.TagName = text + "%";
		    for(Convention e : DataBase.getConventions(qc)) {
		        if(e.getTagName().startsWith(text)) mdl.addElement(e);
		    }
		    return mdl;
		}
	}
}
