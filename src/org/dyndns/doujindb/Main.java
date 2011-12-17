package org.dyndns.doujindb;

/**  
* Main.java - DoujinDB entry point.
* @author nozomu
* @version 1.0
* @see Core
*/
public final class Main
{
	public static void main(String[] args)
	{
		new Thread(new Core(), Main.class.getCanonicalName()).start();
	}
}