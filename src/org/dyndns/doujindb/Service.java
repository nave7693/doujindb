package org.dyndns.doujindb;

/**  
* Service.java - DoujinDB Service.
* @author  nozomu
* @version 1.0
*/
public interface Service
{
	public String getName();
	public void start();
	public void stop();
	public boolean isRunning();
}
