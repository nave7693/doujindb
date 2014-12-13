package org.dyndns.doujindb.plug.impl.dataimport;

import java.net.*;
import java.io.*;

import javax.xml.bind.annotation.*;

final class GEHentaiProvider extends MetadataProvider
{
	@XmlRootElement
	@XmlType(namespace="org.e-hentai.g", name="Metadata")
	public static final class Metadata extends org.dyndns.doujindb.plug.impl.dataimport.Metadata {
		Metadata() { }
	}
	
	@Override
	public Metadata query(File image) throws TaskException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Metadata query(String name) throws TaskException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Metadata query(URI uri) throws TaskException {
		// TODO Auto-generated method stub
		return null;
	}

}
