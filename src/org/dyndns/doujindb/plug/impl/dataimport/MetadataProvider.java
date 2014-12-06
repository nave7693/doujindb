package org.dyndns.doujindb.plug.impl.dataimport;

import java.awt.Image;
import java.net.URI;

public interface MetadataProvider
{
	public Metadata query(Image image) throws TaskException;
	
	public Metadata query(String string) throws TaskException;
	
	public Metadata query(URI uri) throws TaskException;
}
