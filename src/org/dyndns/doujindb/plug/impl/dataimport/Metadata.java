package org.dyndns.doujindb.plug.impl.dataimport;

interface Metadata
{
	public String name();
	public Iterable<String> alias();
	public String translation();
	public Integer pages();
	public Long timestamp();
	public String type();
	public Long size();
	public Iterable<String> artist();
	public Iterable<String> circle();
	public String convention();
	public Iterable<String> content();
	public Iterable<String> parody();
	public String uri();
}
