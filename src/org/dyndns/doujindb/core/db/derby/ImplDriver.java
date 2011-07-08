package org.dyndns.doujindb.core.db.derby;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.*;

import javax.sql.*;

import org.dyndns.doujindb.Core;
import org.dyndns.doujindb.db.*;
import org.dyndns.doujindb.db.records.*;


//import org.apache.derby.jdbc.*;

public class ImplDriver implements DouzDriver
{
	private final String TABLE_PREFIX = "DOUZ_";
	
	private final String TABLE_INSTALL = "INSTALL";
	
	private final String TABLE_BOOK = "BOOK";
	private final String TABLE_TYPE = "TYPE";
	private final String TABLE_CIRCLE = "CIRCLE";
	private final String TABLE_ARTIST = "ARTIST";
	private final String TABLE_CONVENTION = "CONVENTION";
	private final String TABLE_CONTENT = "CONTENT";
	private final String TABLE_PARODY = "PARODY";
	
	private final String TABLE_DELETED = "DELETED";
	private final String TABLE_SHARED = "SHARED";
	private final String TABLE_UNCHECKED = "UNCHECKED";
	
	private final String TABLE_PARODYOF = "PARODYOF";
	private final String TABLE_MEMBEROF = "MEMBEROF";
	private final String TABLE_CONTAINS = "CONTAINS";
	private final String TABLE_DRAWNBY = "DRAWNBY";
	private final String TABLE_PUBLISHEDBY = "PUBLISHEDBY";
	
	private Connection connection;
	
	private ImplTable<Artist> tableArtist;
	private ImplTable<Book> tableBook;
	private ImplTable<Circle> tableCircle;
	private ImplTable<Content> tableContent;
	private ImplTable<Convention> tableConvention;
	private ImplTable<Parody> tableParody;
	private ImplTable<DouzRecord> tableShared;
	private ImplTable<DouzRecord> tableDeleted;
	private ImplTable<DouzRecord> tableUnchecked;
	
	public ImplDriver() throws SQLException
	{
		super();
		System.setProperty("derby.system.home", System.getProperty("user.home") + "/.doujindb");
		System.setProperty("derby.stream.error.file", System.getProperty("user.home") + "/.doujindb/log/derby.log");
		install();
	}
	
	@Override
	public String getConnection() throws DatabaseException
	{
		return "derby://admin:@localhost/dat;create=true";
	}
	
	private void install() throws SQLException
	{
		/*
		DataSource ds = new EmbeddedDataSource();
		ds.setDatabaseName("dat");
		ds.setCreateDatabase("create");
		Connection conn = ds.getConnection("admin", "");
		*/
		DataSource ds;
		URLClassLoader sysloader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        Class<?> sysclass = URLClassLoader.class;
        try
        {
           Method method = sysclass.getDeclaredMethod("addURL", new Class[]{URL.class});
           method.setAccessible(true);
           method.invoke(sysloader, new Object[]{ (new File(System.getProperty("user.home") + "/.doujindb/lib/derby.jar")).toURI().toURL() });
        } catch (Throwable t) {
           throw new SQLException(t);
        }
        try
        {
        	Class<?> dsclass = Class.forName("org.apache.derby.jdbc.EmbeddedDataSource", true, sysloader);
        	ds = (DataSource) dsclass.newInstance();
        	try
	        {
	           Method method = dsclass.getMethod("setDatabaseName", new Class[]{String.class});
	           method.setAccessible(true);
	           method.invoke(ds, new Object[]{ new String("dat") });
	        } catch (Throwable t) {
	        	throw new SQLException(t);
	        }
	        try
	        {
	           Method method = dsclass.getMethod("setCreateDatabase", new Class[]{String.class});
	           method.setAccessible(true);
	           method.invoke(ds, new Object[]{ new String("create") });
	        } catch (Throwable t) {
	        	throw new SQLException(t);
	        }
	        connection = ds.getConnection("admin", "");
			
			if(sqlCreateBase(connection))
			{
				sqlCreateType(connection);
				sqlCreateBook(connection);
				sqlCreateCircle(connection);
				sqlCreateArtist(connection);
				sqlCreateConvention(connection);
				sqlCreateContent(connection);
				sqlCreateParody(connection);
				
				sqlCreateParodyOf(connection);
				sqlCreateMemberOf(connection);
				sqlCreateDrawnBy(connection);
				sqlCreatePublishedBy(connection);
				sqlCreateContains(connection);
				
				sqlCreateDeleted(connection);
				sqlCreateShared(connection);
				sqlCreateUnchecked(connection);
	
				sqlCreateConstraint(connection);
				sqlCreateTriggers(connection);
				
				sqlInsertDefaults(connection);
			}
			
			tableArtist = new ImplTable<Artist>(TABLE_PREFIX + TABLE_ARTIST, connection);
			tableBook = new ImplTable<Book>(TABLE_PREFIX + TABLE_BOOK, connection);
			tableCircle = new ImplTable<Circle>(TABLE_PREFIX + TABLE_CIRCLE, connection);
			tableContent = new ImplTable<Content>(TABLE_PREFIX + TABLE_CONTENT, connection);
			tableConvention = new ImplTable<Convention>(TABLE_PREFIX + TABLE_CONVENTION, connection);
			tableParody = new ImplTable<Parody>(TABLE_PREFIX + TABLE_PARODY, connection);
			tableShared = new ImplTable<DouzRecord>(TABLE_PREFIX + TABLE_SHARED, connection);
			tableDeleted = new ImplTable<DouzRecord>(TABLE_PREFIX + TABLE_DELETED, connection);
			tableUnchecked = new ImplTable<DouzRecord>(TABLE_PREFIX + TABLE_UNCHECKED, connection);
			
		} catch (InstantiationException e) {
			throw new SQLException(e);
		} catch (IllegalAccessException e) {
			throw new SQLException(e);
		} catch (ClassNotFoundException e) {
			throw new SQLException(e);
		}
	}
	
