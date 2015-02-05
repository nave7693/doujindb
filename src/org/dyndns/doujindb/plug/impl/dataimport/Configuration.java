package org.dyndns.doujindb.plug.impl.dataimport;

import org.dyndns.doujindb.conf.ConfigurationItem;

public final class Configuration
{
	public static final ConfigurationItem<String> options_http_useragent = new ConfigurationItem<String>("Mozilla/5.0 (compatible; " + DataImport.class.getName() + "/" + DataImport.mVersion + "; +" + DataImport.mWeblink + ")", "User-Agent used in HTTP requests");
	public static final ConfigurationItem<Integer> options_http_timeout = new ConfigurationItem<Integer>(15000, "Timeout value for HTTP connections expressed in milliseconds");
	public static final ConfigurationItem<Boolean> options_autoadd = new ConfigurationItem<Boolean>(true , "Automatically add new entries (Arist, Circle, Parody ...) to the Database without any confirmation");
	public static final ConfigurationItem<Boolean> options_autoresize = new ConfigurationItem<Boolean>(true , "Resize covers automatically before upload");
	public static final ConfigurationItem<Boolean> options_autocrop = new ConfigurationItem<Boolean>(true , "Crop covers automatically before upload");
	public static final ConfigurationItem<Boolean> options_checkdupes = new ConfigurationItem<Boolean>(true , "Automatically find duplicates");
	
	public static final ConfigurationItem<String> provider_mugimugi_apikey = new ConfigurationItem<String>("", "User KEY needed to query doujinshi.mugimugi.org API system");
	public static final ConfigurationItem<Integer> provider_mugimugi_threshold = new ConfigurationItem<Integer>(75, "Threshold limit for matching cover queries");
	public static final ConfigurationItem<Boolean> provider_mugimugi_enable = new ConfigurationItem<Boolean>(true, "Enable MugiMugi metadata provider");
}
