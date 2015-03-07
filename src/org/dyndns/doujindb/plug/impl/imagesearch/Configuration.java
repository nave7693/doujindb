package org.dyndns.doujindb.plug.impl.imagesearch;

import org.dyndns.doujindb.conf.ConfigurationItem;
import org.dyndns.doujindb.conf.ConfigurationItem.Validator;

public final class Configuration
{
	public static final ConfigurationItem<Integer> query_threshold = new ConfigurationItem<Integer>(16, "Threshold limit for matching image queries");
	public static final ConfigurationItem<Integer> query_maxresult = new ConfigurationItem<Integer>(25, "Max results returned by a single image search");
	public static final ConfigurationItem<Integer> hashdb_threads = new ConfigurationItem<Integer>(1, 1, "Concurrent threads used to build hash database", new Validator<Integer>() {
		@Override
		public boolean isValid(Integer value) {
			return value > 0 && value <= Runtime.getRuntime().availableProcessors();
		}
	});
	public static final ConfigurationItem<Integer> hashdb_size = new ConfigurationItem<Integer>(16, "Hash size (pixel). Higher numbers add precision to image search but also slow it down and increase hashdb size (disk and memory)");
}