	private boolean sqlCreateBase(Connection conn) throws SQLException
	{
		DatabaseMetaData meta = conn.getMetaData();
		ResultSet rs = meta.getTables(null, null, TABLE_PREFIX + TABLE_INSTALL, null);
		if(!rs.next())
		{
			String query = "CREATE TABLE " + TABLE_PREFIX + TABLE_INSTALL +
				"(ID INTEGER NOT NULL GENERATED BY DEFAULT AS IDENTITY (START WITH 1, INCREMENT BY 1), " +
				"VERSION VARCHAR(255) NOT NULL, " +
				"DATE DATE NOT NULL, " +
				"INFO VARCHAR(255) NOT NULL, " +
				"PRIMARY KEY (ID))";
			Statement stmt = null;
			try
			{
				stmt = conn.createStatement();
				stmt.executeUpdate(query);
				try
				{
					PreparedStatement pstmt = conn.prepareStatement("INSERT INTO " + TABLE_PREFIX + TABLE_INSTALL + " ( ID, VERSION, DATE, INFO ) VALUES ( ?, ?, ?, ? )");
					pstmt.setInt(1, 1);
					pstmt.setString(2, Core.class.getPackage().getSpecificationVersion() == null ? "@null" : Core.class.getPackage().getSpecificationVersion());
					pstmt.setDate(3, new java.sql.Date(new java.util.Date().getTime()));
					pstmt.setString(4, "");
					pstmt.executeUpdate();
					return true;
				} catch (SQLException sqle) {
					sqle.printStackTrace();
					throw sqle;
				} finally {
					if (stmt != null) { stmt.close(); }
				}
			} catch (SQLException sqle) {
				sqle.printStackTrace();
				throw sqle;
			} finally {
				if (stmt != null) { stmt.close(); }
			}
		} else {
			//TODO throw new SQLException("DoujinDB is already installed.");
			return false;
		}
	}
	
	private void sqlCreateType(Connection conn) throws SQLException
	{
		DatabaseMetaData meta = conn.getMetaData();
		ResultSet rs = meta.getTables(null, null, TABLE_PREFIX + TABLE_TYPE, null);
		if(!rs.next())
		{
			String query = "CREATE TABLE " + TABLE_PREFIX + TABLE_TYPE +
				"(ID INTEGER NOT NULL GENERATED BY DEFAULT AS IDENTITY (START WITH 1, INCREMENT BY 1), " +
				"JAPANESE_NAME VARCHAR(255) NOT NULL, " +
				"ROMANJI_NAME VARCHAR(255) NOT NULL, " +
				"ENGLISH_NAME VARCHAR(255) NOT NULL, " +
				"PRIMARY KEY (ID))";
			Statement stmt = null;
			try
			{
				stmt = conn.createStatement();
				stmt.executeUpdate(query);
			} catch (SQLException sqle) {
				sqle.printStackTrace();
				throw sqle;
			} finally {
				if (stmt != null) { stmt.close(); }
			}
		}
	}
	
