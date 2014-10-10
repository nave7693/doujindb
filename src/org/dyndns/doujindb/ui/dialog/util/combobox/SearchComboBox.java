package org.dyndns.doujindb.ui.dialog.util.combobox;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

import org.dyndns.doujindb.db.*;

/**
 * @author Terai Atsuhiro
 * @see http://java-swing-tips.blogspot.it/2009/01/create-auto-suggest-jcombobox.html
 */

@SuppressWarnings("serial")
public abstract class SearchComboBox<T extends Record> extends JComboBox<T> implements KeyListener
{
	protected final Vector<T> records = new Vector<T>();
	
	public SearchComboBox()
	{
		super();
		super.setEditable(true);
		super.setSelectedIndex(-1);
		JTextField field = (JTextField) super.getEditor().getEditorComponent();
        field.setText("");
        field.addKeyListener(this);
	}
	
	@Override
	public void keyPressed(final KeyEvent ke)
	{
		if(ke.getKeyCode() != KeyEvent.VK_ENTER)
			return;
		EventQueue.invokeLater(new Runnable()
		{
			@Override public void run()
			{
				String text = ((JTextField) ke.getSource()).getText();
				if(text.length() == 0) {
					setSuggestionModel(SearchComboBox.this, new DefaultComboBoxModel<T>(records), "");
					SearchComboBox.this.hidePopup();
				} else {
					ComboBoxModel<T> mdl = getSuggestedModel(text);
					if(mdl.getSize() == 0) {
						SearchComboBox.this.hidePopup();
					} else {
						setSuggestionModel(SearchComboBox.this, mdl, text);
						SearchComboBox.this.showPopup();
					}
				}
			}
		});
	}

	@Override
	public void keyReleased(KeyEvent ke) { }

	@Override
	public void keyTyped(KeyEvent ke) { }

	private void setSuggestionModel(JComboBox<T> comboBox, ComboBoxModel<T> mdl, String text)
	{
	    comboBox.setModel(mdl);
	    comboBox.setSelectedIndex(-1);
	    ((JTextField) comboBox.getEditor().getEditorComponent()).setText(text);
	}
	
	protected abstract ComboBoxModel<T> getSuggestedModel(String text);
}
