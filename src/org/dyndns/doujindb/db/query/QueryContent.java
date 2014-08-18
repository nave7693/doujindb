package org.dyndns.doujindb.db.query;

import org.dyndns.doujindb.db.record.Content;

public final class QueryContent extends Query<Content>
{
	public Integer Id = null;
	public String TagName = null;
	public Content.Namespace Namespace = null;
}