package org.dyndns.doujindb.plug.impl.dataimport;

import java.io.File;
import java.net.URI;

public interface MetadataProvider
{
	public Metadata query(File image) throws TaskException;
	
	public Metadata query(String string) throws TaskException;
	
	public Metadata query(URI uri) throws TaskException;
}
