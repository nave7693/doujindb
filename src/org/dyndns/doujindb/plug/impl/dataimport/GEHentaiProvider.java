package org.dyndns.doujindb.plug.impl.dataimport;

import java.net.*;
import java.io.*;

import javax.xml.bind.annotation.*;

import org.dyndns.doujindb.plug.impl.dataimport.MugiMugiProvider.Metadata;

final class GEHentaiProvider extends MetadataProvider
{
	@XmlRootElement
	@XmlType(namespace="org.e-hentai.g", name="Metadata")
	public static final class Metadata extends org.dyndns.doujindb.plug.impl.dataimport.Metadata {
		Metadata() { }
	}
	
	@Override
	public Metadata query(File image) {
		try {
			// FIXME Implement GEHentaiProvider.query(File)
			throw new TaskException("Method not implemented");
		} catch (TaskException te) {
			Metadata md = new Metadata();
			md.message = te.getMessage();
			md.exception(te);
			return md;
		}
	}

	@Override
	public Metadata query(String name) {
		try {
			// FIXME Implement GEHentaiProvider.query(String)
			throw new TaskException("Method not implemented");
		} catch (TaskException te) {
			Metadata md = new Metadata();
			md.message = te.getMessage();
			md.exception(te);
			return md;
		}
	}

	@Override
	public Metadata query(URI uri) {
		try {
			// FIXME Implement GEHentaiProvider.query(URI)
			throw new TaskException("Method not implemented");
		} catch (TaskException te) {
			Metadata md = new Metadata();
			md.message = te.getMessage();
			md.exception(te);
			return md;
		}
	}

}
