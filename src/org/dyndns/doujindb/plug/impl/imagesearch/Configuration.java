package org.dyndns.doujindb.plug.impl.imagesearch;

import org.dyndns.doujindb.conf.ConfigurationItem;

public final class Configuration
{
	public static final ConfigurationItem<Integer> query_threshold = new ConfigurationItem<Integer>(75, "Threshold limit for matching image queries");
	public static final ConfigurationItem<Integer> query_maxresult = new ConfigurationItem<Integer>(25, "Max results returned by a single image search");
	public static final ConfigurationItem<Integer> cache_imagescale = new ConfigurationItem<Integer>(16, "Scaling factor of cover image in index file");
}
