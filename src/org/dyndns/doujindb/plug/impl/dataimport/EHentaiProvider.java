package org.dyndns.doujindb.plug.impl.dataimport;

import java.net.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.*;

import javax.xml.bind.annotation.*;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;

final class EHentaiProvider extends MetadataProvider {
	
	private static Pattern mURLPattern = Pattern.compile("(http://(ex|g\\.e[-])hentai\\.org/g/)(?<gid>[0-9]+)(?<gtoken>[a-f0-9]+)(/.*)");
	
	private static final Logger LOG = (Logger) LoggerFactory.getLogger(EHentaiProvider.class);
	
	@XmlRootElement
	@XmlType(namespace="org.e-hentai.g", name="Metadata")
	public static final class Metadata extends org.dyndns.doujindb.plug.impl.dataimport.Metadata {
		Metadata() { }

		@Override
		public String provider() {
			return "e-hentai";
		}
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
			// Extract E-Hentai gallery Id and Token from URI
			Matcher matcher = mURLPattern.matcher(uri.toString());
			if(!matcher.find())
				throw new TaskException("Invalid E-Hentai URI " + uri);
			String galleryId = matcher.group("gid");
			String galleryToken = matcher.group("gtoken");
			// Query JSON API
			//FIXME URLConnection urlc = new URL("http://g.e-hentai.org/api.php").openConnection();
			throw new TaskException("Method not implemented");
		} catch (TaskException te) {
			Metadata md = new Metadata();
			md.message = te.getMessage();
			md.exception(te);
			return md;
		}
	}

	@Override
	public boolean isEnabled() {
		return Configuration.provider_ehentai_enable.get();
	}

}
