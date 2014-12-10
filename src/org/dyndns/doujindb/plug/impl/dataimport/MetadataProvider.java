package org.dyndns.doujindb.plug.impl.dataimport;

import java.io.File;
import java.net.URI;

abstract class MetadataProvider
{
	public abstract Metadata query(File image) throws TaskException;
	
	public abstract Metadata query(String name) throws TaskException;
	
	public abstract Metadata query(URI uri) throws TaskException;

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}
}
