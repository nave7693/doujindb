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
		
//		java.sql.Connection conn = null;
//		try
//        {
//            String userName = "admin";
//            String password = "pass";
//            String url = "jdbc:mysql://192.168.1.200/doujindb";
//            Class.forName ("com.mysql.jdbc.Driver").newInstance ();
//            conn = java.sql.DriverManager.getConnection (url, userName, password);
//            System.out.println ("Database connection established");
//        }
//        catch (Exception e)
//        {
//            System.err.println ("Cannot connect to database server : " + e);
//        }
	}
}