	private void sqlCreateBook(Connection conn) throws SQLException
	{
		DatabaseMetaData meta = conn.getMetaData();
		ResultSet rs = meta.getTables(null, null, TABLE_PREFIX + TABLE_BOOK, null);
		if(!rs.next())
		{
			String query = "CREATE TABLE " + TABLE_PREFIX + TABLE_BOOK +
				"(ID INTEGER NOT NULL GENERATED BY DEFAULT AS IDENTITY (START WITH 1, INCREMENT BY 1), " +
				"JAPANESE_NAME VARCHAR(255) NOT NULL, " +
				"ROMANJI_NAME VARCHAR(255) NOT NULL, " +
				"ENGLISH_NAME VARCHAR(255) NOT NULL, " +
				"INFO VARCHAR(255) NOT NULL, " +
				"CONVENTION INTEGER NOT NULL, " +
				"ADULT BOOLEAN NOT NULL DEFAULT TRUE, " +
				"DECENSORED BOOLEAN NOT NULL DEFAULT FALSE, " +
				"TRANSLATED BOOLEAN NOT NULL DEFAULT FALSE, " +
				"COLOR BOOLEAN NOT NULL DEFAULT FALSE, " +
				"PAGES INTEGER NOT NULL, " +
				"PUBLISHED DATE NOT NULL, " +
				"TYPE INTEGER NOT NULL, " +
				"PRIMARY KEY (ID))";
			Statement stmt = null;
			try
			{
				stmt = conn.createStatement();
				stmt.executeUpdate(query);
			} catch (SQLException sqle) {
				sqle.printStackTrace();
				throw sqle;
			} finally {
				if (stmt != null) { stmt.close(); }
			}
		}
	}
	
	private void sqlCreateCircle(Connection conn) throws SQLException
	{
		DatabaseMetaData meta = conn.getMetaData();
		ResultSet rs = meta.getTables(null, null, TABLE_PREFIX + TABLE_CIRCLE, null);
		if(!rs.next())
		{
			String query = "CREATE TABLE " + TABLE_PREFIX + TABLE_CIRCLE +
				"(ID INTEGER NOT NULL GENERATED BY DEFAULT AS IDENTITY (START WITH 1, INCREMENT BY 1), " +
				"JAPANESE_NAME VARCHAR(255) NOT NULL, " +
				"ROMANJI_NAME VARCHAR(255) NOT NULL, " +
				"ENGLISH_NAME VARCHAR(255) NOT NULL, " +
				"WEBLINK VARCHAR(255) NOT NULL, " +
				"PRIMARY KEY (ID))";
			Statement stmt = null;
			try
			{
				stmt = conn.createStatement();
				stmt.executeUpdate(query);
			} catch (SQLException sqle) {
				sqle.printStackTrace();
				throw sqle;
			} finally {
				if (stmt != null) { stmt.close(); }
			}
		}
	}
	
	private void sqlCreateArtist(Connection conn) throws SQLException
	{
		DatabaseMetaData meta = conn.getMetaData();
		ResultSet rs = meta.getTables(null, null, TABLE_PREFIX + TABLE_ARTIST, null);
		if(!rs.next())
		{
			String query = "CREATE TABLE " + TABLE_PREFIX + TABLE_ARTIST +
				"(ID INTEGER NOT NULL GENERATED BY DEFAULT AS IDENTITY (START WITH 1, INCREMENT BY 1), " +
				"JAPANESE_NAME VARCHAR(255) NOT NULL, " +
				"ROMANJI_NAME VARCHAR(255) NOT NULL, " +
				"ENGLISH_NAME VARCHAR(255) NOT NULL, " +
				"WEBLINK VARCHAR(255) NOT NULL, " +
				"PRIMARY KEY (ID))";
			Statement stmt = null;
			try
			{
				stmt = conn.createStatement();
				stmt.executeUpdate(query);
			} catch (SQLException sqle) {
				sqle.printStackTrace();
				throw sqle;
			} finally {
				if (stmt != null) { stmt.close(); }
			}
		}
	}
	
