package org.dyndns.doujindb.db.cayenne;

import org.dyndns.doujindb.db.cayenne.auto._ContentAlias;

@SuppressWarnings("serial")
public class ContentAlias extends _ContentAlias {
    @Override
    protected void postAdd() {
    	super.setTagName("");
    }
}