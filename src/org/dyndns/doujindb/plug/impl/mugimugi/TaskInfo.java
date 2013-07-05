package org.dyndns.doujindb.plug.impl.mugimugi;

enum TaskInfo {
	IDLE,		// Yet to be started
	RUNNING,	// Still running
	WARNING,	// Completed with warning(s)
	ERROR,		// Automatically stopped by error(s)
	COMPLETED,	// Completed successfully
	PAUSED		// Waiting for user input
}