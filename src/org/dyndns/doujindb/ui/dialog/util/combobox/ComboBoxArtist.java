package org.dyndns.doujindb.ui.dialog.util.combobox;

import java.awt.event.*;

import javax.swing.*;

import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.query.Query.Type;
import org.dyndns.doujindb.db.query.QueryArtist;
import org.dyndns.doujindb.db.record.Artist;

@SuppressWarnings("serial")
public class ComboBoxArtist extends SearchComboBox<Artist>
{
	@Override
	protected ComboBoxModel<Artist> getSuggestedModel(String text)
	{
	    DefaultComboBoxModel<Artist> mdl = new DefaultComboBoxModel<Artist>();
	    QueryArtist qa = new QueryArtist();
	    qa.QueryType = Type.OR;
	    qa.JapaneseName = text + "%";
	    qa.TranslatedName = text + "%";
	    qa.RomajiName = text + "%";
	    for(Artist e : DataBase.getArtists(qa)) {
	    	mdl.addElement(e);
	    }
	    return mdl;
	}
}
