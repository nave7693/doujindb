package org.dyndns.doujindb.ui.dialog.util.combobox;

import java.awt.event.*;

import javax.swing.*;

import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.query.Query.Type;
import org.dyndns.doujindb.db.query.QueryCircle;
import org.dyndns.doujindb.db.record.Circle;

@SuppressWarnings("serial")
public class ComboBoxCircle extends SearchComboBox<Circle>
{
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
