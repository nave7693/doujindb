package org.dyndns.doujindb.plug.impl.dataimport;

import java.io.File;
import java.net.URI;

abstract class MetadataProvider
{
	public abstract Metadata query(File image);
	
	public abstract Metadata query(String name);
	
	public abstract Metadata query(URI uri);
	
	public abstract boolean isEnabled();

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}
}
