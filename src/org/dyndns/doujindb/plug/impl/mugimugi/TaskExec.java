package org.dyndns.doujindb.plug.impl.mugimugi;

enum TaskExec {
	NO_OPERATION,		// Slacking off
	CHECK_API,			// Checks if API key is set/valid
	CHECK_DUPLICATE,	// Checks if item is a duplicate entry (cover image)
	CHECK_SIMILARITY,	// Checks if item has a similar entry (name)
	SAVE_DATABASE,		// Insert data into the database
	SAVE_DATASTORE,		// Save data files in the datastore
	SCAN_IMAGE,			// Find/resize cover image to be uploaded
	UPLOAD_IMAGE,		// Upload image to mugimugi API system
	PARSE_XML,			// Parse returned XML data
	PARSE_BID,			// Add data directly from a BookID (mugimugi)
	CLEANUP_DATA		// Housekeeping
}