	private void sqlCreateContent(Connection conn) throws SQLException
	{
		DatabaseMetaData meta = conn.getMetaData();
		ResultSet rs = meta.getTables(null, null, TABLE_PREFIX + TABLE_CONTENT, null);
		if(!rs.next())
		{
			String query = "CREATE TABLE " + TABLE_PREFIX + TABLE_CONTENT +
				"(ID INTEGER NOT NULL GENERATED BY DEFAULT AS IDENTITY (START WITH 1, INCREMENT BY 1), " +
				"NAME VARCHAR(255) NOT NULL, " +
				"INFO VARCHAR(255) NOT NULL, " +
				"PRIMARY KEY (ID))";
			Statement stmt = null;
			try
			{
				stmt = conn.createStatement();
				stmt.executeUpdate(query);
			} catch (SQLException sqle) {
				sqle.printStackTrace();
				throw sqle;
			} finally {
				if (stmt != null) { stmt.close(); }
			}
		}
	}
	
	private void sqlCreateConvention(Connection conn) throws SQLException
	{
		DatabaseMetaData meta = conn.getMetaData();
		ResultSet rs = meta.getTables(null, null, TABLE_PREFIX + TABLE_CONVENTION, null);
		if(!rs.next())
		{
			String query = "CREATE TABLE " + TABLE_PREFIX + TABLE_CONVENTION +
				"(ID INTEGER NOT NULL GENERATED BY DEFAULT AS IDENTITY (START WITH 1, INCREMENT BY 1), " +
				"TAG_NAME VARCHAR(16) NOT NULL, " +
				"INFO VARCHAR(255) NOT NULL, " +
				"WEBLINK VARCHAR(255) NOT NULL, " +
				"PRIMARY KEY (ID))";
			Statement stmt = null;
			try
			{
				stmt = conn.createStatement();
				stmt.executeUpdate(query);
			} catch (SQLException sqle) {
				sqle.printStackTrace();
				throw sqle;
			} finally {
				if (stmt != null) { stmt.close(); }
			}
		}
	}
	
	private void sqlCreateParody(Connection conn) throws SQLException
	{
		DatabaseMetaData meta = conn.getMetaData();
		ResultSet rs = meta.getTables(null, null, TABLE_PREFIX + TABLE_PARODY, null);
		if(!rs.next())
		{
			String query = "CREATE TABLE " + TABLE_PREFIX + TABLE_PARODY +
				"(ID INTEGER NOT NULL GENERATED BY DEFAULT AS IDENTITY (START WITH 1, INCREMENT BY 1), " +
				"JAPANESE_NAME VARCHAR(255) NOT NULL, " +
				"ROMANJI_NAME VARCHAR(255) NOT NULL, " +
				"ENGLISH_NAME VARCHAR(255) NOT NULL, " +
				"WEBLINK VARCHAR(255) NOT NULL, " +
				"PRIMARY KEY (ID))";
			Statement stmt = null;
			try
			{
				stmt = conn.createStatement();
				stmt.executeUpdate(query);
			} catch (SQLException sqle) {
				sqle.printStackTrace();
				throw sqle;
			} finally {
				if (stmt != null) { stmt.close(); }
			}
		}
	}
	
	private void sqlCreateParodyOf(Connection conn) throws SQLException
	{
		DatabaseMetaData meta = conn.getMetaData();
		ResultSet rs = meta.getTables(null, null, TABLE_PREFIX + TABLE_PARODYOF, null);
		if(!rs.next())
		{
			String query = "CREATE TABLE " + TABLE_PREFIX + TABLE_PARODYOF + " (" + 
				"PARODY_ID INTEGER NOT NULL, " +
				"DOUJIN_ID INTEGER NOT NULL, " +
				"PRIMARY KEY (PARODY_ID, DOUJIN_ID))";
			Statement stmt = null;
			try
			{
				stmt = conn.createStatement();
				stmt.executeUpdate(query);
			} catch (SQLException sqle) {
				sqle.printStackTrace();
				throw sqle;
			} finally {
				if (stmt != null) { stmt.close(); }
			}
		}
	}
	
	private void sqlCreateMemberOf(Connection conn) throws SQLException
	{
		DatabaseMetaData meta = conn.getMetaData();
		ResultSet rs = meta.getTables(null, null, TABLE_PREFIX + TABLE_MEMBEROF, null);
		if(!rs.next())
		{
			String query = "CREATE TABLE " + TABLE_PREFIX + TABLE_MEMBEROF + " (" + 
				"ARTIST_ID INTEGER NOT NULL, " +
				"CIRCLE_ID INTEGER NOT NULL, " +
				"PRIMARY KEY (ARTIST_ID, CIRCLE_ID))";
			Statement stmt = null;
			try
			{
				stmt = conn.createStatement();
				stmt.executeUpdate(query);
			} catch (SQLException sqle) {
				sqle.printStackTrace();
				throw sqle;
			} finally {
				if (stmt != null) { stmt.close(); }
			}
		}
	}
	
