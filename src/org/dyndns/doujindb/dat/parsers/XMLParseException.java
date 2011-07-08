package org.dyndns.doujindb.dat.parsers;

@SuppressWarnings("serial")
public final class XMLParseException extends RuntimeException
{

	public XMLParseException() {
		super();
	}

	public XMLParseException(String s) {
		super(s);
	}

	public XMLParseException(Throwable t) {
		super(t);
	}

	public XMLParseException(String s, Throwable t) {
		super(s, t);
	}

}
