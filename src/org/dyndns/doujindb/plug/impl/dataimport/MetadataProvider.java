package org.dyndns.doujindb.plug.impl.dataimport;

import java.awt.Image;
import java.net.URI;

public interface MetadataProvider
{
	public Metadata query(Image image);
	
	public Metadata query(String string);
	
	public Metadata query(URI uri);
}