	private void sqlCreateDrawnBy(Connection conn) throws SQLException
	{
		DatabaseMetaData meta = conn.getMetaData();
		ResultSet rs = meta.getTables(null, null, TABLE_PREFIX + TABLE_DRAWNBY, null);
		if(!rs.next())
		{
			String query = "CREATE TABLE " + TABLE_PREFIX + TABLE_DRAWNBY + " (" + 
				"ARTIST_ID INTEGER NOT NULL, " +
				"DOUJIN_ID INTEGER NOT NULL, " +
				"PRIMARY KEY (ARTIST_ID, DOUJIN_ID))";
			Statement stmt = null;
			try
			{
				stmt = conn.createStatement();
				stmt.executeUpdate(query);
			} catch (SQLException sqle) {
				sqle.printStackTrace();
				throw sqle;
			} finally {
				if (stmt != null) { stmt.close(); }
			}
		}
	}
	
	private void sqlCreatePublishedBy(Connection conn) throws SQLException
	{
		DatabaseMetaData meta = conn.getMetaData();
		ResultSet rs = meta.getTables(null, null, TABLE_PREFIX + TABLE_PUBLISHEDBY, null);
		if(!rs.next())
		{
			String query = "CREATE TABLE " + TABLE_PREFIX + TABLE_PUBLISHEDBY + " (" + 
				"CIRCLE_ID INTEGER NOT NULL, " +
				"DOUJIN_ID INTEGER NOT NULL, " +
				"PRIMARY KEY (CIRCLE_ID, DOUJIN_ID))";
			Statement stmt = null;
			try
			{
				stmt = conn.createStatement();
				stmt.executeUpdate(query);
			} catch (SQLException sqle) {
				sqle.printStackTrace();
				throw sqle;
			} finally {
				if (stmt != null) { stmt.close(); }
			}
		}
	}
	
	private void sqlCreateContains(Connection conn) throws SQLException
	{
		DatabaseMetaData meta = conn.getMetaData();
		ResultSet rs = meta.getTables(null, null, TABLE_PREFIX + TABLE_CONTAINS, null);
		if(!rs.next())
		{
			String query = "CREATE TABLE " + TABLE_PREFIX + TABLE_CONTAINS + " (" + 
				"CONTENT_ID INTEGER NOT NULL, " +
				"DOUJIN_ID INTEGER NOT NULL, " +
				"PRIMARY KEY (CONTENT_ID, DOUJIN_ID))";
			Statement stmt = null;
			try
			{
				stmt = conn.createStatement();
				stmt.executeUpdate(query);
			} catch (SQLException sqle) {
				sqle.printStackTrace();
				throw sqle;
			} finally {
				if (stmt != null) { stmt.close(); }
			}
		}
	}
	
	private void sqlCreateDeleted(Connection conn) throws SQLException
	{
		DatabaseMetaData meta = conn.getMetaData();
		ResultSet rs = meta.getTables(null, null, TABLE_PREFIX + TABLE_DELETED, null);
		if(!rs.next())
		{
			String query = "CREATE TABLE " + TABLE_PREFIX + TABLE_DELETED +
				"(ID INTEGER NOT NULL, " +
				"PRIMARY KEY (ID))";
			Statement stmt = null;
			try
			{
				stmt = conn.createStatement();
				stmt.executeUpdate(query);
			} catch (SQLException sqle) {
				sqle.printStackTrace();
				throw sqle;
			} finally {
				if (stmt != null) { stmt.close(); }
			}
		}
	}
	
	private void sqlCreateShared(Connection conn) throws SQLException
	{
		DatabaseMetaData meta = conn.getMetaData();
		ResultSet rs = meta.getTables(null, null, TABLE_PREFIX + TABLE_SHARED, null);
		if(!rs.next())
		{
			String query = "CREATE TABLE " + TABLE_PREFIX + TABLE_SHARED +
				"(ID INTEGER NOT NULL, " +
				"PRIMARY KEY (ID))";
			Statement stmt = null;
			try
			{
				stmt = conn.createStatement();
				stmt.executeUpdate(query);
			} catch (SQLException sqle) {
				sqle.printStackTrace();
				throw sqle;
			} finally {
				if (stmt != null) { stmt.close(); }
			}
		}
	}
	
