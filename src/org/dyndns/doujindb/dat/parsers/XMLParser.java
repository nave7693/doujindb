package org.dyndns.doujindb.dat.parsers;

import java.io.Serializable;

public interface XMLParser
{
	public <T extends Serializable> T read() throws XMLParseException;
	public <T extends Serializable> void write(T object) throws XMLParseException;
}
