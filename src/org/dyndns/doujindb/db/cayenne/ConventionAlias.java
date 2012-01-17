package org.dyndns.doujindb.db.cayenne;

import org.dyndns.doujindb.db.cayenne.auto._ConventionAlias;

@SuppressWarnings("serial")
public class ConventionAlias extends _ConventionAlias {
    @Override
    protected void postAdd() {
    	super.setTagName("");
    }
}