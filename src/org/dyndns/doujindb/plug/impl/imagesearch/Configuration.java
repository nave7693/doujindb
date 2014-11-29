package org.dyndns.doujindb.plug.impl.imagesearch;

import org.dyndns.doujindb.conf.ConfigurationItem;
import org.dyndns.doujindb.conf.ConfigurationItem.Validator;

public final class Configuration
{
	public static final ConfigurationItem<Integer> query_threshold = new ConfigurationItem<Integer>(3, "Threshold limit for matching image queries");
	public static final ConfigurationItem<Integer> query_maxresult = new ConfigurationItem<Integer>(25, "Max results returned by a single image search");
	public static final ConfigurationItem<Integer> hashdb_threads = new ConfigurationItem<Integer>(1, 1, "Concurrent threads used to build hash database", new Validator<Integer>() {
		@Override
		public boolean isValid(Integer value) {
			return value > 0 && value <= Runtime.getRuntime().availableProcessors();
		}
	});
}