	private void sqlCreateUnchecked(Connection conn) throws SQLException
	{
		DatabaseMetaData meta = conn.getMetaData();
		ResultSet rs = meta.getTables(null, null, TABLE_PREFIX + TABLE_UNCHECKED, null);
		if(!rs.next())
		{
			String query = "CREATE TABLE " + TABLE_PREFIX + TABLE_UNCHECKED +
				"(ID INTEGER NOT NULL, " +
				"PRIMARY KEY (ID))";
			Statement stmt = null;
			try
			{
				stmt = conn.createStatement();
				stmt.executeUpdate(query);
			} catch (SQLException sqle) {
				sqle.printStackTrace();
				throw sqle;
			} finally {
				if (stmt != null) { stmt.close(); }
			}
		}
	}
	
	private void sqlCreateConstraint(Connection conn) throws SQLException
	{
		Statement stmt = null;
		try
		{
			stmt = conn.createStatement();
			stmt.executeUpdate("ALTER TABLE " + TABLE_PREFIX + TABLE_BOOK + " ADD CONSTRAINT TYPE_FK FOREIGN KEY ( TYPE ) REFERENCES " + TABLE_PREFIX + TABLE_TYPE + " ( ID ) ON DELETE NO ACTION ON UPDATE NO ACTION");
			stmt.executeUpdate("ALTER TABLE " + TABLE_PREFIX + TABLE_BOOK + " ADD CONSTRAINT CONVENTION_FK FOREIGN KEY ( CONVENTION ) REFERENCES " + TABLE_PREFIX + TABLE_CONVENTION + " ( ID ) ON DELETE NO ACTION ON UPDATE NO ACTION");
			//stmt.executeUpdate("ALTER TABLE " + TABLE_PREFIX + TABLE_ARTIST + " ADD CONSTRAINT CIRCLE_FK FOREIGN KEY ( CIRCLE ) REFERENCES " + TABLE_PREFIX + TABLE_CIRCLE + " ( ID ) ON DELETE NO ACTION ON UPDATE NO ACTION");
			//stmt.executeUpdate("ALTER TABLE " + TABLE_PREFIX + TABLE_ARTIST + " ADD CONSTRAINT CIRCLE_FK FOREIGN KEY ( CIRCLE ) REFERENCES " + TABLE_PREFIX + TABLE_CIRCLE + " ( ID ) ON DELETE SET NULL");
			stmt.executeUpdate("ALTER TABLE " + TABLE_PREFIX + TABLE_PARODYOF + " ADD CONSTRAINT PARODYOF_FK1 FOREIGN KEY ( PARODY_ID ) REFERENCES " + TABLE_PREFIX + TABLE_PARODY + " ( ID ) ON DELETE NO ACTION ON UPDATE NO ACTION");
			stmt.executeUpdate("ALTER TABLE " + TABLE_PREFIX + TABLE_PARODYOF + " ADD CONSTRAINT PARODYOF_FK2 FOREIGN KEY ( DOUJIN_ID ) REFERENCES " + TABLE_PREFIX + TABLE_BOOK + " ( ID ) ON DELETE NO ACTION ON UPDATE NO ACTION");
			stmt.executeUpdate("ALTER TABLE " + TABLE_PREFIX + TABLE_MEMBEROF + " ADD CONSTRAINT MEMBEROF_FK1 FOREIGN KEY ( ARTIST_ID ) REFERENCES " + TABLE_PREFIX + TABLE_ARTIST + " ( ID ) ON DELETE NO ACTION ON UPDATE NO ACTION");
			stmt.executeUpdate("ALTER TABLE " + TABLE_PREFIX + TABLE_MEMBEROF + " ADD CONSTRAINT MEMBEROF_FK2 FOREIGN KEY ( CIRCLE_ID ) REFERENCES " + TABLE_PREFIX + TABLE_CIRCLE + " ( ID ) ON DELETE NO ACTION ON UPDATE NO ACTION");
			stmt.executeUpdate("ALTER TABLE " + TABLE_PREFIX + TABLE_DRAWNBY + " ADD CONSTRAINT DRAWNBY_FK1 FOREIGN KEY ( ARTIST_ID ) REFERENCES " + TABLE_PREFIX + TABLE_ARTIST + " ( ID ) ON DELETE NO ACTION ON UPDATE NO ACTION");
			stmt.executeUpdate("ALTER TABLE " + TABLE_PREFIX + TABLE_DRAWNBY + " ADD CONSTRAINT DRAWNBY_FK2 FOREIGN KEY ( DOUJIN_ID ) REFERENCES " + TABLE_PREFIX + TABLE_BOOK + " ( ID ) ON DELETE NO ACTION ON UPDATE NO ACTION");
			stmt.executeUpdate("ALTER TABLE " + TABLE_PREFIX + TABLE_PUBLISHEDBY + " ADD CONSTRAINT PUBLISHEDBY_FK1 FOREIGN KEY ( DOUJIN_ID ) REFERENCES " + TABLE_PREFIX + TABLE_BOOK + " ( ID ) ON DELETE NO ACTION ON UPDATE NO ACTION");
			stmt.executeUpdate("ALTER TABLE " + TABLE_PREFIX + TABLE_PUBLISHEDBY + " ADD CONSTRAINT PUBLISHEDBY_FK2 FOREIGN KEY ( CIRCLE_ID ) REFERENCES " + TABLE_PREFIX + TABLE_CIRCLE + " ( ID ) ON DELETE NO ACTION ON UPDATE NO ACTION");
			stmt.executeUpdate("ALTER TABLE " + TABLE_PREFIX + TABLE_CONTAINS + " ADD CONSTRAINT CONTAINS_FK1 FOREIGN KEY ( DOUJIN_ID ) REFERENCES " + TABLE_PREFIX + TABLE_BOOK + " ( ID ) ON DELETE NO ACTION ON UPDATE NO ACTION");
			stmt.executeUpdate("ALTER TABLE " + TABLE_PREFIX + TABLE_CONTAINS + " ADD CONSTRAINT CONTAINS_FK2 FOREIGN KEY ( CONTENT_ID ) REFERENCES " + TABLE_PREFIX + TABLE_CONTENT + " ( ID ) ON DELETE NO ACTION ON UPDATE NO ACTION");
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw sqle;
		} finally {
			if (stmt != null) { stmt.close(); }
		}
	}
	
