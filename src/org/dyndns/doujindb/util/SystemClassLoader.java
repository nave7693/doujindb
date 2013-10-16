package org.dyndns.doujindb.util;

import java.io.*;
import java.lang.reflect.*;
import java.net.*;

/**	 
*	SystemClassLoader.java - ClassLoader utility methods.
*	@author  nozomu
*	@version 1.0
*/
public final class SystemClassLoader
{
	public static void addClassPath(String s) throws IOException
	{
		File f = new File(s);
		addClassPath(f);
	}
	
	public static void addClassPath(File f) throws IOException
	{
		addClassPath(f.toURI().toURL());
	}

	public static void addClassPath(URL url) throws IOException
	{
		URLClassLoader sysloader = (URLClassLoader) ClassLoader.getSystemClassLoader();
		Class<?> sysclass = URLClassLoader.class;
		try
		{
			Method method = sysclass.getDeclaredMethod("addURL", new Class[]{ URL.class });
			method.setAccessible(true);
			method.invoke(sysloader, new Object[]{ url });
		} catch (Throwable t)
		{
			t.printStackTrace();
		}
	}
}
