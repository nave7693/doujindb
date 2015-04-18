package org.dyndns.doujindb.plug.impl.dataimport;

import org.dyndns.doujindb.conf.ConfigurationItem;

public final class Configuration
{
	public static final ConfigurationItem<String> options_http_useragent = new ConfigurationItem<String>("Mozilla/5.0 (compatible; " + DataImport.class.getName() + "/" + DataImport.mVersion + "; +" + DataImport.mWeblink + ")", "User-Agent used in HTTP requests");
	public static final ConfigurationItem<Integer> options_http_timeout = new ConfigurationItem<Integer>(15000, "Timeout value for HTTP connections expressed in milliseconds");
	public static final ConfigurationItem<Boolean> options_autoadd = new ConfigurationItem<Boolean>(true , "Automatically add new entries (Arist, Circle, Parody ...) to the Database without any confirmation");
	public static final ConfigurationItem<Boolean> options_autoresize = new ConfigurationItem<Boolean>(true , "Resize covers automatically before upload");
	public static final ConfigurationItem<Boolean> options_autocrop = new ConfigurationItem<Boolean>(true , "Crop covers automatically before upload");
	public static final ConfigurationItem<Boolean> options_check_similar = new ConfigurationItem<Boolean>(true , "Automatically find duplicates (cover image)");
	public static final ConfigurationItem<Boolean> options_check_duplicate = new ConfigurationItem<Boolean>(true , "Automatically find duplicates (Metadata info)");
	
	public static final ConfigurationItem<String> provider_mugimugi_apikey = new ConfigurationItem<String>("", "User KEY needed to query doujinshi.mugimugi.org API system");
	public static final ConfigurationItem<Integer> provider_mugimugi_threshold = new ConfigurationItem<Integer>(75, "Threshold limit for matching cover queries");
	public static final ConfigurationItem<Boolean> provider_mugimugi_enable = new ConfigurationItem<Boolean>(true, "Enable MugiMugi metadata provider");
	
	public static final ConfigurationItem<String> provider_ehentai_cookie = new ConfigurationItem<String>("", "E-Hentai HTTP cookie");
	public static final ConfigurationItem<Boolean> provider_ehentai_useex = new ConfigurationItem<Boolean>(false, "Use ExHentai instead of E-Hentai");
	public static final ConfigurationItem<Boolean> provider_ehentai_similarity = new ConfigurationItem<Boolean>(true, "Use Similarity Scan");
	public static final ConfigurationItem<Boolean> provider_ehentai_coversonly = new ConfigurationItem<Boolean>(false, "Only Search Covers");
	public static final ConfigurationItem<Boolean> provider_ehentai_showexpunged = new ConfigurationItem<Boolean>(false, "Show Expunged");
	public static final ConfigurationItem<Boolean> provider_ehentai_enable = new ConfigurationItem<Boolean>(true, "Enable E-Hentai metadata provider");

}