	private void sqlCreateTriggers(Connection conn) throws SQLException
	{
		Statement stmt = null;
		try
		{
			stmt = conn.createStatement();
			//stmt.executeUpdate("");
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw sqle;
		} finally {
			if (stmt != null) { stmt.close(); }
		}
	}
	
	private void sqlInsertDefaults(Connection conn) throws SQLException
	{
		Statement stmt = null;
		try
		{
			stmt = conn.createStatement();
			/*
			stmt.execute("INSERT INTO " + TABLE_PREFIX + TABLE_TYPE + " ( SELECT 1 as ID, '不詳' as JAPANESE_NAME, 'UNKNOWN' as ROMANJI_NAME, 'UNKNOWN' as ENGLISH_NAME FROM DOUZ_TYPE WHERE ID = 1 AND JAPANESE_NAME = '不詳' AND ROMANJI_NAME = 'UNKNOWN' AND ENGLISH_NAME = 'UNKNOWN' HAVING count(*)=0 )");
			stmt.execute("INSERT INTO " + TABLE_PREFIX + TABLE_TYPE + " ( SELECT 2 as ID, '同人誌' as JAPANESE_NAME, 'Doujinshi' as ROMANJI_NAME, 'Doujinshi' as ENGLISH_NAME FROM DOUZ_TYPE WHERE ID = 2 AND JAPANESE_NAME = '同人誌' AND ROMANJI_NAME = 'Doujinshi' AND ENGLISH_NAME = 'Doujinshi' HAVING count(*)=0 )");
			stmt.execute("INSERT INTO " + TABLE_PREFIX + TABLE_TYPE + " ( SELECT 3 as ID, '同人ソフト' as JAPANESE_NAME, 'Doujin Soft' as ROMANJI_NAME, 'Doujin Soft' as ENGLISH_NAME FROM DOUZ_TYPE WHERE ID = 3 AND JAPANESE_NAME = '同人ソフト' AND ROMANJI_NAME = 'Doujin Soft' AND ENGLISH_NAME = 'Doujin Soft' HAVING count(*)=0 )");
			stmt.execute("INSERT INTO " + TABLE_PREFIX + TABLE_TYPE + " ( SELECT 4 as ID, '同人CG' as JAPANESE_NAME, 'Doujin CG' as ROMANJI_NAME, 'Doujin CG' as ENGLISH_NAME FROM DOUZ_TYPE WHERE ID = 4 AND JAPANESE_NAME = '同人CG' AND ROMANJI_NAME = 'Doujin CG' AND ENGLISH_NAME = 'Doujin CG' HAVING count(*)=0 )");
			stmt.execute("INSERT INTO " + TABLE_PREFIX + TABLE_TYPE + " ( SELECT 5 as ID, '漫画' as JAPANESE_NAME, 'Manga' as ROMANJI_NAME, 'Manga' as ENGLISH_NAME FROM DOUZ_TYPE WHERE ID = 5 AND JAPANESE_NAME = '漫画' AND ROMANJI_NAME = 'Manga' AND ENGLISH_NAME = 'Manga' HAVING count(*)=0 )");
			*/
			stmt.execute("INSERT INTO " + TABLE_PREFIX + TABLE_TYPE + " VALUES ( 1, '不詳', 'UNKNOWN', 'UNKNOWN' )");
			stmt.execute("INSERT INTO " + TABLE_PREFIX + TABLE_TYPE + " VALUES ( 2, '同人誌', 'Doujinshi', 'Doujinshi' )");
			stmt.execute("INSERT INTO " + TABLE_PREFIX + TABLE_TYPE + " VALUES ( 3, '同人ソフト', 'Doujin Soft', 'Doujin Soft' )");
			stmt.execute("INSERT INTO " + TABLE_PREFIX + TABLE_TYPE + " VALUES ( 4, '同人CG', 'Doujin CG', 'Doujin CG' )");
			stmt.execute("INSERT INTO " + TABLE_PREFIX + TABLE_TYPE + " VALUES ( 5, '漫画', 'Manga', 'Manga' )");
			stmt.execute("INSERT INTO " + TABLE_PREFIX + TABLE_CIRCLE + " VALUES ( 1, '不詳', 'UNKNOWN', 'UNKNOWN', '' )");
			stmt.execute("INSERT INTO " + TABLE_PREFIX + TABLE_ARTIST + " VALUES ( 1, '不詳', 'UNKNOWN', 'UNKNOWN', '')");
			stmt.execute("INSERT INTO " + TABLE_PREFIX + TABLE_CONVENTION + " VALUES ( 1, '不詳', '', '' )");
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw sqle;
		} finally {
			if (stmt != null) { stmt.close(); }
		}
	}

	@Override
	public void commit() throws DatabaseException
	{
		try {
			connection.commit();
		} catch (SQLException sqle) {
			throw new DatabaseException(sqle);
		}
	}

	@Override
	public void rollback() throws DatabaseException
	{
		try {
			connection.rollback();
		} catch (SQLException sqle) {
			throw new DatabaseException(sqle);
		}
	}
	
	@Override
	public boolean getAutoCommit() throws DatabaseException
	{
		try {
			return connection.getAutoCommit();
		} catch (SQLException sqle) {
			throw new DatabaseException(sqle);
		}
	}

	@Override
	public void setAutoCommit(boolean autoCommit) throws DatabaseException
	{
		try {
			connection.setAutoCommit(autoCommit);
		} catch (SQLException sqle) {
			throw new DatabaseException(sqle);
		}
	}

	@Override
	public DouzTable<Book> getBooks() throws DatabaseException
	{
		return tableBook;
	}

	@Override
	public DouzTable<Circle> getCircles() throws DatabaseException
	{
		return tableCircle;
	}

	@Override
	public DouzTable<Artist> getArtists() throws DatabaseException
	{
		return tableArtist;
	}

	@Override
	public DouzTable<Parody> getParodies() throws DatabaseException
	{
		return tableParody;
	}

	@Override
	public DouzTable<Content> getContents() throws DatabaseException
	{
		return tableContent;
	}

	@Override
	public DouzTable<Convention> getConventions() throws DatabaseException
	{
		return tableConvention;
	}

	@Override
	public DouzTable<DouzRecord> getDeleted() throws DatabaseException
	{
		return tableDeleted;
	}

	@Override
	public DouzTable<DouzRecord> getShared() throws DatabaseException
	{
		return tableShared;
	}

	@Override
	public DouzTable<DouzRecord> getUnchecked() throws DatabaseException
	{
		return tableUnchecked;
	}

	@Override
	public Artist newArtist() throws DatabaseException
	{
		return new ImplArtist(connection);
	}

	@Override
	public Book newBook() throws DatabaseException
	{
		return new ImplBook(connection);
	}

	@Override
	public Circle newCircle() throws DatabaseException
	{
		return new ImplCircle(connection);
	}

	@Override
	public Content newContent() throws DatabaseException
	{
		return new ImplContent(connection);
	}

	@Override
	public Convention newConvention() throws DatabaseException
	{
		return new ImplConvention(connection);
	}

	@Override
	public Parody newParody() throws DatabaseException
	{
		return new ImplParody(connection);
	}
}
